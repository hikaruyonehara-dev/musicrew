# Musicrew

> 音楽仲間を見つけるためのウェブアプリ
> A web app for musicians to find bandmates — built end-to-end as a solo SDLC exercise.

**デモ / Demo:** [`https://musicrew.duckdns.org`](https://musicrew.duckdns.org)
**ユーザー / User:** `aki@musicrew.test`
**パスワード / Password:** `Password`

---

## 日本語

ミュージシャンがプロフィール (楽器・ジャンル・所在地・演奏動画・配信リンク) を作成し、Instagram 風のフィードでアクティブなバンドメンバー募集を閲覧、フィルターで他のミュージシャンを検索、1 対 1 で会話できるウェブアプリです。アカウント管理 (非公開 / 退会) と、ブロック・通報・管理者によるモデレーション機能まで含めて作っています。GitHub Actions で EC2 へ継続的デプロイ。

要件定義 → 設計 → 実装 → テスト → デプロイの SDLC サイクルを通して構築しています。

### 技術スタック

| レイヤー | 採用技術 |
|---|---|
| 言語 / ランタイム | Java 21 (LTS) |
| フレームワーク | Spring Boot 3.3 |
| 永続化 | Spring Data JPA + Hibernate、H2 (開発)、PostgreSQL 対応 (本番) |
| セキュリティ | Spring Security 6 — フォームログイン、BCrypt、CSRF、ロールベースアクセス制御 (`USER` / `ADMIN`) |
| ビュー | Thymeleaf + フラグメント方式のレイアウト |
| スタイリング | Tailwind CSS (Play CDN)、Inter フォント、Resend 風ダークテーマ |
| ビルド | Maven、`spring-boot-maven-plugin` による fat-JAR |
| 画像処理 | Thumbnailator (サーバーサイドで 1024×1024 にリサイズ) |
| ファイルストレージ | ローカルディスク (`LocalDiskFileStorageService`) |
| テスト | JUnit 5、AssertJ、MockMvc、`@DataJpaTest`、`spring-security-test` |
| CI / CD | GitHub Actions — `main` へのプッシュでビルド + scp + `systemctl restart` |
| デプロイ | EC2 (Ubuntu 24.04) + `systemd` + nginx リバースプロキシ + Let's Encrypt HTTPS |

### 機能

#### 認証
- 18 歳以上チェック付きのメール + パスワードサインアップ
- BCrypt によるパスワードハッシュ化
- セッションベースのログイン、CSRF 対策、POST によるセキュアなログアウト
- サインアップ時のスキルレベル選択 (Beginner / Intermediate / Advanced / Pro)
- Spring Security の権限機構を使ったロールベースアクセス (`USER` / `ADMIN`)
- **パスワードリセット**: 安全に生成されたトークン (256bit) をハッシュ化して DB に保存、1 時間で有効期限切れ、1 回限りの使い切り

#### プロフィール
- 表示名、自己紹介、国 (ISO 3166)、都市、スキルバッジ
- 楽器・ジャンルをキュレートされたリストから複数選択
- プロフィール画像のアップロード (サーバーサイドリサイズ + コンテンツタイプ検証)
- 演奏動画リンクを複数登録 (YouTube / Vimeo / SoundCloud のホワイトリスト)
- 音楽配信リンクを複数登録 (TuneCore / DistroKid / CDBaby / UnitedMasters)
- 画像 + 演奏動画 1 本以上を登録するまで検索結果に表示されない
- **オンライン状態** — 過去 5 分以内にアクセスがあれば緑のドット + "● Online"、それ以外は "Last seen 2h ago" を表示。プロフィール、受信箱、会話画面のアバター上に表示

#### バンド募集 (ホームフィード)
- `/home` に Instagram 風のアクティブな募集フィード
- 各カードに投稿者のアバター、表示名、タイトル、必要な楽器、所在地を表示
- 自分の募集を投稿 / 編集 / 締切り / 削除
- 楽器、ジャンル、国でフィルタリング
- 募集詳細ページからダイレクトメッセージで 1 タップ返信

#### 検索
- JPA Specifications による動的フィルター (合成可能、null 安全)
- 折りたたみアコーディオン内の複数選択チェックボックス
- 楽器、ジャンル、国、都市でフィルター
- 自分を結果から除外、ブロック関係も考慮 (双方向で見えなくなる)
- フィルター状態を URL に保存しブックマーク可能

#### メッセージング
- 1 対 1 の会話 (ユーザー ID で正規化、同じペアでスレッドが重複しない)
- 最新順の受信箱、未読バッジ、時:分のタイムスタンプ
- アバター + 表示名のヘッダーからプロフィールへ遷移
- 複数行のメッセージ入力
- 閲覧時に自動既読化、`readAt` タイムスタンプによる既読管理
- 会話の削除 (両者の履歴を消去)
- ブロック関係があれば送信が無効化され、バナー表示

#### アカウント管理
- プロフィールの公開・非公開を切り替え (設定画面から)
- アカウント論理削除: ログイン無効化 + 自分の募集を物理削除 + 過去のメッセージは相手側に残す
- 存在しない / 非公開 / ブロック中 / 退会済みプロフィール・募集に対する独自 404 ページ (すべての「見られない」ケースで同じ画面を返すことで情報漏えいを防止)

#### モデレーション
- **ユーザーブロック** (双方向): 検索、フィード、プロフィール、募集、メッセージから相互に見えなくなる
- 専用の `/profile/me/blocks` ページでブロック一覧の確認と解除
- **ユーザー通報**: 理由カテゴリ 5 種類 (スパム、嫌がらせ、不適切なコンテンツ、なりすまし、その他) + 任意の自由記述
- 管理者用キュー `/admin/reports` で Open / Reviewed / Dismissed タブを切替
- `ROLE_ADMIN` ユーザーにのみ管理画面リンクを表示
- ステータス変更ごとに監査ログ (レビュアーのメール + タイムスタンプ) を記録

### アーキテクチャ

Spring 標準のレイヤードアーキテクチャ、機能ごとのパッケージ構成:

```
co.sponto.musicrew
├── admin           — 管理者モデレーション (通報キュー)
├── block           — Block エンティティ、リポジトリ、サービス、コントローラー
├── config          — SecurityConfig (ロールベースアクセス)、WebConfig
├── listing         — Listing エンティティ + フィード + CRUD
├── messaging       — Conversation、Message、スレッド型受信箱
├── passwordreset   — パスワードリセットフロー (トークン発行・検証・消費)
├── profile         — Profile、Instrument、Genre、VideoLink、MusicLink
├── report          — Report エンティティ、理由・ステータス列挙型、コントローラー
├── search          — SearchService + ProfileSpecs (合成可能な JPA Specifications)
├── upload          — FileStorageService (ローカルディスク実装)
└── user            — User、AppUserDetailsService、AuthController、Role 列挙型
```

データモデル: 14 テーブル — `User`、`Profile`、`Instrument`、`Genre`、`VideoLink`、`MusicLink`、`Conversation`、`Message`、`Listing`、`Block`、`Report`、`PasswordResetToken` + M:N 中間テーブル。`User` と `Profile` の 1:1 関係により、認証関連の情報と公開プロフィールデータを分離しています。

**JPA Specifications** が検索とバンド募集の合成可能で null 安全なフィルターを支えています。ブロック、非公開プロフィール、無効化アカウントはクエリレベルで除外されるため、対象ユーザーは結果集合に含まれません。

**双方向ブロック** は方向性を持つ 1 行の `Block` レコードで表現し、可視性チェック (`isBlockedBetween(a, b)`) で `(a → b)` と `(b → a)` の両方を JPQL の OR で判定。データ構造をシンプルに保ったまま双方向セマンティクスを実現。

**パスワードリセット** ではトークンの平文を DB に保存しません。SHA-256 ハッシュのみを保存し、平文は URL に乗せて 1 回限り渡します。DB が漏えいしてもアカウント乗っ取りに使えません。

### ローカル実行

前提: Java 21、Maven 3.8+

```bash
git clone https://github.com/hikaruyonehara-dev/musicrew.git
cd musicrew
mvn spring-boot:run
```

`http://localhost:8080` で起動し、`./data/musicrew.mv.db` の H2 ファイル DB が使われます。H2 Web コンソールは `/h2-console`。シードデータ (楽器、ジャンル、管理者アカウント) は初回起動時に `src/main/resources/data.sql` から読み込まれます。

ローカル DB を初期化する場合: `rm -rf data/` 後に再起動。

**シード済み管理者アカウント:** `admin@test.com` / `Password`

### デプロイ

`main` ブランチがデプロイブランチ。`main` へのプッシュごとに `.github/workflows/deploy.yml` のワークフローが起動し、以下を実行します:

1. Maven で fat-JAR をビルド
2. SSH 経由で EC2 インスタンスへ SCP (`secrets.EC2_SSH_KEY`、`EC2_USER`、`EC2_HOST` を使用)
3. EC2 上で `musicrew` systemd サービスを再起動

本番では `prod` Spring プロファイル (`application-prod.properties`) が有効化されており、**EC2 上で自己ホスト** の PostgreSQL (`localhost:5432`) とローカルディスクストレージを使用。秘匿値 (DB パスワードなど) は systemd の `EnvironmentFile=/etc/musicrew/env` 経由で注入されます。

ライブ URL: [`https://musicrew.duckdns.org/`](https://musicrew.duckdns.org/)。**nginx** がリバースプロキシとして 443 番ポートで受けて Spring Boot (`localhost:8080`) に転送、**Let's Encrypt** の TLS 証明書を **certbot** で取得し、systemd タイマーで自動更新します。

---

## English

Musicrew lets musicians create a profile (instruments, genres, location, performance videos, music distribution links), browse an Instagram-style feed of active band-searches, discover others by filter, and chat in 1:1 conversations. Account management (hide / delete) and a moderator-led block / report system round out the social layer. Continuous deploy via GitHub Actions to a single Ubuntu EC2 instance.

Built from a formal Requirements → Design → Implementation → Test → Deploy cycle.

### Tech stack

| Layer | Choice |
|---|---|
| Language / runtime | Java 21 (LTS) |
| Framework | Spring Boot 3.3 |
| Persistence | Spring Data JPA + Hibernate, H2 (dev), PostgreSQL-ready (prod) |
| Security | Spring Security 6 — form login, BCrypt, CSRF, role-based access (`USER` / `ADMIN`) |
| Views | Thymeleaf with fragment-based layout |
| Styling | Tailwind CSS via Play CDN, Inter font, Resend-style dark theme |
| Build | Maven, fat-JAR via `spring-boot-maven-plugin` |
| Image processing | Thumbnailator (server-side resize to 1024×1024) |
| File storage | Local disk (`LocalDiskFileStorageService`) |
| Testing | JUnit 5, AssertJ, MockMvc, `@DataJpaTest`, `spring-security-test` |
| CI / CD | GitHub Actions — build + scp + `systemctl restart` on push to `main` |
| Deployment | EC2 (Ubuntu 24.04) + `systemd` + nginx reverse proxy + Let's Encrypt HTTPS |

### Features

#### Authentication
- Email + password signup with 18+ age gate
- BCrypt password hashing
- Session-based login, CSRF protection, secure POST logout
- Skill-level selection at signup (Beginner / Intermediate / Advanced / Pro)
- Role-based access (`USER` / `ADMIN`) granted by Spring Security authorities
- **Password reset**: cryptographically random 256-bit token, SHA-256-hashed in the DB, 1-hour expiry, one-time use

#### Profile
- Display name, bio, country (ISO 3166), city, skill badge
- Multi-select instruments and genres from curated lists
- Profile picture upload with server-side resize and content-type whitelisting
- Multiple performance video links (YouTube / Vimeo / SoundCloud whitelist)
- Multiple music distribution links (TuneCore / DistroKid / CDBaby / UnitedMasters)
- Hidden from public discovery until profile has picture + ≥ 1 performance video
- **Online presence** — green dot + "● Online" label when active in the last 5 min; "Last seen 2h ago" otherwise. Shown on the profile page, inbox rows, and conversation headers. Powered by a Spring `HandlerInterceptor` that updates `lastSeenAt` on every authenticated request

#### Listings (home feed)
- Instagram-style feed of active band-searches at `/home`
- Each card shows poster avatar, display name, title, instruments needed, location
- Post / edit / close / delete your own listings
- Filterable by instrument, genre, country
- Click-through to detail page with one-tap reply via direct message

#### Search
- Dynamic filters via JPA Specifications (composable, null-safe)
- Multi-select checkboxes in collapsible accordions
- Filter by instrument, genre, country, city
- Self-exclusion and block-aware (you and blocked users never see each other)
- Bookmarkable URL with filter state preserved

#### Messaging
- 1:1 conversations with canonical user ordering (no duplicate threads per pair)
- Inbox sorted by recency, unread badges, hour:minute timestamps
- Avatar + display name header, click-through to profile
- Multi-line message input
- Auto mark-as-read on view, read/unread tracking via `readAt` timestamp
- Delete conversation (clears history for both participants)
- Send disabled and banner shown when a block exists in either direction

#### Account management
- Toggle profile visibility (hide / unhide) — reversible from settings
- Soft-delete account: disables login, hard-deletes own listings, leaves prior messages visible to the other participant
- Custom 404 page (themed to match the rest of the app) for missing / hidden / blocked / deleted profiles and listings — same surface for all "not available" cases, no enumeration leaks

#### Moderation
- **Block users** (symmetric): each disappears from the other's search, feed, profile pages, listings, and messaging
- Dedicated `/profile/me/blocks` page to review and unblock
- **Report users** with five reason categories (Spam, Harassment, Inappropriate content, Impersonation, Other) and optional free-text description
- Admin queue at `/admin/reports` with Open / Reviewed / Dismissed tabs
- Admin nav link visible only to `ROLE_ADMIN` accounts
- Audit trail (reviewer email + timestamp) recorded on every status change

### Architecture

Standard Spring layered architecture, package-by-feature:

```
co.sponto.musicrew
├── admin           — admin moderation controller (reports queue)
├── block           — Block entity, repository, service, controller
├── config          — SecurityConfig (role-based access), WebConfig
├── listing         — Listing entity + feed + CRUD
├── messaging       — Conversation, Message, threaded inbox
├── passwordreset   — password reset flow (token issue / validate / consume)
├── profile         — Profile, Instrument, Genre, VideoLink, MusicLink
├── report          — Report entity, reason / status enums, controller
├── search          — SearchService + ProfileSpecs (composable JPA Specifications)
├── upload          — FileStorageService (local-disk implementation)
└── user            — User, AppUserDetailsService, AuthController, Role enum
```

Data model: 14 tables — `User`, `Profile`, `Instrument`, `Genre`, `VideoLink`, `MusicLink`, `Conversation`, `Message`, `Listing`, `Block`, `Report`, `PasswordResetToken` plus M:N join tables. The 1:1 between `User` and `Profile` keeps auth concerns separate from public profile data.

**JPA Specifications** power composable, null-safe filtering across search and listings — blocks, hidden profiles, and disabled accounts are enforced at the query level, so excluded users never make it into the result set.

**Symmetric blocking** is modeled as a single directed `Block` row whose visibility check (`isBlockedBetween(a, b)`) covers both `(a → b)` and `(b → a)` via a JPQL OR, simplifying the data shape while preserving symmetric semantics.

**Password reset** never stores the plaintext token. We persist only the SHA-256 hash and put the plaintext in the URL — a DB leak doesn't yield usable reset links.

### Running locally

Prereqs: Java 21, Maven 3.8+.

```bash
git clone https://github.com/hikaruyonehara-dev/musicrew.git
cd musicrew
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with an H2 file-based DB at `./data/musicrew.mv.db`. The H2 web console is available at `/h2-console`. Seed data (instruments, genres, admin account) is loaded from `src/main/resources/data.sql` on first run.

To wipe local data: `rm -rf data/` and restart.

**Default admin account (seeded):** `admin@test.com` / `Password`

### Deployment

`main` is the deploy branch. Every push to `main` triggers the workflow at `.github/workflows/deploy.yml`, which:

1. Builds the fat JAR with Maven
2. SCPs it to the EC2 instance over SSH (uses `secrets.EC2_SSH_KEY`, `EC2_USER`, `EC2_HOST`)
3. Restarts the `musicrew` `systemd` service on the box

Production runs with the `prod` Spring profile (`application-prod.properties`) active, backed by **self-hosted PostgreSQL** on the same EC2 (`localhost:5432`) and local-disk storage. Secrets (DB password, etc.) are injected via the systemd `EnvironmentFile=/etc/musicrew/env` (root-owned, mode 600).

Live at [`https://musicrew.duckdns.org/`](https://musicrew.duckdns.org/). **nginx** sits in front as a reverse proxy on port 443, forwarding to Spring Boot at `localhost:8080`. **Let's Encrypt** TLS certificates issued via **certbot**, auto-renewed by the certbot `systemd` timer.

---

## Built with / 使用ツール

Built with assistance from **[Claude Code](https://claude.com/claude-code)** — used throughout development for pair-programming, code review, and architectural guidance.

開発全体を通して **[Claude Code](https://claude.com/claude-code)** をペアプログラミング・コードレビュー・設計相談のツールとして使用しています。

---

## License

MIT — see [LICENSE](LICENSE).