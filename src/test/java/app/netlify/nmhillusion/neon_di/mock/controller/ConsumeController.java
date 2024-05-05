package app.netlify.nmhillusion.neon_di.mock.controller;

import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.mock.service.DrinkService;
import app.netlify.nmhillusion.neon_di.mock.service.FoodService;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * date: 2022-02-01
 * <p>
 * created-by: nmhillusion
 */

@Neon
public class ConsumeController {
    @Inject(referenceClasses = FoodService.class)
    private FoodService foodService;

    @Inject
    private DrinkService drinkService;

    public void execute() {
        getLogger(this).info("Food Service --> " + foodService.service());
        getLogger(this).info("Drink Service --> " + drinkService.service());
    }
}
