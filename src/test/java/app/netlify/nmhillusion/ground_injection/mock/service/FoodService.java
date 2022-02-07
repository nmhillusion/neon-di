package app.netlify.nmhillusion.ground_injection.mock.service;

import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.mock.model.Cook;
import app.netlify.nmhillusion.ground_injection.util.log.LogHelper;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */

@GroundOut(name = "foodService")
public class FoodService implements IService {

    @GroundIn(propertyKey = "food.fruit")
    private String fruit;

    @GroundIn(propertyKey = "food.meal")
    private String meal;

    @GroundIn(groundNames = "MrGinCook")
    private Cook ginCook;

    @GroundIn(groundNames = "linzCook")
    private Cook linzCook;

    @GroundIn
    private DrinkService drinkService;

    @Override
    public String service() {
        LogHelper.getLog(this).info("Cooking gin: " + ginCook);
        LogHelper.getLog(this).info("Cooking linz: " + linzCook);
        return "service by food (fruit: " + fruit + "; meal: " + meal + "); then thirsty so " + drinkService.service();
    }
}
