package app.netlify.nmhillusion.ground_injection;

import app.netlify.nmhillusion.ground_injection.mock.controller.ConsumeController;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
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

            GroundEngine engine = new GroundEngine();
            engine
                    .putProperties(properties)
                    .run(this.getClass());

            final Optional<ConsumeController> consumeController =
                    engine.findFirstGroundByClass(ConsumeController.class);
            consumeController.ifPresent(ConsumeController::execute);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}