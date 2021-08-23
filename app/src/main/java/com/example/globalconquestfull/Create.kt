package com.example.globalconquestfull

import android.graphics.Point

/**
 * Prepares the individual objects for the game.
 * Only 1 such object per game.
 * @param activity is the application environment (main activity) that needs to be known to add views to the UI.
 * @param map is the map.
 */
class Create(val activity: MainActivity, val map: Map) {

    /**
     * Array of all units in the game.
     */
    val allUnits get() = nation1Red.units.union(nation2Blue.units).union(nation3Green.units).toTypedArray()

    /**
     * Provinces and armies.
     */
    val province1 = Province (Point(500, 500), arrayListOf(Point(500, 500)), Point(623, 648), arrayListOf(Point(623, 648)), "Province1", map)
    val province2 = Province (Point(600, 900), arrayListOf(Point(600, 900)), Point(780, 1000), arrayListOf(Point(780, 1000)),"Province2", map)
    val province3 = Province (Point(1100, 1100), arrayListOf(Point(1100, 1100)), Point(1350, 1100), arrayListOf(Point(1350, 1100)), "Province3", map)
    val province4 = Province (Point(1000, 900), arrayListOf(Point(1000, 900)), Point(1260, 660), arrayListOf(Point(1260, 660)), "Province4", map)
    val province5 = Province (Point(1400, 300), arrayListOf(Point(1400, 300)), Point(1680, 310), arrayListOf(Point(1680, 310)),"Province5", map)

    /**
     * Array of all provinces in the game.
     */
    val allProvinces = arrayListOf(province1, province2, province3, province4, province5)

    /**
     * Array of all visual objects in the game.
     */
    val allVisualObjects = ArrayList<VisualObject>()

    /**
     * Nations in the game.
     */
    val nation1Red = Nation ("nation1(red)", Color.RED, Color.WHITE, this)
    val nation2Blue = Nation ("nation2(blue)", Color.BLUE, Color.WHITE, this)
    val nation3Green = Nation ("nation3(green)", Color.GREEN, Color.WHITE, this)

    /**
     * Array of all nations in the game.
     */
    val allNations = arrayListOf(nation1Red, nation2Blue, nation3Green)

    /**
     * class instance that creates the user interface.
     */
    val createUI = CreateUI(activity, this, map)

    /**
     * Turn object for the game.
     */
    val turn = Turn(this)

    /**
     * Initializes all variables.
     */
    init {
        //Sets up array list of allVisualObjects.
        this.allProvinces.forEach {
            allVisualObjects.add(it)
            allVisualObjects.add(it.army)
        }

        //Sets up province adjacency.
        province1.adjacentProvinces.addAll(arrayListOf(province2, province4))
        province2.adjacentProvinces.addAll(arrayListOf(province1, province3, province4))
        province3.adjacentProvinces.addAll(arrayListOf(province2, province4))
        province4.adjacentProvinces.addAll(arrayListOf(province1, province2, province3, province5))
        province5.adjacentProvinces.addAll(arrayListOf(province4))

        //Sets up combat units.
        val redClubman = CombatUnit (UnitClass.Land, UnitSubClass.Infantry, UnitType.Clubman, nation1Red, 10, 1, 0, province1)
        val redArcher = CombatUnit (UnitClass.Land, UnitSubClass.Infantry, UnitType.Archer, nation1Red, 8, 2, 2, province1)
        val blueClubman = CombatUnit (UnitClass.Land, UnitSubClass.Infantry, UnitType.Clubman, nation2Blue, 10, 1, 0, province3)
        val greenClubman = CombatUnit (UnitClass.Land, UnitSubClass.Infantry, UnitType.Clubman, nation3Green, 10, 1, 0, province2)

        //Sets up nations
        province1.changeControllingNation(nation1Red)
        province2.changeControllingNation((nation3Green))
        province3.changeControllingNation(nation2Blue)

        //Sets up for first phase (orders phase).
        turn.phaseCheck()
    }

}