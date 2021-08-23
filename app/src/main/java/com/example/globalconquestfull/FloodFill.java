package com.example.globalconquestfull;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Fills in an existing color with another color.
 */
public class FloodFill {

    /**
     * Fills in an existing color with another color.
     * @param image is the bitmap that has a portion of it being colored.
     * @param startingPoint is starting point of the fill.
     * @param existingColor is the color that is being replaced.
     * @param replacementColor is the color that is replacing the existing color.
     */
    public void floodFill(Bitmap image, Point startingPoint, int existingColor,
                          int replacementColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        int replacement = replacementColor;
        if (existingColor != replacement) {
            Queue<Point> queue = new LinkedList<Point>();
            do {

                int x = startingPoint.x;
                int y = startingPoint.y;
                while (x > 0 && image.getPixel(x - 1, y) == existingColor) {
                    x--;

                }
                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width && image.getPixel(x, y) == existingColor) {
                    image.setPixel(x, y, replacement);
                    if (!spanUp && y > 0
                            && image.getPixel(x, y - 1) == existingColor) {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    } else if (spanUp && y > 0
                            && image.getPixel(x, y - 1) != existingColor) {
                        spanUp = false;
                    }
                    if (!spanDown && y < height - 1
                            && image.getPixel(x, y + 1) == existingColor) {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    } else if (spanDown && y < height - 1
                            && image.getPixel(x, y + 1) != existingColor) {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((startingPoint = queue.poll()) != null);
        }
    }

    /**
     * Fills in all pixels in a region defined by a specifically colored boundary in another color.
     * @param image is the bitmap that has a portion of it being colored.
     * @param x is the x-coordinate of the starting point of the fill.
     * @param y is the y-coordinate of the starting point of the fill.
     * @param fill_color is the color that is replacing the existing color.
     * @param boundary_color is the color of the boundary of the region.
     */
    void boundaryFill4(Bitmap image, int x, int y, int fill_color, int boundary_color)
    {
        if (!new Utilities().isNearBoundary(x, y, boundary_color, image) && image.getPixel(x, y) != fill_color) {
            image.setPixel(x, y, fill_color);
            boundaryFill4(image, x + 1, y, fill_color, boundary_color);
            boundaryFill4(image, x, y + 1, fill_color, boundary_color);
            boundaryFill4(image, x - 1, y, fill_color, boundary_color);
            boundaryFill4(image, x, y - 1, fill_color, boundary_color);
        }
    }

}