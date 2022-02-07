package app.netlify.nmhillusion.ground_injection.mock.service;

import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.mock.seeder.CocaColaSeeder;
import app.netlify.nmhillusion.ground_injection.mock.seeder.DrinkSeeder;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */
@GroundOut(name = "drinkService")
public class DrinkService implements IService {

    private final String drinkType;

    public DrinkService(@GroundIn(
            referenceClasses = CocaColaSeeder.class
//            ,groundNames = "mePepsi"
    ) DrinkSeeder drinkSeeder) {
        drinkType = drinkSeeder.drink();
    }

    @Override
    public String service() {
        return "service by drink of " + drinkType;
    }
}
