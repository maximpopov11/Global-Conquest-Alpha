package com.example.globalconquestfull

import java.lang.Math.ceil

/**
 * Combat that occurs when units of different nations exist in the same province.
 * @param army is the army of units in the combat.
 */
class Combat(val province: Province) {

    /**
     * Largest distance value of the combat: the largest range value in the combat.
     */
    private var largestDistanceValue = this.province.army.units.sortedByDescending { it.range }[0].range

    /**
     * Current distance value of the combat: how far away the armies are from one another, which increases the range/speed needed for a unit to deal damage.
     */
    private var distanceValue = this.province.army.units.sortedByDescending { it.range }[0].range

    /**
     * ArrayList of pairs of nations and ArrayLists of their units.
     */
    private val unitsByNation = ArrayList<Pair<Nation, ArrayList<CombatUnit>>>()

    /**
     * Arraylist of pairs of nations and ArrayLists of IntArrays representing the damage they will take in this combat round.
     */
    private val damageTakenByNation = ArrayList<Pair<Nation, ArrayList<IntArray>>>()

    /**
     * Starting total health and units per nation.
     * Triple is nation, starting health, starting unit count.
     */
    private val startingHealthPerNation = ArrayList<Triple<Nation, Int, Int>>()

    /**
     * Initializes all variables.
     */
    init {
        val unitPerNation = this.province.army.units.distinctBy { it.nation }
        //For each nation.
        unitPerNation.forEach {
            val nation = it.nation
            //Adds units to unitsByNation.
            var nationUnits = ArrayList<CombatUnit>()
            this.province.army.units.forEach {
                if (it.nation.equals(nation)) {
                    nationUnits.add(it)
                    it.inCombat = true
                }
            }
            unitsByNation.add(Pair(nation, nationUnits))
            //Determines total unit health for startingHealtPerNation.
            var totalHealth = 0
            var totalUnits = 0
            this.province.army.units.forEach {
                if (it.nation.equals(nation)) {
                    totalHealth += it.health
                    totalUnits++
                }
            }
            //Adds nation and health to startingHealthPerNation.
            this.startingHealthPerNation.add(Triple(nation, totalHealth, totalUnits))
            this.damageTakenByNation.add(Pair(nation, ArrayList()))
        }
    }

    /**
     * Completes a round of combat.
     */
    fun nextRound() {
        //All units in the province that are not in the combat enter the combat.
        newUnitsEnterCombat()
        //Determines damage dealt to each nation.
        this.unitsByNation.forEach { attacker ->
            this.unitsByNation.forEach { defender ->
                if (attacker != defender) {
                    val damageDealt = determineDamage(attacker, defender)
                    this.damageTakenByNation.find { it.first == defender.first }!!.second.add(damageDealt)
                }
            }
        }
        //Deals damage to each nation.
        this.damageTakenByNation.forEach {defendingPair ->
            val defender = this.unitsByNation.find { it.first ==  defendingPair.first}
            for (i in defendingPair.second.size - 1 downTo 0) {
                dealDamage(defendingPair.second[i], defender!!)
                defendingPair.second.removeAt(i)
            }
        }
        //Nations with no non-retreating units remaining are removed from the combat.
        for (i in this.unitsByNation.size - 1 downTo 0) {
            if (this.unitsByNation[i].second.isEmpty() || this.unitsByNation[i].second.distinctBy { !it.mustRetreat }.isEmpty()) {
                this.unitsByNation.removeAt(i)
            }
        }
        this.unitsByNation.forEach {
            checkRetreat(it)
        }
        checkVictor()
        //Armies get closer to one another for the next round of combat. When it is negative, 0 range units can attack higher range units if lower range ones do not exist.
        distanceValue--
    }

