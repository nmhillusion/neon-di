package app.netlify.nmhillusion.ground_injection.mock.seeder;

import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;

/**
 * date: 2022-02-03
 * <p>
 * created-by: MINGUY1
 */

@GroundOut(priority = 9)
public class CocaColaSeeder implements DrinkSeeder {

    @Override
    public String drink() {
        return "CocaCola";
    }

}
