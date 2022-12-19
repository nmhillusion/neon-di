package app.netlify.nmhillusion.pill_di.mock.service;

import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.mock.model.Cook;
import app.netlify.nmhillusion.pi_logger.PiLoggerHelper;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */

@Pillable(name = "foodService")
public class FoodService implements IService {

    @Inject(propertyKey = "food.fruit")
    private String fruit;

    @Inject(propertyKey = "food.meal")
    private String meal;

    @Inject(pillNames = "MrGinCook")
    private Cook ginCook;

    @Inject(pillNames = "linzCook")
    private Cook linzCook;

    @Inject
    private DrinkService drinkService;

    @Override
    public String service() {
        PiLoggerHelper.getLog(this).info("Cooking gin: " + ginCook);
        PiLoggerHelper.getLog(this).info("Cooking linz: " + linzCook);
        return "service by food (fruit: " + fruit + "; meal: " + meal + "); then thirsty so " + drinkService.service();
    }
}
