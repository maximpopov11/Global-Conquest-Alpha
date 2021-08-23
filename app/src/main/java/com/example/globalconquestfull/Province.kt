package com.example.globalconquestfull

import android.graphics.Point
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

/**
 * Province tile that the map is made out of.
 * @param fillAnchor is the point in the province used as the starting point for fills thereof.
 * @param allAnchors is an array of all of the fill anchors in this province.
 * @param armyFillAnchor is the point in this province's army used as the starting point for fills thereof.
 * @param armyAllAnchors is an array of all of the fill anchors in this province's army.
 * @param name is the name of the province.
 * @param map is the map the province is on.
 */
class Province (fillAnchor: Point, allAnchors: ArrayList<Point>, val armyFillAnchor: Point, val armyAllAnchors: ArrayList<Point>, val name: String, val map: Map) : VisualObject(fillAnchor, allAnchors) {

    /**
     * Array of adjacent provinces.
     */
    val adjacentProvinces = ArrayList<Province>()

    /**
     * Array of buildings in this province.
     */
    val buildings = ArrayList<Building>()

    /**
     * Controlling nation of this province.
     */
    var controllingNation = Nation.noneNation
        set(value) {
            field = value
            correctColor()
        }

    /**
     * City in this province.
     */
    val city = City(Cities.cityLevel0)

    /**
     * Combat in this province.
     */
    var combat: Combat? = null

    /**
     * Army in this province.
     */
    val army = Army(this.armyFillAnchor, this.armyAllAnchors, this)

    /**
     * @return province name.
     */
    override fun toString(): String {
        return name
    }

    /**
     * Colors the province with the color of the controlling nation.
     */
    private fun correctColor() {
        map.fill(fillAnchor.x, fillAnchor.y, controllingNation.color)
        //todo: other way to give time rather than thread.sleep? Something that lets invalidate happen in a second, but other things can occur in the meantime could work? There is a workInBackground function in Map, does that work?
        //If thread.sleep and invalidate are moved to turn when controlling nation is updated (with 1 second delay), the map does not update correctly.
        Thread.sleep(1000)
        map.invalidate()
    }

    /**
     * Colors the army of the province with the color of the nation contrlling the army.
     */
    fun correctArmyColor() {
        val firstUnit = army.units.firstOrNull() { !it.mustRetreat }
        val unitNation = if (firstUnit == null) Nation.noneNation else firstUnit.nation
        map.fill(army.fillAnchor.x, army.fillAnchor.y, unitNation.color)
        //If thread.sleep and invalidate are moved to turn when controlling nation is updated (with 1 second delay), the map does not update correctly.
        Thread.sleep(100)
        map.invalidate()
    }

    /**
     * @return the production yield of this province.
     */
    fun production(): Int {
        return this.city.city.level * 10
    }

    /**
     * @return the gold yield of this province.
     */
    fun gold(): Int {
        return this.city.city.level * 10
    }

    /**
     * @return the science yield of this province.
     */
    fun science(): Int {
        return this.city.city.level * 10
    }

    /**
     * @param otherProvince is the province being tested for adjacency to this one.
     * @return true if the given province is adjacent to this province.
     */
    fun adjacent(otherProvince: Province): Boolean {
        return adjacentProvinces.contains(otherProvince)
    }

    /**
     * Changes the controlling nation of the province.
     * @param newNation is the new controlling nation of this province
     */
    fun changeControllingNation (newNation: Nation){
        controllingNation.provinces.remove(this)
        controllingNation = newNation;
        newNation.provinces.add(this)
    }

    /**
     * @return true if there is a combat occuring in this province.
     */
    fun hasCombat (): Boolean {
        return army.units.filter { !it.mustRetreat }.map { it.nation }.distinct().size > 1
    }

    /**
     * Runs a combat in this province.
     */
    fun doCombatRound() {
        if (combat == null) {
            this.combat = Combat(this)
        }
        else {
            this.combat!!.nextRound()
        }
    }

    /**
     * Sets up the information view for this province.
     * @param context is the application environment (main activity) that needs to be known to add views to the UI.
     * @param selectedInforrmation is the layout in which to place the required information.
     */
    override fun view(context: MainActivity, selectedInforrmation: TableLayout) {
        val row = TableRow(context)
        val textView = TextView(context)
        textView.setText("this is a province                        ")
        textView.setTextColor(Color.BLACK.rgb)
        row.addView(textView)
        row.setBackgroundColor(Color.YELLOW.rgb)
        selectedInforrmation.addView(row)
    }

}