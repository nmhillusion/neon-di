package app.netlify.nmhillusion.pill_di.mock.controller;

import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.mock.service.DrinkService;
import app.netlify.nmhillusion.pill_di.mock.service.FoodService;
import app.netlify.nmhillusion.pi_logger.PiLoggerHelper;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */

@Pillable
public class ConsumeController {
    @Inject(referenceClasses = FoodService.class)
    private FoodService foodService;

    @Inject
    private DrinkService drinkService;

    public void execute() {
        PiLoggerHelper.getLog(this).info("Food Service --> " + foodService.service());
        PiLoggerHelper.getLog(this).info("Drink Service --> " + drinkService.service());
    }
}