    /**
     * Adds units that entered the province since the last combat round to the combat.
     */
    private fun newUnitsEnterCombat() {
        this.province.army.units.forEach {combatUnit ->
            if (!combatUnit.inCombat && !combatUnit.mustRetreat) {
                combatUnit.inCombat = true
                val existingNationPair = this.unitsByNation.find { it.first == combatUnit.nation }
                //If the unit's nation is already participating in the combat.
                if (existingNationPair != null) {
                    existingNationPair.second.add(combatUnit)
                    val existingNationData = this.startingHealthPerNation.find { it.first == combatUnit.nation }
                    existingNationData?.second?.plus(combatUnit.health)
                    existingNationData?.third?.plus(1)
                }
                else {
                    val newNationArray = ArrayList<CombatUnit>()
                    newNationArray.add(combatUnit)
                    this.unitsByNation.add(Pair(combatUnit.nation, newNationArray))
                    this.startingHealthPerNation.add(Triple(combatUnit.nation, combatUnit.health, 1))
                    this.damageTakenByNation.add(Pair(combatUnit.nation, ArrayList()))
                }
                //The largestDistanceValue is reevaluated.
                if (combatUnit.range > this.largestDistanceValue) {
                    largestDistanceValue = combatUnit.range
                }
            }
        }
    }

    /**
     * Determines the attacker's damage to the defender.
     * @param attacker is the attacking nation-units pair.
     * @param defender is the nation-units pair taking damage.
     */
    private fun determineDamage(attacker: Pair<Nation, ArrayList<CombatUnit>>, defender: Pair<Nation, ArrayList<CombatUnit>>): IntArray {
        val damageDealt = DoubleArray(this.largestDistanceValue + 1)
        val defendingUnitsByRange = ArrayList<ArrayList<CombatUnit>>()
        for (i in 0..this.largestDistanceValue) {
            defendingUnitsByRange.add(ArrayList<CombatUnit>())
        }
        for (unit in defender.second) {
            defendingUnitsByRange[unit.range].add(unit)
        }
        //Distance of the closest defender.
        var closestDefenderDistance = 0
        while (defendingUnitsByRange[closestDefenderDistance].isEmpty()) {
            closestDefenderDistance++
        }
        findDamageAtDistanceValues(attacker, damageDealt, closestDefenderDistance)
        movePartOfRangeDamageToLowerIndex(damageDealt, closestDefenderDistance)
        moveEmptyDistanceDamageToLowerIndex(damageDealt, defendingUnitsByRange)
        divideDamageByNationCount(damageDealt)
        val dealDamageInt = IntArray(this.largestDistanceValue + 1)
        for (i in 0..damageDealt.size - 1) {
            dealDamageInt[i] = ceil(damageDealt[i]).toInt()
        }
        return dealDamageInt
    }

    /**
     * Fills damageDealt.
     * @param attacker is the attacking nation-units pair.
     * @param damageDealt is an array of doubles representing the damage dealt by the attacking units at index values corresponding to the distance values of defending units.
     * @param closestDefenderDistance is the distance of the closest defender.
     */
    private fun findDamageAtDistanceValues(attacker: Pair<Nation, ArrayList<CombatUnit>>, damageDealt: DoubleArray, closestDefenderDistance: Int) {
        //Distance traveled towards the closest defender.
        val closedDistance = if (this.distanceValue >= 0) 0 else if (-1 * this.distanceValue >= closestDefenderDistance) closestDefenderDistance else -1 * this.distanceValue
        //It is possible that some but not all units of a nation retreat.
        attacker.second.forEach {
            if (!it.mustRetreat) {
                //Furthest range that can be attacked by this unit.
                val attackingRange = if (it.range + closedDistance <= this.largestDistanceValue) it.range + closedDistance else this.largestDistanceValue
                //Furthest distance that can be reached by this unit.
                val attackingSpeedDistance = if (it.speed + closedDistance <= this.largestDistanceValue) it.range + closedDistance else this.largestDistanceValue
                if (attackingRange >= closestDefenderDistance) {
                    damageDealt[attackingRange] += 1.0 * it.strength * it.health / 100
                }
                //Not enough range, but enough speed to get to at least the first line results in attacking the first line.
                else if (attackingSpeedDistance >= closestDefenderDistance) {
                    damageDealt[closestDefenderDistance] += 1.0 * it.strength * it.health / 100
                }
            }
        }
    }

