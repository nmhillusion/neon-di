package app.netlify.nmhillusion.pill_di.mock.factory;

import app.netlify.nmhillusion.pill_di.annotation.PillFactory;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.mock.model.Cook;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
 */
@PillFactory
public class CookFactory {

    @Pillable(name = "MrBenCook")
    public Cook benCook() {
        return new Cook()
                .setAge(25)
                .setMeal("Pizza")
                .setName("Ben");
    }

    @Pillable
    public Cook linzCook() {
        return new Cook()
                .setAge(28)
                .setMeal("Cheese")
                .setName("Linz");
    }

    @Pillable(name = "MrGinCook")
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
