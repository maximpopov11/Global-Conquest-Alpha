package com.example.globalconquestfull

/**
 * City information by level.
 * todo: is it okay to have an enum class in the city class file?
 */
enum class Cities(val level: Int, val productionCost: Int, val strength: Int, val range: Int) {
    cityLevel0(0, 0, 0, 0),
    cityLevel1(1, 10, 10, 2),
    cityLevel2(2, 50, 50, 3),
    cityLevel3(3, 200, 100, 4),
    cityLevel4(4, 500, 200, 5),
    cityLevel5(5, 1000, 500, 6);
}

/**
 * City that exists in each province.
 * @param level is the city's level, between 0 and 5, determining its effects.
 */
//todo: is it okay to leave todo's around (for future changes) when releasing my first version? Meaning that the source code will have todo's in it. Will other be able to see it?
class City (var city: Cities) {

    fun levelUpCost(): Int {
        //todo: how to get the next level enum?
        //todo: if already level 5 return -1
        return this.city.next.productionCost
    }

    /**
     * Increases the level of this city by one if it is less than level 5.
     * @return true if the level up was legal and was thus completed.
     */
    fun upgrade(): Boolean {
        if (city.level < 5) {
            //todo: how to get the next level enum?
            this.city = this.city.next
            return true
        }
        else {
            return false
        }
    }

}