    /**
     * Moves 75% of ranged damage to a lower distance index.
     * @param damageDealt is the damageDealt by the attacking nation-units pair.
     * @param closestDefenderDistance is the distance of the closest defender.
     */
    private fun movePartOfRangeDamageToLowerIndex(damageDealt: DoubleArray, closestDefenderDistance: Int) {
        val lowestMove = if (closestDefenderDistance >= 1) closestDefenderDistance else 0
        for (i in damageDealt.size - 1 downTo lowestMove + 1) {
            val moveDamage = 0.75 * damageDealt[i]
            damageDealt[i] -= moveDamage
            damageDealt[i-1] += moveDamage
        }
    }

    /**
     * If no defending units exist at a range index the damage at that index is taken at the next lowest index.
     * @param damageDealt is the damageDealt by the attacking nation-units pair.
     * @param defendingUnitsByRange are the units taking damage indexed by their range value.
     */
    private fun moveEmptyDistanceDamageToLowerIndex(damageDealt: DoubleArray, defendingUnitsByRange: ArrayList<ArrayList<CombatUnit>>) {
        for (i in defendingUnitsByRange.size - 1 downTo 1) {
            if (defendingUnitsByRange[i].isEmpty()) {
                damageDealt[i-1] += damageDealt[i]
                damageDealt[i] = 0.0
            }
        }
    }

    /**
     * Divides the damage dealt by the number of enemy nations.
     * @param damageDealt is the damageDealt by the attacking nation-units pair.
     */
    private fun divideDamageByNationCount(damageDealt: DoubleArray) {
        val numberOfEnemyNations = this.unitsByNation.size - 1
        for (i in 0..damageDealt.size - 1) {
            damageDealt[i] = damageDealt[i] / numberOfEnemyNations
        }
    }

    /**
     * Deals the given damage to the given units.
     * @param dealDamageInt is the damageDealt by the attacking nation-units pair.
     * @param defender is the nation-units pair taking damage.
     */
    private fun dealDamage(dealDamageInt: IntArray, defender: Pair<Nation, ArrayList<CombatUnit>>) {
        val defendingUnitsByRange = ArrayList<ArrayList<CombatUnit>>()
        for (i in 0..this.largestDistanceValue) {
            defendingUnitsByRange.add(ArrayList<CombatUnit>())
        }
        for (unit in defender.second) {
            defendingUnitsByRange[unit.range].add(unit)
        }
        //Deals damage from correct range index of damagePerNation to the units of that range.
        for (i in 0..dealDamageInt.size - 1) {
            val numberOfUnitsAtRange = defendingUnitsByRange[i].size
            if (numberOfUnitsAtRange > 0) {
                val damageToEach = ceil(1.0 * dealDamageInt[i] / numberOfUnitsAtRange).toInt()
                var extraDamage = dealDamageInt[i] % numberOfUnitsAtRange
                defendingUnitsByRange[i].forEach {
                    //It does not take damage if it has taken retreat damage already.
                    if (!it.hasTakenRetreatDamage && it.takeDamage(damageToEach)) {
                        //This unit is disbanded (from takeDamage returning true) and removed from appropriate ArrayLists since the unit is now out of the combat.
                        removeUnitFromArrays(it, defender.second, defendingUnitsByRange[i], this.unitsByNation.find { it.equals(defender) })
                    }
                    if (it.mustRetreat) {
                        //HasTakenRetreatDamage is never set to false by combat, but when mustRetreat is set to false, hasTakenRetreatDamage is set to false as well.
                        it.hasTakenRetreatDamage = true
                        //Removed from appropriate ArrayLists since the unit is now out of the combat.
                        removeUnitFromArrays(it, defender.second, defendingUnitsByRange[i], this.unitsByNation.find { it.equals(defender) })
                    }
                }
                //Deals all extra damage to random units. It is possible that some units take more than 1 damage and some 0.
                while (extraDamage > 0) {
                    val unit = defendingUnitsByRange[i].random()
                    if (unit.takeDamage(1)) {
                        removeUnitFromArrays(unit, defender.second, defendingUnitsByRange[i], this.unitsByNation.find { it.equals(defender) })
                    }
                    extraDamage--
                }
            }
        }
    }

