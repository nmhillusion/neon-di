package app.netlify.nmhillusion.neon_di.mock.controller;

import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.mock.service.DrinkService;
import app.netlify.nmhillusion.neon_di.mock.service.FoodService;

import static app.netlify.nmhillusion.n2mix.helper.log.LogHelper.getLog;

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
        getLog(this).info("Food Service --> " + foodService.service());
        getLog(this).info("Drink Service --> " + drinkService.service());
    }
}
