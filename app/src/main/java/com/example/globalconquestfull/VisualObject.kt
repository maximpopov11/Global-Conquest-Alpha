package com.example.globalconquestfull

import android.graphics.Bitmap
import android.graphics.Point
import android.widget.TableLayout
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.pow

/**
 * Parent class to all classes with a visual representation in game, i.e. those objects that can be clicked.
 * @param fillAnchor is a point whithin the visual object at which fills are started.
 * @param allAnchors is the array of all anchor points whithin the visual object.
 */
abstract class VisualObject (val fillAnchor: Point, val allAnchors: ArrayList<Point>) {

    /**
     * Returns the squared distance between the given point and the fillAnchor of this visual object.
     * @param point is the point that the squared distance between it and this visual object is being determined.
     * @return the squared distance between the given point and the fillAnchor of this visual object.
     */
    fun distanceSquared(point: Point): Double {
        return distanceSquared(point, fillAnchor)
    }

    /**
     * Returns the squared distance between two points.
     * @param point1 is one of the points.
     * @param point2 is the other point.
     * @return the squared distance between the given points.
     */
    fun distanceSquared(point1: Point, point2: Point): Double {

        return ((point1.x - point2.x).toDouble()).pow(2) + ((point1.y - point2.y).toDouble()).pow(2)

    }

    /**
     * Determines if this visual object contains a particular point.
     * @param originalPoint is the point that is being determined if it is in the visual object.
     * @param map is the map that contains the visual object.
     * @return true if the point is in the visual object.
     */
    fun contains(originalPoint: Point, map: Bitmap): Boolean {
        var point = originalPoint
        val checkedPoints = HashSet<Point>()
        var existingColor = map.getPixel(point.x, point.y)
        val width = map.getWidth()
        val height: Int = map.getHeight()
        val queue: Queue<Point> =
            LinkedList()
        do {
            var x: Int = point.x
            val y: Int = point.y
            while (x > 0 && map.getPixel(x - 1, y) == existingColor && !checkedPoints.contains(Point(x,y))) {
                x--
            }
            var spanUp = false
            var spanDown = false
            while (x < width && map.getPixel(x, y) == existingColor && !checkedPoints.contains(Point(x,y))) {

                if (!spanUp && y > 0 && map.getPixel(x, y - 1) == existingColor && !checkedPoints.contains(
                        Point(x,y)
                    )
                ) {
                    queue.add(Point(x, y - 1))
                    spanUp = true
                } else if (spanUp && y > 0 && map.getPixel(x, y - 1) != existingColor && !checkedPoints.contains(
                        Point(x,y)
                    )
                ) {
                    spanUp = false
                }
                if (!spanDown && y < height - 1 && map.getPixel(x, y + 1) == existingColor && !checkedPoints.contains(
                        Point(x,y)
                    )
                ) {
                    queue.add(Point(x, y + 1))
                    spanDown = true
                } else if (spanDown && y < height - 1 && map.getPixel(
                        x,
                        y + 1
                    ) != existingColor && !checkedPoints.contains(Point(x,y))
                ) {
                    spanDown = false
                }

                checkedPoints.add(Point(x,y))
                if (allAnchors.contains(Point(x,y))){
                    return true
                }

                x++
            }

            var test = Point(1,1).equals(Point(1,1))

        } while (queue.size > 0 && queue.poll().also { point = it } != null)

        return checkedPoints.intersect(allAnchors).any()
    }

    /**
     * Creates and shows the selectedInformation view for this visual object.
     */
    abstract fun view(context: MainActivity, selectedInforrmation: TableLayout)

}