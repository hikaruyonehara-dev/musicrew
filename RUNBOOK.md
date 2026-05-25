# Musicrew Runbook

> When the site breaks, **start here**. Scenarios are listed roughly in "most likely first" order. Every command assumes you've SSH'd into the EC2 as `ubuntu`.

---

## Quick diagnosis — run these first

Don't know what's wrong? Run all seven in order. The output tells you ~90% of what you need to triage.

```bash
# 1. Is the site responding from the outside?
curl -sS -o /dev/null -w "HTTP %{http_code}\n" https://musicrew.duckdns.org/

# 2. Is the Spring app running?
sudo systemctl is-active musicrew

# 3. Is nginx running?
sudo systemctl is-active nginx

# 4. Is Postgres running?
sudo systemctl is-active postgresql

# 5. Is the disk healthy?
df -h /

# 6. Recent app logs (last 50 lines)
sudo journalctl -u musicrew -n 50 --no-pager

# 7. Recent nginx errors
sudo tail -20 /var/log/nginx/error.log
```

Whichever one returns "inactive", "100%", or shows the exception — jump to the matching scenario below.

---

## Scenarios

### 1. Site shows 502 Bad Gateway

**Symptom:** Browser shows "502 Bad Gateway" or curl returns `502`.

**Likely cause:** nginx is up but can't reach Spring on `localhost:8080`. Spring crashed, is restarting, or never started.

**Fix:**
```bash
sudo systemctl status musicrew --no-pager
sudo journalctl -u musicrew -n 100 --no-pager
sudo systemctl restart musicrew
```

If Spring keeps crashing after restart, the journalctl output has the actual exception — usually a DB issue (scenario 3) or a regression from a recent commit.

### 2. Site shows "Connection refused" or "This site can't be reached"

**Symptom:** Browser can't reach the server at all.

**Likely cause:** nginx is down, port 443 closed, or DuckDNS points at the wrong IP.

**Fix:**
```bash
sudo systemctl status nginx --no-pager
sudo systemctl restart nginx

# Verify DuckDNS still points at this box
curl -4 -s ifconfig.me            # EC2's current public IP
ping -c 1 musicrew.duckdns.org    # what DuckDNS resolves to
```

If the two IPs differ, see scenario 9 (renew DuckDNS).

### 3. Spring fails to start with a DB error

**Symptom:** `journalctl -u musicrew` shows `PSQLException: FATAL: password authentication failed` or `Connection refused: localhost:5432`.

**Likely cause:** Postgres is down, OR the credentials in `/etc/musicrew/env` don't match the actual DB password.

**Fix:**
```bash
# Is Postgres running?
sudo systemctl status postgresql --no-pager
sudo systemctl restart postgresql

# Can the app user log in?
psql -h localhost -U musicrew_app -d musicrew -c '\dt'   # prompts for password

# If the password is wrong, reset both sides
sudo -u postgres psql -c "ALTER USER musicrew_app WITH PASSWORD 'NewPass';"
sudo nano /etc/musicrew/env       # update DB_PASSWORD to match
sudo systemctl restart musicrew
```

### 4. HTTPS broken / "Your connection is not private"

**Symptom:** Browser shows the cert warning page; curl on HTTPS returns SSL errors.

**Likely cause:** Cert expired (rare — auto-renewal runs twice daily) or nginx lost its TLS config.

**Fix:**
```bash
sudo certbot certificates           # shows expiry dates
sudo certbot renew --dry-run        # tests renewal without changing anything
sudo certbot renew --force-renewal -d musicrew.duckdns.org
sudo systemctl reload nginx
```

### 5. Disk full

**Symptom:** `df -h /` shows 100%; the site may be down with cascading errors. The `musicrew-disk.log` will have recent `WARN` lines.

**Likely culprits:** Postgres data growth, profile-pic uploads, old backups, system logs.

**Fix — find the offender, then trim:**
```bash
# Top-level usage
sudo du -h --max-depth=1 / 2>/dev/null | sort -hr | head

# Likely directories
sudo du -sh /var/log /var/lib/postgresql /var/musicrew/uploads /var/backups

# Trim systemd journal to last 7 days
sudo journalctl --vacuum-time=7d

# Trim oldest backups (script only keeps 7 — if more, something's wrong)
ls -lt /var/backups/musicrew/
```

### 6. CI/CD deploy fails

**Symptom:** GitHub Actions run is red; latest commit on `main` didn't reach prod.

**Likely cause (in order):**
- `EC2_HOST` GitHub secret is stale (e.g. EC2 got a new IP since the secret was set)
- `EC2_SSH_KEY` secret invalid or rotated
- Build error (rare — would have failed locally first)

**Fix:**
```bash
gh run list --limit 3
gh run view <RUN_ID> --log-failed
```

For the IP-change case: update the `EC2_HOST` secret to `musicrew.duckdns.org` instead of a literal IP, so future IP changes don't break CI/CD.

### 7. EC2 rebooted (unattended-upgrades or AWS maintenance)

**Symptom:** Brief outage around 04:00 server time, or after AWS scheduled maintenance.

**Likely cause:** Kernel patch triggered auto-reboot. Services come back on boot automatically (all three have `WantedBy=multi-user.target`).

