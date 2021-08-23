package com.example.globalconquestfull

import android.app.ProgressDialog
import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.view.View
import androidx.core.graphics.applyCanvas
import kotlin.math.pow

/**
 * Paints and draws arrows on map.
 * @param context is the application environment (main activity) that needs to be known to add views to the UI.
 */
class Map(context: Context?) : View(context) {
    /**
     * Used to draw on bitmap: map.
     */
    var paint: Paint = Paint()

    /**
     * Used to draw lines on bitmap: map.
     */
    private val path: Path

    /**
     * Bitmap that is being painted.
     */
    var mainBitmap: Bitmap

    /**
     * Backup bitmap that mBitmap can be reverted to.
     */
    var backupBitmap: Bitmap

    /**
     * Notification that shows up on screen when filling.
     * todo: (POST-ALPHA) is this needed? Could be useful for tests if filling is being slow to show whats going on, but for the real thing? If it is deleted, delete all functions whithin the 2 fill inner classes that use it
     */
    var pd: ProgressDialog

    /**
     * Point from which filling begins.
     */
    val fillStartPoint = Point()

    /**
     * Object which allows for bitmap to be drawn on the screen.
     */
    var canvas: Canvas? = null

    /**
     * Distance in pixels from army to base of order-showing arrow.
     */
    val offsetFromArmy = 75

    /**
     * Tickness in pixels of order-showing arrow.
     */
    val thickness = 25

    /**
     * Offset in pixels from order-showing arrow's length to the sides for the base of the arrowhead.
     */
    val arrowheadOffset = 20

    /**
     * Length in pixels of the order-showing arrow's arrowhead from the base to the point.
     */
    val arrowheadLength = 50

    /**
     * Creates map on screen.
     */
    override fun onDraw(canvas: Canvas) {
        this.canvas = canvas
        canvas.drawBitmap(mainBitmap, 0f, 0f, paint)
    }

    /**
     * Paints an area of a uniform color with a new color.
     * @param x is the x-coordinate of the pixel to be recolored.
     * @param y is the y-coordinate of the pixel to be recolered.
     * @param targetColor is the color to which the pixel is recolored.
     */
    fun fill(x: Int, y: Int, targetColor: Color) {
        fillStartPoint.x = x
        fillStartPoint.y = y
        val sourceColor = mainBitmap.getPixel(x.toInt(), y.toInt())
        FillArea(mainBitmap, fillStartPoint, sourceColor, targetColor).execute()
        invalidate()
    }

    /**
     * todo: (POST-ALPHA) determine function. Delete since not used anywhere?
     */
    fun clear() {
        path.reset()
        invalidate()
    }

    /**
     * todo: (POST-ALPHA) determine function. Delete since not used anywhere?
     */
    val currentPaintColor: Int
        get() = paint.color

