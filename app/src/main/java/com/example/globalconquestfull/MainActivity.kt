package com.example.globalconquestfull

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.isVisible

/**
 * Runs the universal aspects of game.
 */
class MainActivity : AppCompatActivity(), View.OnTouchListener {

    /**
     * Layout onto which the map is placed.
     */
    private var drawingLayout: LinearLayout? = null

    /**
     * Map.
     */
    private lateinit var map: Map

    /**
     * Used in diagonal scroll.
     */
    private var mx = 0f

    /**
     * Used in diagonal scroll.
     */
    private var my = 0f

    /**
     * Used in diagonal scroll.
     */
    private val curX = 0f

    /**
     * Used in diagonal scroll.
     */
    private val curY = 0f

    /**
     * Vertical scroll view for map.
     */
    private lateinit var vScroll: ScrollView

    /**
     * Horizontal scroll view for map.
     */
    private lateinit var hScroll: HorizontalScrollView

    /**
     * Selected information view.
     */
    lateinit var selectedInforrmation: TableLayout

    /**
     * Scroll view for selected information view.
     */
    lateinit var scrollSelectedInformation: ScrollView

    /**
     * Creates all game objects for the map.
     */
    private lateinit var create: Create

    /**
     * Minimum number of pixels of movement for a click to be considered a scroll.
     */
    val scrollThreshold = 10

    /**
     * X-coordinate of down part of click.
     */
    var lastDownEventX: Int? = null

    /**
     * Y-coordinate of down part of click.
     */
    var lastDownEventY: Int? = null

    /**
     * Visual object that is currently selected.
     */
    var selectedVisualObject: VisualObject? = null

    /**
     * Controlling nation.
     */
    var controllingNation: Nation? = null

    /**
     * Combat unit that is currently selected.
     */
    var selectedCombatUnit: CombatUnit? = null
        /**
         * Sets buttons as visible if there is a combat unit selected.
         * If orders were shown for the previously selected combat unit, they are shown for the new one.
         */
        set(value) {
            if (value == null || value.nation == this.controllingNation) {
                val ordersShown = this.create.createUI.ordersShown
                if (ordersShown) {
                    this.create.createUI.hideOrders()
                }
                field = value
                (findViewById<View?>(R.id.buttonToggleOrderVisibility) as Button).isVisible =
                    value != null
                (findViewById<View?>(R.id.buttonDeselect) as Button).isVisible = value != null
                (findViewById<View?>(R.id.buttonClearOrders) as Button).isVisible = value != null
                if (ordersShown) {
                    this.create.createUI.showOrders()
                }
                if (this.selectedCombatUnit != null) {
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setMessage("" + this.selectedCombatUnit!!.nation.name + " " + this.selectedCombatUnit!!.unitType + " " + this.selectedCombatUnit!!.strength + " " + this.selectedCombatUnit!!.health)
                    val alert = dialogBuilder.create()
                    alert.setTitle("Unit Selected")
                    alert.show()
                }
            }
            else {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("This is not your unit.")
                val alert = dialogBuilder.create()
                alert.setTitle("Unit Selection Failed")
                alert.show()
            }
        }

    /**
     * Determines if a click is currently being processed.
     */
    var currentlyProcessingClick = false

