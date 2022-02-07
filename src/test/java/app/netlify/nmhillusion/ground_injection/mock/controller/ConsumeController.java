package app.netlify.nmhillusion.ground_injection.mock.controller;

import app.netlify.nmhillusion.ground_injection.annotation.GroundFactory;
import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.mock.service.DrinkService;
import app.netlify.nmhillusion.ground_injection.mock.service.FoodService;
import app.netlify.nmhillusion.ground_injection.util.log.LogHelper;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */

@GroundOut
public class ConsumeController {
    @GroundIn(referenceClasses = FoodService.class)
    private FoodService foodService;

    @GroundIn
    private DrinkService drinkService;

    public void execute() {
        LogHelper.getLog(this).info("Food Service --> " + foodService.service());
        LogHelper.getLog(this).info("Drink Service --> " + drinkService.service());
    }
}
