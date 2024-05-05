package app.netlify.nmhillusion.neon_di.mock.service;

import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.mock.model.Cook;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;


/**
 * date: 2022-02-01
 * <p>
 * created-by: nmhillusion
 */

@Neon(name = "foodService")
public class FoodService implements IService {

    @Inject(propertyKey = "food.fruit")
    private String fruit;

    @Inject(propertyKey = "food.meal")
    private String meal;

    @Inject(names = "MrGinCook")
    private Cook ginCook;

    @Inject(names = "linzCook")
    private Cook linzCook;

    @Inject
    private DrinkService drinkService;

    @Override
    public String service() {
        getLogger(this).info("Cooking gin: " + ginCook);
        getLogger(this).info("Cooking linz: " + linzCook);
        return "service by food (fruit: " + fruit + "; meal: " + meal + "); then thirsty so " + drinkService.service();
    }
}