    /**
     * Removes given combat unit (which has just disbanded) from given arrays in the combat.
     * @param combatUnit is the unit being removed.
     * @param units is the first array to remove from.
     * @param unitsByRange is the second array to remove from.
     * @param unitsByNationInDamage is the pair whose second value is the third array to remove from.
     */
    private fun removeUnitFromArrays(combatUnit: CombatUnit, units: ArrayList<CombatUnit>, unitsByRange: ArrayList<CombatUnit>, unitsByNationInDamage: Pair<Nation, ArrayList<CombatUnit>>?) {
        units.remove(combatUnit)
        unitsByRange.remove(combatUnit)
        unitsByNationInDamage?.second?.remove(combatUnit)
    }

//    /**
//     * @param pair are the nation and units of a particular nation in the combat.
//     * @return the damage dealt by the units at distance values in this combat round.
//     */
//    private fun damageAtDistanceValues(pair: Pair<Nation, ArrayList<CombatUnit>>): DoubleArray {
//        val damageDealt = DoubleArray(this.largestDistanceValue + 1)
//        pair.second.forEach {
//            //It is possible that some but not all units of a nation retreat.
//            if (!it.mustRetreat) {
//                val nonNegativeDistanceValue = if (this.distanceValue >= 0) this.distanceValue else 0
//                val distance = it.range - nonNegativeDistanceValue
//                if (distance >= 0) {
//                    damageDealt[distance] += 1.0 * it.strength * it.health / 100
//                }
//                //Not enough range, but enough speed to get to at least the first line results in attacking the first line.
//                else if (it.speed - nonNegativeDistanceValue >= 0) {
//                    damageDealt[0] += 1.0 * it.strength * it.health / 100
//                }
//            }
//        }
//        return damageDealt
//    }

//    /**
//     * @param damageValues is an array of damage dealt split by the highest distance value the damage is dealt to.
//     * @return an array of damage dealt per distance value.
//     */
//    private fun damageDealt(damageValues: DoubleArray): IntArray {
//        val damageDealt = damageValues.clone()
//        for (i in 1..damageDealt.size - 1) {
//            for (j in 0..i - 1) {
//                val moveDamage = 0.75 * damageDealt[i]
//                damageDealt[j] += moveDamage
//                damageDealt[i] -= moveDamage
//            }
//            //Remainder stays in i index.
//        }
//        val damageDealtRoundedUp = IntArray(damageDealt.size)
//        for (i in 0..damageDealt.size - 1) {
//            damageDealtRoundedUp[i] = ceil(damageDealt[i]).toInt()
//        }
//        return damageDealtRoundedUp
//    }

//    /**
//     * Deals damage from given nation to other nations.
//     * @param nation is the nation whose units are dealing damage.
//     * @param damageDealt is the total damage dealt by that nation's units per distance value.
//     */
//    private fun dealDamage(nation: Nation, damageDealt: IntArray) {
//        val damagePerNation = dealDamageSplit(damageDealt)
//        this.unitsByNation.forEach {
//            if (!it.second.equals(nation)) {
//                dealDamageToUnits(it, damagePerNation)
//            }
//        }
//    }

//    /**
//     * @param damageDealt is the damage dealt by that nation's units per distance value.
//     * @return damage dealt to each nation.
//     */
//    private fun dealDamageSplit(damageDealt: IntArray): IntArray {
//        val damagePerNation = IntArray(damageDealt.size)
//        val numberOfNations = this.unitsByNation.size - 1
//        for (i in 0..damagePerNation.size - 1) {
//            damagePerNation[i] = ceil(1.0 * damageDealt[i] / numberOfNations).toInt()
//        }
//        return damagePerNation
//    }

//    /**
//     * Deals the appropriate damage to the given units.
//     * @param pair are the nation and units taking damage.
//     * @param damagePerNation is the damage dealt to the units of this nation per distance value.
//     */
//    private fun dealDamageToUnits(pair: Pair<Nation, ArrayList<CombatUnit>>, damagePerNation: IntArray) {
//        val nation = pair.first
//        val modifiedDamagePerNation = damagePerNation.clone()
//        val unitsByRange = ArrayList<ArrayList<CombatUnit>>()
//        for (i in 0..this.largestDistanceValue) {
//            unitsByRange.add(ArrayList<CombatUnit>())
//        }
//        for (unit in pair.second) {
//            unitsByRange[unit.range].add(unit)
//        }
//        moveDamageToHigherDistanceIndex(unitsByRange, modifiedDamagePerNation)
//        moveDamageToLowerDistanceIndex(unitsByRange, modifiedDamagePerNation)
//        //Deals damage from correct range index of damagePerNation to the units of that range.
//        for (i in 0..modifiedDamagePerNation.size - 1) {
//            val numberOfUnitsAtRange = unitsByRange[i].size
//            if (numberOfUnitsAtRange > 0) {
//                val damageToEach = ceil(1.0 * modifiedDamagePerNation[i] / numberOfUnitsAtRange).toInt()
//                var extraDamage = modifiedDamagePerNation[i] % numberOfUnitsAtRange
//                unitsByRange[i].forEach {
//                    //If it is disbanded.
//                    //It does not take damage if it has taken retreat damage already.
//                    if (!it.hasTakenRetreatDamage && it.takeDamage(damageToEach)) {
//                        //Removed from appropriate ArrayLists since the unit is now out of the combat.
//                        removeUnitFromArrays(it, pair.second, unitsByRange[i], this.unitsByNation.find { it.equals(pair) })
//                    }
//                    if (it.mustRetreat) {
//                        //HasTakenRetreatDamage is never set to false by combat, but when mustRetreat is set to false, hasTakenRetreatDamage is set to false as well.
//                        it.hasTakenRetreatDamage = true
//                        //Removed from appropriate ArrayLists since the unit is now out of the combat.
//                        removeUnitFromArrays(it, pair.second, unitsByRange[i], this.unitsByNation.find { it.equals(pair) })
//                    }
//                }
//                //Deals all extra damage to random units. It is possible that some units take more than 1 damage and some 0.
//                while (extraDamage > 0) {
//                    val unit = unitsByRange[i].random()
//                    if (unit.takeDamage(1)) {
//                        pair.second.remove(unit)
//                        unitsByRange[i].remove(unit)
//                        this.unitsByNation.find { it.equals(pair) }?.second?.remove(unit)
//                    }
//                    extraDamage--
//                }
//            }
//        }
//    }

//    /**
//     * Moves damage to higher distance index if distance value of combat is negative and no units exist at the current distance value.
//     * @param unitsByRange are the units taking damage indexed by their range value.
//     * @param modifiedDamagePerNation is the amount of damage the units take indexed by distance value.
//     */
//    private fun moveDamageToHigherDistanceIndex(unitsByRange: ArrayList<ArrayList<CombatUnit>>, modifiedDamagePerNation: IntArray) {
//        if (this.distanceValue < 0) {
//            //Does not get to highest distance value because that cannot be moved up.
//            for (i in unitsByRange.size - 2 downTo 0) {
//                //Maximum distance value that damage at this range can be increased to.
//                val maximumDistanceValue = if (i - this.distanceValue <= unitsByRange.size - 1) i - this.distanceValue else unitsByRange.size - 1
//                for (j in i..maximumDistanceValue - 1) {
//                    //If there are no units at the current distance value to take damage.
//                    if (unitsByRange[j].size == 0) {
//                        modifiedDamagePerNation[j+1] += modifiedDamagePerNation[j]
//                        modifiedDamagePerNation[j] = 0
//                    }
//                }
//            }
//        }
//    }

//    /**
//     * Moves damage to lower distance index if no units to take it at that range index.
//     * @param unitsByRange are the units taking damage indexed by their range value.
//     * @param modifiedDamagePerNation is the amount of damage the units take indexed by distance value.
//     */
//    private fun moveDamageToLowerDistanceIndex(unitsByRange: ArrayList<ArrayList<CombatUnit>>, modifiedDamagePerNation: IntArray) {
//        for (i in unitsByRange.size - 1 downTo 1) {
//            if (unitsByRange[i].size == 0) {
//                modifiedDamagePerNation[i-1] += modifiedDamagePerNation[i]
//                modifiedDamagePerNation[i] = 0
//            }
//        }
//    }

//    /**
//     * Removes given combat unit (which has just disbanded) from given arrays in the combat.
//     * @param combatUnit is the unit being removed.
//     * @param units is the first array to remove from.
//     * @param unitsByRange is the second array to remove from.
//     * @param unitsByNationInDamage is the pair whose second value is the third array to remove from.
//     */
//    private fun removeUnitFromArrays(combatUnit: CombatUnit, units: ArrayList<CombatUnit>, unitsByRange: ArrayList<CombatUnit>, unitsByNationInDamage: Pair<Nation, ArrayList<CombatUnit>>?) {
//        units.remove(combatUnit)
//        unitsByRange.remove(combatUnit)
//        unitsByNationInDamage?.second?.remove(combatUnit)
//    }

