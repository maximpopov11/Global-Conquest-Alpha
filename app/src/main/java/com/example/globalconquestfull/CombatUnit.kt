package com.example.globalconquestfull

import android.app.AlertDialog

/**
 * Unit that is controlled by a nation and used to conquer provinces and partake in combats.
 * @param unitClass is the unit's class.
 * @param unitSubClass is the unit's subclass.
 * @param unitType is the unit type.
 * @param nation is the unit's controlling nation.
 * @param strength is the unit's strength value which determines its damage dealt in combat.
 * @param speed is the unit's speed value which determines how far the unit can move.
 * @param province is the unit's current province.
 */
class CombatUnit (val unitClass: UnitClass, val unitSubClass: UnitSubClass, val unitType: UnitType, val nation: Nation, val strength: Int, val speed: Int
                  , val range: Int, var province: Province) {

    /**
     * Health value of the unit. When it reaches 0 the unit is disbanded.
     */
    var health = 100
        private set(value) {
            field = if (value <= 100) value else 100
        }

    /**
     * ArrayList of provinces that the unit is ordered to move to in order.
     */
    var orderedDestination = arrayOfNulls<Province>(speed)

    /**
     * Province that the unit was in at the start of the turn.
     */
    var provinceAtStartOfTurn = province

    /**
     * Last province that this unit was in.
     */
    var lastProvince = province

    /**
     * The round of the combat phase in which this unit entered its current province.
     */
    var combatMoveRoundWhenEnteredProvince = 0

    /**
     * True if this unit is currently in combat.
     */
    var inCombat = false

    /**
     * True if the unit must retreat during the retreat phase and false otherwise.
     * TODO: (POST-ALPHA) (Former Todo) make this a property to change the size of orderedDestination to 1 (for retreats) until builds
     */
    var mustRetreat = false
        set(value) {
            field = value
            if (value) {
                orderedDestination = arrayOfNulls<Province>(1)
            }
            else {
                orderedDestination = arrayOfNulls<Province>(this.speed)
                this.hasTakenRetreatDamage = false
            }
        }

    /**
     * True if this unit has taken one round of damage after having to retreat in combat.
     */
    var hasTakenRetreatDamage = false
        set(value) {
            field = value
            if (value) {
                this.inCombat = false
            }
        }

    /**
     * True if all orders are given and false if not all orders are given.
     */
    var ordersAreFull = false
        get() {
            return orderedDestination.last() != null
        }

    /**
     * Adds the unit to the lists of units in it's province and in it's nation.
     * Opposite of disband() function.
     */
    init {
        province.army.units.add(this)
        nation.units.add(this)
    }

    /**
     * @return the unit's strength and health in a readable format.
     */
    override fun toString(): String {
        //todo: add speed and range here or no? It will take up more space in the army view, but it will be useful to know.
        return unitType.toString() + " " + this.nation.name + " " + this.strength + " " + this.health
    }

    /**
     * Disbands the unit, removing it from the lists of units in it's province and nation.
     * Opposite of init() function.
     */
    fun disband() {
        province.army.units.remove(this)
        nation.units.remove(this)
        //todo: When this is removed from all arrays it is deleted from memory, correct? If so, test that it is correctly removed from everything and thus deleted.
    }

    /**
     * @param heal is the amount that this unit heals.
     */
    fun heal (heal: Int) {
        this.health += heal
    }

    /**
     * @param damage is the amount of damage this unit takes.
     * @return true if the unit is disbanded due to going to 0 health.
     */
    fun takeDamage(damage: Int): Boolean {
        this.health -= damage
        if (this.health <= 0) {
            this.disband()
            return true
        }
        else {
            return false
        }
    }

    /**
     * @return true if this unit can retreat.
     */
    fun canRetreat(): Boolean {
        this.province.adjacentProvinces.forEach {destination ->
            if (!(this.province.army.units.any { it.nation != this.nation
                        && !it.mustRetreat
                        && it.lastProvince == destination
                        && (this.lastProvince != destination || it.combatMoveRoundWhenEnteredProvince > this.combatMoveRoundWhenEnteredProvince) })) {
                return true
            }
        }
        return false
    }

    /**
     * Gives the unit an order to move to a province after the previous ordered destination and gives a notification if the order is illegal.
     * @param destination is the given province this unit is ordered to move to next.
     * @param phase is the current phase of the turn.
     */
    fun orderMove (destination: Province, phase: Phase) {
        val result = move(destination, phase)
        //If order is illegal give notification.
        if (result != OrderMoveResult.SUCCESS) {
            val dialogBuilder = AlertDialog.Builder(this.nation.create!!.activity)
            dialogBuilder.setMessage(result.text)
            val alert = dialogBuilder.create()
            alert.setTitle("Unit Movement Failed")
            alert.show()
        }
    }

    /**
     * Gives the unit an order to move to a province after the previous ordered destination.
     * @param destination is the given province this unit is ordered to move to next.
     * @param phase is the current phase of the turn.
     * @return result of the order (failed if illegally given) and corresponding text.
     */
    private fun move(destination: Province, phase: Phase): OrderMoveResult {
        //Last index that has an order.
        val lastNonNullIndex = orderedDestination.indexOfLast { it != null }
        var lastOrderedProvince = if (lastNonNullIndex != -1) this.orderedDestination[lastNonNullIndex]!! else this.province
        //If cannot give orders to this unit in this phase (not command phase and mustRetreat is false).
        if (phase != Phase.COMMAND && !this.mustRetreat) {
            //TODO: (POST-ALPHA) (Former Todo) change this to alert that orders are full once orders can be reset
            return OrderMoveResult.CANNOTGIVEORDERS
        }
        //If orders are already full.
        else if (this.orderedDestination.lastOrNull() != null) {
            return OrderMoveResult.ORDERSFULL
        }
        else {
            //If province not adjacent to latest order.
            if (!destination.adjacent(lastOrderedProvince)) {
                return OrderMoveResult.NOTADJACENT
            }
            //If advance retreating (There is a non-retreating unit from another nation that came from destination after this unit did).
            else if (this.province.army.units.any { it.nation != this.nation
                        && !it.mustRetreat
                        && it.lastProvince == destination
                        && (this.lastProvince != destination || it.combatMoveRoundWhenEnteredProvince > this.combatMoveRoundWhenEnteredProvince) }) {
                return OrderMoveResult.AVANCERETREAT
            }
            //Order is legal.
            else {
                //At first null index, which exists because otherwise orders would be full and that is dealt with above.
                orderedDestination[lastNonNullIndex + 1] = destination
                if (this.nation.create!!.createUI.ordersShown) {
                    //Orders are hidden first so that the correct map is saved.
                    this.nation.create.createUI.hideOrders()
                    this.nation.create.createUI.showOrders()
                }
                return OrderMoveResult.SUCCESS
            }
        }
    }


    /**
     * Does the next movement of the orders.
     * @param combatRound is the number of rounds of movement/combat that has passed since the start of the combat or combat post retreats phase.
     */
    fun moveNext(combatRound: Int) {
        if (orderedDestination.size > combatRound && orderedDestination[combatRound] != null){
            lastProvince = this.province
            province.army.units.remove(this)
            province = orderedDestination[combatRound]!!
            orderedDestination[combatRound] = null
            province.army.units.add(this)
            this.combatMoveRoundWhenEnteredProvince = combatRound
        }
    }

    /**
     * @return true if not all orders have been processed and carried out.
     */
    fun ordersNeedProcessing(): Boolean {
        return orderedDestination.any { it != null }
    }

    /**
     * Clears all orders.
     */
    fun clearOrderedMoves() {
        this.nation.create!!.createUI.hideOrders()
        orderedDestination.fill(null)
        this.nation.create!!.createUI.showOrders()
    }

}