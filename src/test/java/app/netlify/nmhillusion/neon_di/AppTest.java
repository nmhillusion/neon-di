package app.netlify.nmhillusion.neon_di;

import app.netlify.nmhillusion.neon_di.exception.NeonException;
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
    private static NeonEngine engine;

    @BeforeAll
    static void init() throws NeonException {
        getDefaultLogConfig()
                .setColoring(true)
                .setDisplayLineNumber(true)
                .setLogLevel(LogLevel.INFO)
                .setIsOutputToFile(false)
                .setTimestampPattern("yyyy-MM-dd HH:mm:ss")
        ;

        final Map<String, Object> properties = new HashMap<>();
        properties.put("key1", "hello");
        properties.put("user.username", "nmhillusion");
        properties.put("user.fullName", "Nguyen Minh Hieu");
        properties.put("food.fruit", "Apple");
        properties.put("food.meal", "Cheese");
        properties.put("user.email", "nguyenminhhieu.geek@gmail.com");

        engine = new NeonEngine();
        engine
                .putProperties(properties)
                .run(AppTest.class);

        getLog(AppTest.class).info("start app by dependency --->");
    }

    @Test
    public void testApp() {
        assertTrue(testDependency(), "Test Wiring Dependency");
    }

    private boolean testDependency() {
        try {
            final Optional<ConsumeController> consumeController =
                    engine.findFirstNeonByClass(ConsumeController.class);
            consumeController.ifPresent(ConsumeController::execute);

            return true;
        } catch (Exception ex) {
            getLog(this).error(ex.getMessage(), ex);
            return false;
        }
    }

    @Test
    public void testAppAndMakeSureExistNeon() {
        assertTrue(testDependencyAndMakeSureExistNeon(), "Test Wiring Dependency And Make Sure Exist Neon");
    }

    private boolean testDependencyAndMakeSureExistNeon() {
        try {
            final ConsumeController consumeController =
                    engine.makeSureObtainNeon(ConsumeController.class);
            consumeController.execute();

            return true;
        } catch (Exception ex) {
            getLog(this).error(ex.getMessage(), ex);
            return false;
        }
    }
}