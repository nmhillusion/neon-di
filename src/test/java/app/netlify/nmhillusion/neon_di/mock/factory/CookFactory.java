package app.netlify.nmhillusion.neon_di.mock.factory;

import app.netlify.nmhillusion.neon_di.annotation.NeonFactory;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.mock.model.Cook;

/**
 * date: 2022-02-04
 * <p>
 * created-by: nmhillusion
 */
@NeonFactory
public class CookFactory {

    @Neon(name = "MrBenCook")
    public Cook benCook() {
        return new Cook()
                .setAge(25)
                .setMeal("Pizza")
                .setName("Ben");
    }

    @Neon
    public Cook linzCook() {
        return new Cook()
                .setAge(28)
                .setMeal("Cheese")
                .setName("Linz");
    }

    @Neon(name = "MrGinCook")
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