    /**
     * Initializes variables.
     */
    // Bitmap mutableBitmap ;
    init {
        paint = Paint()
        paint.isAntiAlias = true
        pd = ProgressDialog(context)
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 5f
        mainBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.test_map_8_13_2020_large_armies_forbidden_color
        ).copy(Bitmap.Config.ARGB_8888, true)
        backupBitmap = mainBitmap.copy(Bitmap.Config.ARGB_8888, true)
        path = Path()
    }

    /**
     * Sets the backup bitmap to be the current bitmap.
     */
    fun saveBitmap() {
        backupBitmap = mainBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    /**
     * Restores the current bitmap to be the backup bitmap.
     */
    fun restoreBitmap() {
        mainBitmap = backupBitmap.copy(Bitmap.Config.ARGB_8888, true)
        invalidate()
    }

    /**
     * Draws arrow between start (base) and end (point).
     * @param start is the point at the center of the base of the arrow.
     * @param end is the point at the tip of the arrow.
     * @param color is the color of the edges of the arrow.
     */
    fun drawUnitOrderArrow(start: Point, end: Point, color: Paint) {
        val arrowLength = distance(start, end)
        val pointsPreConversion = getArrowPoints(arrowLength)
        val points = convertPoints(pointsPreConversion, start, end)
        drawPolygon(points, color)
    }

    /**
     * Determines the distance between two points.
     * @param start is the first point.
     * @param end is the second point.
     * @return distance between the two points.
     */
    private fun distance(start: Point, end: Point): Double {
        return Math.sqrt((end.x - start.x).toDouble().pow(2) + (end.y - start.y).toDouble().pow(2))
    }

    /**
     * Finds all points of the arrow in the coordinate system where the start point (base) is at the origin and the end point (point) is to the right on the x-axis.
     * @param arrowLength is the distance between the base and the tip of the arrow.
     * @return the arrow verticies in the different coordinate system.
     */
    private fun getArrowPoints(arrowLength: Double): ArrayList<Point> {
        val start = Point(offsetFromArmy, 0)
        val end = Point((arrowLength - offsetFromArmy).toInt(), 0)
        val points = arrayListOf(
            Point(start.x, thickness)
            , Point(end.x - arrowheadLength, thickness)
            , Point(end.x - arrowheadLength, thickness + arrowheadOffset)
            , end
            , Point(end.x - arrowheadLength, -thickness - arrowheadOffset)
            , Point(end.x - arrowheadLength, -thickness)
            , Point(start.x, -thickness)
        )
        return points
    }

    /**
     * Converts points from the coordinate system where the start point (base) is at the origin and the end point (point) is to the right on the x-axis to the normal coordinate system.
     * @param pointsPreConversion is an array of the base and tip of the arrow.
     * @param start is the point at the center of the base of the arrow.
     * @param end is the point at the tip of the arrow.
     * @return the arrow verticies in the normal coordinate system.
     */
    private fun convertPoints(pointsPreConversion: java.util.ArrayList<Point>, start: Point, end: Point): ArrayList<Point> {
        val sine = (end.y - start.y) / distance(start, end)
        val cosine = (end.x - start.x) / distance(start, end)
        var points = pointsPreConversion.map { transform(it, sine, cosine) }.map { shift(it, start) }
        return ArrayList(points)
    }

    /**
     * Shifts point by adding x and y values of second point to it. Used to move a correctly-oriented arrow to the right place.
     * @param point is the point that is being shifted. This is a point on the arrow whose base is at the origin.
     * @param start is the amount of shift. This is the point at which the base of the arrow is supposed to be.
     * @return the point in the arrow now positioned correctly for the map.
     */
    private fun shift(point: Point, start: Point): Point {
        return Point(point.x + start.x, point.y + start.y)
    }

    /**
     * Transforms a given point to a different coordinate system. Used to rotate an arrow from laying on the x-axis to having the correct orientation.
     * @param point is the point being rotated.
     * @param sine is used to determine the rotation.
     * @param cosine is used to determine the rotation.
     * @return the now rotated point.
     */
    private fun transform(point: Point, sine: Double, cosine: Double): Point {
        val transformedX = cosine * point.x + sine * point.y
        val transformedY = sine * point.x - cosine * point.y
        return Point(transformedX.toInt(), transformedY.toInt())
    }

    /**
     * Draws lines between set of points, in order, to create a polygon.
     * @param points is the array of verticies of the polygon.
     * @param paint is the color the edges of the polygon are to be made.
     */
    private fun drawPolygon(points: java.util.ArrayList<Point>, paint: Paint) {
        for (x in 0 until points.size - 1) {
            //todo: (POST-ALPHA) in drawline function below, the super function called does not exist, so how is the paint used? It should be of that color, but it isn't working and I need this to check where the problem is.
            mainBitmap.applyCanvas { drawLine(points[x].x.toFloat(), points[x].y.toFloat(), points[x + 1].x.toFloat(), points[x + 1].y.toFloat(), paint) }
        }
        mainBitmap.applyCanvas { drawLine(points[points.size-1].x.toFloat(), points[points.size-1].y.toFloat(), points[0].x.toFloat(), points[0].y.toFloat(), paint) }
    }

    /**
     * Fills an area of a uniform color with a new color.
     * @param bmp is the bitmap that is being colored on.
     * @param fillStartPoint is the point at which the fill begins.
     * @param targetColor is the color that is being filled.
     * @param replacementColor is the color that is replacing the target color.
     */
    internal inner class FillArea(
        var bmp: Bitmap,
        var fillStartPoint: Point,
        var targetColor: Int,
        var replacementColor: Color
    ) : AsyncTask<Void?, Int?, Void?>() {

        /**
         * Displays a message to show that the filling is in progress.
         */
        override fun onPreExecute() {
            pd.show()
        }

        /**
         * todo: (POST-ALPHA) determine function. Can it be deleted?
         * @param values is todo: (POST-ALPHA) what is it?
         */
        protected fun onProgressUpdate(vararg values: Int) {}

        /**
         * Fills the area.
         * @return todo: (POST-ALPHA) what does it return?
         */
        protected override fun doInBackground(vararg params: Void?): Void? {
            val f = FloodFill()
            f.floodFill(bmp, fillStartPoint, targetColor, replacementColor.rgb)
            return null
        }

        /**
         * Removes notification that the filling is in progress when completed.
         */
        override fun onPostExecute(result: Void?) {
            pd.dismiss()
            invalidate()
        }

        /**
         * Displays a notification showing the the fill is in progress.
         */
        init {
            pd.setMessage("Filling....")
            pd.show()
        }
    }

    /**
     * todo: (POST-ALPHA) is the purpose of this to fill the arrow? If so, use it for that.
     * todo: (POST-ALPHA) rename to FillBoundedArea if that is the purpose.
     * Fills in a bounded area with a color.
     * @param bmp is the bitmap that is being colored on.
     * @param pt is the point at which the fill begins.
     * @param borderColor is the color of the boundary of the area being filled.
     * @param fillColor is color the area is being filled with.
     */
    internal inner class TheTaskArrow(
        var bmp: Bitmap,
        var pt: Point,
        //todo: (POST-ALPHA) why border color int but fill color is color?
        var borderColor: Int,
        var fillColor: Color
    ) : AsyncTask<Void?, Int?, Void?>() {

        /**
         * Displays a message to show that the filling is in progress.
         */
        override fun onPreExecute() {
            pd.show()
        }

        /**
         * todo: (POST-ALPHA) determine function. Can it be deleted?
         * @param values is todo: (POST-ALPHA) what is it?
         */
        protected fun onProgressUpdate(vararg values: Int) {}

        /**
         * Fills the area.
         * @param params is todo: (POST-ALPHA) what is it?
         * @return todo: (POST-ALPHA) what is returned?
         */
        protected override fun doInBackground(vararg params: Void?): Void? {
            val f = FloodFill()
            f.boundaryFill4(bmp, pt.x, pt.y, Color.WHITE.rgb, Color.BORDER.rgb)
            return null
        }

        /**
         * Removes notification that the filling is in progress when completed.
         */
        override fun onPostExecute(result: Void?) {
            pd.dismiss()
            invalidate()
        }

        /**
         * Displays a notification showing the the fill is in progress.
         */
        init {
            pd.setMessage("Filling....")
            pd.show()
        }
    }

}