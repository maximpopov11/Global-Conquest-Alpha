package com.example.globalconquestfull

import android.graphics.Bitmap
import android.graphics.Point

/**
 * Assortment of utility functions.
 */
class Utilities {

    /**
     * Determines if pixel with given x and y coordinates is next to a boundary pixel (including diagonally).
     * @param x is the x-coordinate of the point.
     * @param y is the y-coordinate of the point.
     * @param boundary_color is the color of the boundary of all visual objects.
     * @param image is the bitmap on which the point lies.
     * @return whether or not the point is next to the boundary.
     */
    public fun isNearBoundary(x: Int, y: Int, boundary_color: Int, image: Bitmap): Boolean {
        return arrayListOf(
            Point(x - 1, y - 1),
            Point(x - 1, y),
            Point(x - 1, y + 1),
            Point(x, y - 1),
            Point(x, y),
            Point(x, y + 1),
            Point(x + 1, y - 1),
            Point(x + 1, y),
            Point(x + 1, y + 1)
        ).filter { it.x > 0 && it.x < image.width && it.y > 0 && it.y < image.height }
            .any{ image.getPixel(it.x, it.y) == boundary_color }
    }

}