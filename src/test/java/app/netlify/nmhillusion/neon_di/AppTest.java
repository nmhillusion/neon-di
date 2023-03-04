package app.netlify.nmhillusion.neon_di;

import app.netlify.nmhillusion.neon_di.mock.controller.ConsumeController;
import app.netlify.nmhillusion.pi_logger.constant.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static app.netlify.nmhillusion.pi_logger.PiLoggerFactory.getDefaultLogConfig;
import static app.netlify.nmhillusion.pi_logger.PiLoggerFactory.getLog;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
    @BeforeAll
    static void init() {
        getDefaultLogConfig()
                .setColoring(true)
                .setDisplayLineNumber(true)
                .setLogLevel(LogLevel.INFO)
                .setIsOutputToFile(false)
                .setTimestampPattern("yyyy-MM-dd HH:mm:ss")
        ;
    }

    @Test
    public void testApp() {
        assertTrue(testDependency(), "Test Wiring Dependency");
    }

    private boolean testDependency() {
        try {
            final Map<String, Object> properties = new HashMap<>();
            properties.put("key1", "hello");
            properties.put("user.username", "nmhillusion");
            properties.put("user.fullName", "Nguyen Minh Hieu");
            properties.put("food.fruit", "Apple");
            properties.put("food.meal", "Cheese");
            properties.put("user.email", "nguyenminhhieu.geek@gmail.com");

            NeonEngine engine = new NeonEngine();
            engine
                    .putProperties(properties)
                    .run(this.getClass());

            getLog(this).info("start app by dependency --->");

            final Optional<ConsumeController> consumeController =
                    engine.findFirstNeonByClass(ConsumeController.class);
            consumeController.ifPresent(ConsumeController::execute);

            return true;
        } catch (Exception ex) {
            getLog(this).error(ex.getMessage(), ex);
            return false;
        }
    }
}