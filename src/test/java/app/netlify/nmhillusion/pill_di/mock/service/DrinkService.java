package app.netlify.nmhillusion.pill_di.mock.service;

import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.mock.seeder.CocaColaSeeder;
import app.netlify.nmhillusion.pill_di.mock.seeder.DrinkSeeder;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */
@Pillable(name = "drinkService")
public class DrinkService implements IService {

    private final String drinkType;

    public DrinkService(@Inject(
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
