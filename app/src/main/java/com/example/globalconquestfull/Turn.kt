package com.example.globalconquestfull

/**
 * Turn that keeps track of phases and the number of cycles of phases that has passed.
 * Only 1 such object per game.
 * @param create is is the object creating the game functions.
 */
class Turn (val create: Create) {

    /**
     * ArrayList of phases in order.
     */
    val phases = arrayListOf(Phase.COMMAND, Phase.COMBAT, Phase.RETREAT, Phase.COMBATPOSTRETREAT, Phase.BUILD)

    /**
     * Position whithin order of phases.
     */
    var phaseOrderPosition = 0

    /**
     * Current phase.
     */
    val phase get() = phases[phaseOrderPosition]

    /**
     * Current turn: the number of times all phases have occured, starting at 1.
     */
    var turn = 1

    //todo: (POST-ALPHA) maybe create phase class? Phases have a fair bit of similarity in them so that may be useful. Later, when phases are on a timer and other things are added, thats even more shared information.

    /**
     * Command phase.
     */
    val command get() = phase == Phase.COMMAND

    /**
     * Combat phase.
     */
    val combat get () = phase == Phase.COMBAT

    /**
     * Retreats phase.
     */
    val retreats get() = phase == Phase.RETREAT

    /**
     * Combat post retreats phase.
     */
    val combatPostRetreats get() = phase == Phase.COMBATPOSTRETREAT

    /**
     * Builds phase.
     */
    val builds get() = phase == Phase.BUILD

    /**
     * Goes to the next phase in order.
     */
    fun nextPhase() {
        phaseOrderPosition++
        if (phaseOrderPosition > phases.size - 1) {
            phaseOrderPosition = 0
            turn++
        }
        phaseCheck()
    }

    /**
     * Goes to a specific phase.
     * todo: (POST-ALPHA) this isn't for testing, rather for when phases are not needed. Could this be combined with the normal next phase and that checks if the order is kept or skipped retreats or combat? If this is used anywhere for testing, then it would be used only for testing at that point.
     * @param newPhase is the phase to go to.
     */
    private fun goToPhase(newPhase: Phase) {
        val newPhaseIndex = phases.indexOf(newPhase)
        if (newPhaseIndex == phaseOrderPosition) {
            throw IllegalArgumentException("Cannot Jump To Current Phase: " + phase)
        }
        if (newPhaseIndex < phaseOrderPosition) {
            turn++
        }
        phaseOrderPosition = newPhaseIndex
        phaseCheck()
    }

    /**
     * Calls the correct phase to do the required actions at the start of the phase.
     * todo: (POST-ALPHA) with a seperate phase class, could this be unnecessary?
     */
    fun phaseCheck() {
        //todo: make state machine as a graph?
        if (command) {
            commandPhase()
        }
        else if (combat) {
            combatPhase()
        }
        else if (retreats) {
            retreatPhase()
        }
        else if (combatPostRetreats) {
            combatPostRetreatPhase()
        }
        else if (builds) {
            buildPhase()
        }
    }

    /**
     * Prepares for the build phase.
     */
    private fun buildPhase() {
        create.allUnits.forEach { it.lastProvince = it.province }
        this.create.allUnits.forEach {
            it.heal(10)
        }
    }

    /**
     * Does the combatPostRetreat phase.
     * No player actions occur during this phase, so when this is completed the next phase begins.
     */
    private fun combatPostRetreatPhase() {
        val ordersShown = this.create.createUI.ordersShown
        create.createUI.hideOrders()
        doCombat()
        create.allUnits.filter { it.mustRetreat }.forEach{it.disband()}
        postCombatUpdate()
        if (ordersShown) {
            this.create.createUI.showOrders()
        }
        nextPhase()
    }

    /**
     * Prepares for the retreat phase.
     */
    private fun retreatPhase() {
        if (create.allUnits.filter { it.mustRetreat }.none()) {
            goToPhase(Phase.BUILD)
        }
    }

    /**
     * Does the combat phase.
     * No player actions occur during this phase, so when this is completed the next phase begins.
     */
    private fun combatPhase() {
        val ordersShown = this.create.createUI.ordersShown
        create.createUI.hideOrders()
        doCombat()
        postCombatUpdate()
        if (ordersShown) {
            this.create.createUI.showOrders()
        }
        nextPhase()
    }

    /**
     * Updates the UI and updates everything that combat is over.
     */
    private fun postCombatUpdate() {
        updateControllingNations()
        create.allProvinces.forEach {it.correctArmyColor()}
        create.allProvinces.forEach { it.combat = null }
        create.allUnits.forEach { it.inCombat = false }
    }

    /**
     * Prepares for the command phase.
     */
    private fun commandPhase() {
        create.allUnits.forEach { it.provinceAtStartOfTurn = it.province }
        create.allProvinces.forEach {it.correctArmyColor()}
    }

    /**
     * Updates the controlling nation for all provinces.
     */
    private fun updateControllingNations() {
        create.allProvinces.filter { it.army.units.filter { !it.mustRetreat }.any() }.forEach{
            it.controllingNation = it.army.units.filter { !it.mustRetreat }.first().nation
        }
    }