    /**
     * Checks if the army of the given nation must retreat.
     * @param pair are the nation and units that are being checked.
     */
    private fun checkRetreat(pair: Pair<Nation, ArrayList<CombatUnit>>) {
        val nation = pair.first
        val startingHealth = this.startingHealthPerNation.find { it.first.equals(nation) }?.second
        var currentHealth = 0
        pair.second.forEach {
            currentHealth += it.health
        }
        //If lost less than 40% starting health.
        if (1.0 * currentHealth / startingHealth!! >= 0.6) {
            return
        }
        //If lost less than 20% maximum health.
        val maximumHealth = 100 * this.startingHealthPerNation.find { it.first.equals(nation) }?.third!!
        val healthLost = startingHealth - currentHealth
        if (1.0 * healthLost < maximumHealth!! * 0.2) {
            return
        }
        pair.second.forEach {
            if (it.canRetreat()) {
                it.mustRetreat = true
            }
        }
        if (pair.second.size == 0) {
            this.unitsByNation.remove(pair)
        }
    }

    /**
     * Checks if the combat has a victor and sets the controlling nation if so.
     */
    private fun checkVictor() {
        //If there are at least 2 nations that have not taken retreat damage and at least one that does not have to retreat the combat continues.
        if (this.province.army.units.filter { !it.hasTakenRetreatDamage }.distinctBy { it.nation }.size >= 2 && this.province.army.units.filter { !it.mustRetreat }.isNotEmpty()) {
            return
        }
        //If no units remain in this province nothing happens and thus the controlling nation remains the same.
        else if (this.province.army.units.isEmpty()) {
            return
        }
        //If only one nation remains that is not retreating it wins.
        //Otherwise the current controller wins if they have a unit retreating that has not taken retreat damage.
        //Otherwise a random unit's nation is the victor.
        val clearVictor = this.province.army.units.filter { !it.mustRetreat }.distinctBy { it.nation }.size == 1
        val currentNationVictor = this.province.army.units.filter { !it.hasTakenRetreatDamage && it.nation == this.province.controllingNation }.isNotEmpty()
        val victor: Nation =
            if (clearVictor) this.province.army.units.first().nation
            else if (currentNationVictor) this.province.controllingNation
            else this.province.army.units.random().nation
        this.province.army.units.filter { it.nation == victor }.forEach { it.mustRetreat = false }
        this.province.controllingNation = victor
    }


}