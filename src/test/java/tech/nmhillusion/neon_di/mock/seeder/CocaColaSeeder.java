package tech.nmhillusion.neon_di.mock.seeder;

import tech.nmhillusion.neon_di.annotation.Neon;

/**
 * date: 2022-02-03
 * <p>
 * created-by: nmhillusion
 */

@Neon(priority = 9)
public class CocaColaSeeder implements DrinkSeeder {

    @Override
    public String drink() {
        return "CocaCola";
    }

}