    /**
     * Does combat in all appropriate provinces.
     */
    private fun doCombat() {
        var combatRound = 0;
        while(anyUnitsNeedToMove() || anyProvinceHasCombat()) {
            //ArrayList of all units who will move this round.
            val movingUnits = this.create.allUnits.filter { combatRound < it.orderedDestination.size && it.orderedDestination[combatRound] != null}
            //ArrayList of pairs of current province and next moving to province for each unit.
            val provincePairs = ArrayList<Pair<Province, Province>>()
            //Populates provincePairs.
            for (unit in movingUnits) {
                provincePairs.add(Pair<Province, Province>(unit.province, unit.orderedDestination[combatRound]!!))
                provincePairs.distinct()
            }
            //ArrayList of pairs of provinces that has at least 1 unit in each moving to the other.
            var symmetricMatches = ArrayList<Pair<Province, Province>>()
            for (i in 0..provincePairs.size - 1) {
                for (j in i+1..provincePairs.size - 1) {
                    if (provincePairs[i].first.equals(provincePairs[j].second) && provincePairs[j].first.equals(provincePairs[i].second)) {
                        symmetricMatches.add(Pair<Province, Province>(provincePairs[i].first, provincePairs[i].second))
                    }
                }
            }
            for (pair in symmetricMatches) {
                val units1 = ArrayList<CombatUnit>()
                for (unit in pair.first.army.units.filter { it.orderedDestination[combatRound]!!.equals(pair.second) }) {
                    units1.add(unit)
                }
                val units2 = ArrayList<CombatUnit>()
                for (unit in pair.second.army.units.filter { it.orderedDestination[combatRound]!!.equals(pair.first) }) {
                    units2.add(unit)
                }
                for (unit in slowerArmy(units1, units2)) {
                    unit.clearOrderedMoves()
                }
            }
            doCombatMove(combatRound)
            if (combatPostRetreats) {
                this.create.allUnits.forEach { it.mustRetreat = false }
            }
            //Sets controlling nation for provinces without combat
            this.create.allUnits.forEach {
                if (!it.province.hasCombat()) {
                    it.province.controllingNation = it.nation
                }
            }
            doCombatRound(combatRound)
            combatRound++
        }
    }

    /**
     * Determines which group of units, out of two groups moving into one another's current province, does not end up moving.
     * @param units1 is an ArrayList of units moving from province A to province B
     * @param units2 is an ArrayList of unit moving from province B to province A
     * @return the group of units that does not end up moving.
     */
    private fun slowerArmy(units1: ArrayList<CombatUnit>, units2: ArrayList<CombatUnit>): ArrayList<CombatUnit> {
        //Determines the slower individual unit
        val fastest1 = units1.sortedByDescending { it.speed }
        val fastest2 = units2.sortedByDescending { it.speed }
        if (fastest1[0].speed < fastest2[0].speed) {
            return  units1
        }
        else if (fastest2[0].speed < fastest1[0].speed) {
            return  units2
        }
        else {
            //Determines the slower group
            if (averageSpeed(units1) < averageSpeed(units2)) {
                return units1
            }
            else if (averageSpeed(units2) < averageSpeed(units1)) {
                return units2
            }
            else {
                //Randomly chooses a group
                if (Math.random() < 0.5) {
                    return units1
                }
                else {
                    return units2
                }
            }
        }
    }

    /**
     * @param units is the ArrayList of units whose speed is being averaged.
     * @return the weighted (for health) average of the units' speeds.
     */
    private fun averageSpeed (units: ArrayList<CombatUnit>): Double {
        var weightedTotal = 0.0
        for (unit in units) {
            weightedTotal += unit.speed * unit.health / 100
        }
        return weightedTotal / units.size
    }

    /**
     * Does a single round of combat in all provinces with a combat.
     * @param combatRound is the number of rounds of movement/combat that has passed since the start of the combat or combat post retreats phase.
     */
    private fun doCombatRound(combatRound: Int) {
        create.allProvinces.filter { it.hasCombat() }.forEach{
            it.army.units.forEach { it.clearOrderedMoves() }
            it.doCombatRound()
        }
    }

    /**
     * Does a single step of movement for all units not in combat.
     * @param combatRound is the number of rounds of movement/combat that has passed since the start of the combat or combat post retreat phase.
     */
    private fun doCombatMove(combatRound: Int) {
        create.allUnits.filter { !it.province.hasCombat() }.forEach { it.moveNext(combatRound) }
    }

    /**
     * @return true if at least one province has a combat occuring in it.
     */
    private fun anyProvinceHasCombat(): Boolean {
        return create.allProvinces.any{ it.hasCombat() }
    }

    /**
     * @return true if at least one unit has not made all of its movements in this combat or combat post retreat phase.
     */
    private fun anyUnitsNeedToMove(): Boolean {
        return create.allUnits.any { it.ordersNeedProcessing() }
    }

}