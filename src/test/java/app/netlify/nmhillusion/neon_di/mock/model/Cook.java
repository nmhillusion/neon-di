package app.netlify.nmhillusion.neon_di.mock.model;

import java.io.Serializable;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
 */

public class Cook implements Serializable {
    private String name;
    private int age;
    private String meal;

    public String getName() {
        return name;
    }

    public Cook setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Cook setAge(int age) {
        this.age = age;
        return this;
    }

    public String getMeal() {
        return meal;
    }

    public Cook setMeal(String meal) {
        this.meal = meal;
        return this;
    }

    @Override
    public String toString() {
        return "Cook{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", meal='" + meal + '\'' +
                '}';
    }
}