**Fix:** Usually nothing — wait ~60 seconds and verify:
```bash
sudo systemctl is-active musicrew nginx postgresql
```
All three should say `active`. If any aren't, `systemctl restart <name>`.

### 8. Restore from a backup

**Symptom:** Data is gone, corrupted, or you want to roll back to yesterday's state.

**Procedure (destructive — read carefully):**
```bash
# 1. Stop the app so no new writes happen during restore
sudo systemctl stop musicrew

# 2. List available backups (newest first)
ls -lht /var/backups/musicrew/

# 3. Pick a dump
BACKUP=/var/backups/musicrew/musicrew-YYYYMMDD-HHMMSS.sql.gz

# 4. Drop and recreate the DB (THIS DELETES CURRENT DATA)
sudo -u postgres psql <<SQL
DROP DATABASE musicrew;
CREATE DATABASE musicrew;
GRANT ALL PRIVILEGES ON DATABASE musicrew TO musicrew_app;
\c musicrew
GRANT ALL ON SCHEMA public TO musicrew_app;
SQL

# 5. Restore
gunzip -c $BACKUP | sudo -u postgres psql musicrew

# 6. Restart the app
sudo systemctl start musicrew
```

### 9. EC2 got a new IP — update DuckDNS

**Symptom:** `ping musicrew.duckdns.org` returns an IP that's not the current EC2's; site appears down to outside world.

**Fix:**
```bash
# Get the EC2's current public IP
NEW_IP=$(curl -4 -s ifconfig.me) && echo $NEW_IP

# Update DuckDNS via API
curl "https://www.duckdns.org/update?domains=musicrew&token=YOUR_TOKEN&ip=${NEW_IP}"
```

DuckDNS token is at the top of the dashboard at https://www.duckdns.org/. The update is typically reflected within ~60 seconds. Verify with `ping musicrew.duckdns.org`.

If the IP change happens often, set up the [DuckDNS auto-update cron](https://www.duckdns.org/install.jsp) so it self-heals.

### 10. unattended-upgrades broke something

**Symptom:** Something worked yesterday, doesn't today, no code change made.

**Diagnosis:**
```bash
# What did the upgrader do last night?
ls -lt /var/log/unattended-upgrades/
sudo tail -50 /var/log/unattended-upgrades/unattended-upgrades.log
```

If a package upgrade is the culprit, you can roll back:
```bash
# Show recent apt history
sudo grep -E "^(Start-Date|Commandline|Upgrade)" /var/log/apt/history.log | tail -30

# Roll back a specific package
sudo apt install <pkg>=<old-version>
```

---

## Logs to know

| Log | Path | What it has |
|---|---|---|
| Spring app | `journalctl -u musicrew` | Boot, requests, exceptions |
| nginx access | `/var/log/nginx/access.log` | Every HTTP request |
| nginx error | `/var/log/nginx/error.log` | 502s, upstream errors, config errors |
| Backup runs | `/var/log/musicrew-backup.log` | One line per nightly dump |
| Disk check | `/var/log/musicrew-disk.log` | One line per hour |
| Postgres | `/var/log/postgresql/postgresql-*.log` | Slow queries, auth failures |
| Unattended upgrades | `/var/log/unattended-upgrades/` | Nightly patch installs |
| Let's Encrypt | `/var/log/letsencrypt/letsencrypt.log` | Cert renewals |

---

## Useful one-liners

```bash
# How much data is in Postgres?
sudo -u postgres psql musicrew -c "SELECT pg_size_pretty(pg_database_size('musicrew'));"

# Top tables by row count
sudo -u postgres psql musicrew -c "SELECT relname, n_live_tup FROM pg_stat_user_tables ORDER BY n_live_tup DESC LIMIT 10;"

# Who's "online" right now (last_seen_at within 5 min)
sudo -u postgres psql musicrew -c "SELECT email FROM users WHERE last_seen_at > NOW() - INTERVAL '5 minutes';"

# Tail every important log at once
sudo journalctl -u musicrew -f &
sudo tail -f /var/log/nginx/error.log /var/log/musicrew-disk.log

# Manual backup right now
sudo /usr/local/bin/musicrew-backup.sh

# Force certbot to renew the TLS cert
sudo certbot renew --force-renewal -d musicrew.duckdns.org && sudo systemctl reload nginx
```

---

## Where everything lives

| Thing | Path |
|---|---|
| App JAR | `/opt/musicrew/musicrew.jar` |
| systemd unit | `/etc/systemd/system/musicrew.service` |
| Env vars (DB password etc.) | `/etc/musicrew/env` (root-only, mode 600) |
| Profile pic uploads | `/var/musicrew/uploads/` |
| Postgres data dir | `/var/lib/postgresql/16/main/` |
| Postgres backups | `/var/backups/musicrew/` |
| nginx config | `/etc/nginx/sites-available/musicrew` |
| TLS cert + key | `/etc/letsencrypt/live/musicrew.duckdns.org/` |
| Backup script | `/usr/local/bin/musicrew-backup.sh` |
| Disk-check script | `/usr/local/bin/musicrew-disk-check.sh` |

---

Built with [Claude Code](https://claude.com/claude-code).
