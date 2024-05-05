package app.netlify.nmhillusion.neon_di;

import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.mock.controller.ConsumeController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.nmhillusion.pi_logger.constant.LogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;
import static tech.nmhillusion.pi_logger.PiLoggerFactory.getDefaultLogConfig;

class AppTest {
    private static NeonEngine engine;

    @BeforeAll
    static void init() throws NeonException {
        getDefaultLogConfig()
                .setColoring(true)
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

        getLogger(AppTest.class).info("start app by dependency --->");
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
            getLogger(this).error(ex);
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
            getLogger(this).error(ex);
            return false;
        }
    }
}