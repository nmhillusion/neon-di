package app.netlify.nmhillusion.ground_injection.mock.factory;

import app.netlify.nmhillusion.ground_injection.annotation.GroundFactory;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.mock.model.Cook;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
 */
@GroundFactory
public class CookFactory {

    @GroundOut(name = "MrBenCook")
    public Cook benCook() {
        return new Cook()
                .setAge(25)
                .setMeal("Pizza")
                .setName("Ben");
    }

    @GroundOut
    public Cook linzCook() {
        return new Cook()
                .setAge(28)
                .setMeal("Cheese")
                .setName("Linz");
    }

    @GroundOut(name = "MrGinCook")
    public Cook ginCook() {
        return new Cook()
                .setAge(35)
                .setMeal("Rice")
                .setName("Gin");
    }

    public Cook hoodieCook() {
        return new Cook()
                .setAge(30)
                .setMeal("Noodles")
                .setName("Hoodie");
    }

}
