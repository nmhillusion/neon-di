package app.netlify.nmhillusion.neon_di.mock.seeder;

import app.netlify.nmhillusion.neon_di.annotation.Neon;

/**
 * date: 2022-02-03
 * <p>
 * created-by: MINGUY1
 */

@Neon(priority = 9, name = "mePepsi")
public class PepsiSeeder implements DrinkSeeder {
    @Override
    public String drink() {
        return "Pepsi";
    }
}
