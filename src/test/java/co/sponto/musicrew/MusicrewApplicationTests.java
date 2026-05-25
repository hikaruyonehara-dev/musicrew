package co.sponto.musicrew;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — confirms the full Spring application context can be loaded
 * with the default profile. If any bean fails to wire (missing config,
 * circular dependency, etc.), this test fails fast at build time before
 * the JAR is ever deployed.
 */
@SpringBootTest
class MusicrewApplicationTests {

    @Test
    void contextLoads() {
    }
}