    /**
     * Initializes declared variables and prepares map.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        vScroll = findViewById<View>(R.id.vScroll) as ScrollView
        hScroll = findViewById<View>(R.id.scrollView) as HorizontalScrollView
        selectedInforrmation = findViewById(R.id.selectedInformation) as TableLayout
        scrollSelectedInformation = findViewById(R.id.scrollSelectedInformation) as ScrollView
        map = Map(this)
        drawingLayout = findViewById<View>(R.id.relative_layout) as LinearLayout
        drawingLayout!!.addView(map)
        map.layoutParams = LinearLayout.LayoutParams(2000, 2000)
        create = Create(this, map)

        //todo: (POST-ALPHA) testing arrow color below. At this point, it seems like the map is not ready for some reason. It should be made on the line initializing map I think.
//        Thread.sleep(10000)
//        var paint: Paint = Paint()
//        paint.setColor(Color.WHITE.rgb)
//        map.drawUnitOrderArrow(Point(100, 100), Point (300, 300), Paint())
//        //use theTaskArrow inner class of Map to color in the arrow
//        map.TheTaskArrow(map.mainBitmap, Point(200, 200), Color.BLACK.rgb, Color.BLUE)
    }

    /**
     * /todo: (POST-ALPHA) determine function. Something that occurs before anything else on touch? What is the return for then?
     * Required for program to compile.
     * @return todo: (POST-ALPHA) what does it return?
     */
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false;
    }

    //todo: is there a way to make clicks on buttons not also select whatever is underneath the button?
    /**
     * Finds click location and selects appropriate visual object.
     * @return todo: (POST-ALPHA) what does it return? True if event was "consumed" in super function. Pass it along if false, or not consumed. Who does this pass to?
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (!currentlyProcessingClick) {
            currentlyProcessingClick = true
            val selectedVisualObjectFixedRows = 1
            //Z-value at which selected information view is shown.
            val informationViewShown = 100f
            if (event != null) {
                //If information view is not currently shown.
                var startPoint: Point? = null
                if (event.action == MotionEvent.ACTION_DOWN) {
                    lastDownEventX = event.x.toInt()
                    lastDownEventY = event.y.toInt()
                }
                if (event.action == MotionEvent.ACTION_UP && lastDownEventX != null && lastDownEventY != null) {
                    //If less motion than scrollThreshold, then click is treated as a selection click.
                    if (lastDownEventX!! - event.x.toInt() < scrollThreshold && lastDownEventY!! - event.y.toInt() < scrollThreshold
                        && map.mainBitmap.getPixel(event.x.toInt() + hScroll.scrollX, event.y.toInt() + vScroll.scrollY) != Color.FORBIDDENAREA.rgb) {
                        //Finds the selected visual object.
                        selectedVisualObject = findVisualObject(
                            Point(
                                event.x.toInt() + hScroll.scrollX,
                                event.y.toInt() + vScroll.scrollY
                            )
                        )
                        //If unit is selected, then click is treated as command click.
                        if (this.selectedCombatUnit != null) {
                            val destination = if (selectedVisualObject is Province) selectedVisualObject else (selectedVisualObject as Army).province
                            this.selectedCombatUnit!!.orderMove(destination as Province, this.create.turn.phase)
                        }
                        //Else, click is treated as a select visual object click (but only if no visual object is currently selected).
                        else if (scrollSelectedInformation.translationZ != informationViewShown) {
                            for (x in selectedInforrmation.childCount - 1 downTo selectedVisualObjectFixedRows) {
                                selectedInforrmation.removeViewAt(x)
                            }
                            (selectedVisualObject as VisualObject).view(
                                this,
                                selectedInforrmation
                            )
                            scrollSelectedInformation.translationZ = informationViewShown
                        }
                    }
                }
            }
            currentlyProcessingClick = false
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * Given a point finds the visual object the point is in.
     * @return the visual object in which the point resides.
     */
    private fun findVisualObject(point: Point): VisualObject {
        //Sorts all anchor points by how close they are to the click and starting with the closest tests if it is in the correct object.
        //Todo: (POST-ALPHA) If there are a lot of anchor points will this become inneficient? Say 5 points per each of up to 1000 provinces +1 in each per army? is there a way to find and test close points without looking at them all? Maybe checking those whithin a certain x and y pixel value range first? Maybe having multiple increments of that?
        create.allVisualObjects.sortedBy { it.distanceSquared(point) }.forEach{
            if (it.contains(point, map.mainBitmap)) {
                return it
            }
        }
        //If no object is found, province 1 is returned and an alert is shown explaining that.
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Returning Province 1")
        val alert = dialogBuilder.create()
        alert.setTitle("Click Not Found")
        alert.show()
        return create.province1
    }

    //todo: (POST-ALPHA) implement Diagonal Scroll below
    //useful? : https://stackoverflow.com/questions/6237200/motionevent-gety-and-getx-return-incorrect-values
    /*if (event.getAction()==MotionEvent.ACTION_MOVE) {
        layout.leftMargin = (int) event.getX() - dragIcon.getWidth()/2;
        layout.topMargin = (int) event.getY() - dragIcon.getHeight()/2;
        //Log.d("Tag", "Pozycja: " +  event.getX() +", "+  event.getY());
    }
    dragIcon.setLayoutParams(layout);
    */

    //note that event.x/y gives coordinate reletive to view, not screen, use raw
    /* override fun onTouchEvent(event: MotionEvent): Boolean {
         val curX: Float
         val curY: Float
         when (event.action) {
             MotionEvent.ACTION_DOWN -> {
                 mx = event.x
                 my = event.y
             }
             MotionEvent.ACTION_MOVE -> {
                 curX = event.x
                 curY = event.y
                 vScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                 hScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                 mx = curX
                 my = curY
             }
             MotionEvent.ACTION_UP -> {
                 curX = event.x
                 curY = event.y
                 vScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                 hScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
             }
         }
         return true
     }*/

}