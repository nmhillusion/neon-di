package tech.nmhillusion.neon_di.mock.service;

import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;
import tech.nmhillusion.neon_di.mock.seeder.CocaColaSeeder;
import tech.nmhillusion.neon_di.mock.seeder.DrinkSeeder;

/**
 * date: 2022-02-01
 * <p>
 * created-by: nmhillusion
 */
@Neon(name = "drinkService")
public class DrinkService implements IService {

	private final String drinkType;

	public DrinkService(@Inject(
			referenceClasses = CocaColaSeeder.class
//			, names = "mePepsi"
	) DrinkSeeder drinkSeeder) {
		drinkType = drinkSeeder.drink();
	}

	@Override
	public String service() {
		return "service by drink of " + drinkType;
	}
}
