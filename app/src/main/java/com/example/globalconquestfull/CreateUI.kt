package com.example.globalconquestfull

import android.app.AlertDialog
import android.graphics.Paint
import android.view.View
import android.widget.Button

/**
 * Prepares the user interface for the game.
 * Only 1 such object per game.
 * todo: (POST-ALPHA) why is this a file rather than a class?
 * todo: (POST-ALPHA) why not have this and create in the same class, for creating an individual game? Name that Game or CreateGame?
 * @param activity is the application environment (main activity) that needs to be known to add views to the UI.
 * @param create is the object creating the game functions.
 * @param map is the game map.
 */
class CreateUI(val activity: MainActivity, val create: Create, val map: Map) {

    /**
     * Button that toggles whether unit orders are shown.
     */
    private val buttonToggleOrderVisibility = activity.findViewById<View?>(R.id.buttonToggleOrderVisibility) as Button

    /**
     * True if order arrows are currently being shown.
     */
    var ordersShown = false

    /**
     * Index of current nation in array of nations.
     * todo: is there an easier way of iterating through nations with the button without using this?
     */
    var nationIndex = 0
        set(value) {
            field = if (value < this.create.allNations.size) value else 0
        }

    /**
     * Sets up UI upon initialization of object.
     */
    init{

//        /**
//         * Buttons for moving units in tests.
//         */
//        val button1 = activity.findViewById<android.view.View?>(R.id.moveToProvince1) as android.widget.Button
//        val button2 = activity.findViewById<android.view.View?>(R.id.moveToProvince2) as android.widget.Button
//        val button3 = activity.findViewById<android.view.View?>(R.id.moveToProvince3) as android.widget.Button
//        val button4 = activity.findViewById<android.view.View?>(R.id.moveToProvince4) as android.widget.Button
//        val button5 = activity.findViewById<android.view.View?>(R.id.moveToProvince5) as android.widget.Button

//        /**
//         * Configures the buttons that move units in tests.
//         */
//        configureUnitMovementInTestButton(button1, create.province1)
//        configureUnitMovementInTestButton(button2, create.province2)
//        configureUnitMovementInTestButton(button3, create.province3)
//        configureUnitMovementInTestButton(button4, create.province4)
//        configureUnitMovementInTestButton(button5, create.province5)

        /**
         * Button that changes phases in tests.
         */
        val buttonNextPhase = activity.findViewById<View?>(R.id.buttonNextTurn) as Button
        buttonNextPhase.text = "COMMAND"
        buttonNextPhase.setOnClickListener{
            create.turn.nextPhase()
            buttonNextPhase.text = create.turn.phase.toString()
        }

        /**
         * Button that changes nations in tests.
         * todo: can only select unit when of the same nation
         * todo: when changing nation deselect unit if of different nation
         */
        val buttonChangeNation = activity.findViewById<View?>(R.id.buttonSwitchNation) as Button
        buttonChangeNation.text = "No Nation Selected"
        buttonChangeNation.setOnClickListener{
            this.activity.controllingNation = this.create.allNations[nationIndex]
            buttonChangeNation.text = this.activity.controllingNation?.toString()
            this.nationIndex++
        }

        /**
         * Button that hides the selected information tab.
         */
        val buttonHideSelectedInformation = activity.findViewById<View?>(R.id.hideSelectedInformation) as Button
        buttonHideSelectedInformation.setOnClickListener{
            activity.scrollSelectedInformation.translationZ = 0f
        }

        /**
         * Button that clears the orders of the selected unit.
         */
        val buttonClearOrders = activity.findViewById<View?>(R.id.buttonClearOrders) as Button
        buttonClearOrders.setOnClickListener{
            this.activity.selectedCombatUnit?.clearOrderedMoves()
        }

        /**
         * Button that deselects the currently selected unit.
         */
        val buttonDeselect = activity.findViewById<View?>(R.id.buttonDeselect) as Button
        buttonDeselect.setOnClickListener{
            activity.selectedCombatUnit = null
        }

        buttonToggleOrderVisibility.setOnClickListener{
            if (!ordersShown) {
                showOrders()
            }
            else {
                hideOrders()
            }
        }

    }

    /**
     * Shows order arrows for the selected unit.
     */
    fun showOrders() {
        val selectedUnit = this.activity.selectedCombatUnit
        map.saveBitmap()
        if (selectedUnit != null) {
            val paint = Paint(Color.BORDER.rgb)
            var arrowStart = selectedUnit.province.army.fillAnchor
            var somethingDrawn = false
            //Does nothing if no unit is selected.
            this.activity.selectedCombatUnit?.orderedDestination?.forEach {
                if (it != null) {
                    map.drawUnitOrderArrow(arrowStart, it.army.fillAnchor, paint)
                    arrowStart = it.army.fillAnchor
                    somethingDrawn = true
                }
            }
        }
        this.ordersShown = true
        buttonToggleOrderVisibility.setText("Hide Orders")
    }

    /**
     * Hides order arrows for the selected unit.
     */
    fun hideOrders() {
        map.restoreBitmap()
        this.ordersShown = false
        buttonToggleOrderVisibility.setText("Show Orders")
    }

//    /**
//     * Configures unit movement buttons for tests.
//     */
//    fun configureUnitMovementInTestButton(button: Button, province: Province) {
//        button.setOnClickListener(View.OnClickListener {
//            if (activity.selectedCombatUnit != null) {
//                activity.selectedCombatUnit!!.orderMove(province, create.turn.phase)
//            }
//        })
//    }

}