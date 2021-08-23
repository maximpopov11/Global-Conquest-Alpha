package com.example.globalconquestfull

/**
 * Nation that a user controls to interact with the game.
 * @param name is the nation's name.
 * @param color is the nation's color for provinces.
 * @param textColor is the nation's color for text.
 * @param create is the individual game this nation is in.
 */
class Nation(val name: String, val color: Color, val textColor: Color, val create: Create?) {

    /**
     * ArrayList of combat units this nation controls.
     */
    val units = ArrayList<CombatUnit>()

    /**
     * ArrayList of provinces this nation controls.
     */
    val provinces = ArrayList<Province>()

    /**
     * Production this nation has.
     */
    var Production = 0

    /**
     * Production this nation gets per turn.
     */
    var productionPerTurn = 0

    /**
     * Gold this nation has.
     */
    var gold = 0

    /**
     * Gold this nation gets per turn.
     */
    var goldPerTurn = 0

    /**
     * Science this nation has.
     */
    var Science = 0

    /**
     * Science  this nation gets per turn.
     */
    var sciencePerTurn = 0

    /**
     * Nonexistant nation that controls all uncontrolled provinces.
     * Do not make any units or other entities controlled by this
     */
    private object noneNationHolder {
        val INSTANCE = Nation("none", Color.WHITE, Color.WHITE, null)
    }
    companion object {
        val noneNation: Nation by lazy {noneNationHolder.INSTANCE}
    }

    override fun toString(): String {
        return this.name
    }

    /**
     * Levels up city in given province if has enough production and city can be leveled up.
     * @param province is the province whose city is being upgraded.
     */
    fun upgradeCity(province: Province): Boolean {
        if (this.Production >= province.city.levelUpCost()) {
            this.Production -= province.city.levelUpCost()
            return province.city.upgrade()
        }
        else {
            return false
        }
    }

}