package com.example.globalconquestfull

import android.app.AlertDialog
import android.graphics.Point
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.children

/**
 * Army that contains all combat units in it's province.
 * @param fillAnchor is a point in the province used as the starting point for fills thereof.
 * @param allAnchors is an array list of all of the fill anchors of this army, including the primary fill anchor.
 */
class Army(fillAnchor: Point, allAnchors: ArrayList<Point>, val province: Province) : VisualObject(fillAnchor, allAnchors) {

    /**
     * ArrayList of all units in this army/province.
     */
    val units = ArrayList<CombatUnit>()

    /**
     * @return the armies location in a readable format.
     */
    override fun toString(): String {
        return "army: " + fillAnchor.x + " " + fillAnchor.y
    }

    /**
     * Sets up the information view for this army.
     * @param context is the application environment (main activity) that needs to be known to add views to the UI.
     * @param selectedInforrmation is the layout in which to place the required information.
     */
    override fun view(context: MainActivity, selectedInforrmation: TableLayout) {

        /**
         * The second row of the table which labels the table below it.
         */
        val secondRow = TableRow(context)

        /**
         * The text displayed in the second row of the table.
         */
        val secondRowText = TextView(context)

        //Sets up the second row of the table to label what is to come below.
        secondRowText.setText("Type/Strength/Health")
        secondRowText.setTextColor(Color.WHITE.rgb)
        secondRow.addView(secondRowText)
        secondRow.setBackgroundColor(Color.BLACK.rgb)
        selectedInforrmation.addView(secondRow)

        //Sets up the second row if there are units in the army which thus need a header to explain their provided information.
        if (this.units.any()) {
            val params = secondRowText.getLayoutParams() as TableRow.LayoutParams
            params.span = 3
            secondRowText.setLayoutParams(params)
        }

        //Orders the table by nation, then unit class, then unit subclass, then unit type, then by health.
        val order = compareBy<CombatUnit>({it.nation.name}, {it.unitClass.name}, {it.unitSubClass.name}, {it.unitType.shortName}).thenByDescending({it.health})
        this.units.sortedWith(order).forEach { combatUnit ->
            val row = TableRow(context)
            arrayListOf(
                combatUnit.unitType.shortName,
                "" + combatUnit.strength,
                "" + combatUnit.health + "                     "
            ).forEach { text ->
                val textView = TextView(context)
                textView.setText(text)
                textView.setTextColor(combatUnit.nation.textColor.rgb)
                row.addView(textView)
            }
            row.children.first().setPadding(0, 0, 20, 0)
            row.children.last().setPadding(20, 0, 0, 0)
            row.setBackgroundColor(combatUnit.nation.color.rgb)
            row.setOnClickListener{
                context.selectedCombatUnit = combatUnit
            }
            selectedInforrmation.addView(row)

            val rowParams = row.getLayoutParams() as TableLayout.LayoutParams
            rowParams.width = selectedInforrmation.width - 1
            row.setLayoutParams(rowParams)

            val dividerView = View(context)
            dividerView.setBackgroundColor(Color.BLACK.rgb)
            selectedInforrmation.addView(dividerView)
            val dividerParams = dividerView.getLayoutParams() as TableLayout.LayoutParams
            dividerParams.height = 1
            dividerView.setLayoutParams(dividerParams)
        }
    }

}