package com.example.globalconquestfull

//todo: (POST-ALPHA) is the code below needed? Can it be deleted?
/*class ColorFunctions {

    fun convertFromRGB (red: Int, green: Int, blue: Int, transparency: Int = 100): Int {
        return (transparency and 0xff) shl 24 or (red and 0xff) shl 16 or (green and 0xff) shl 8 or (blue and 0xff)
    }

    private object noneNationHolder {
        val INSTANCE = Nation("none", Color.WHITE, Color.WHITE, Color.BLACK)
    }
    companion object {
        val noneNation: Nation by lazy {noneNationHolder.INSTANCE}
    }

}*/

/**
 * Colors.
 */
enum class Color(val rgb: Int) {
    RED(-0x10000)
    , GREEN(-0xff0100)
    , BLUE(-0xffff01)
    , WHITE(-0x1)
    , MAGENTA (android.graphics.Color.MAGENTA)
    , CYAN (android.graphics.Color.CYAN)
    , YELLOW (android.graphics.Color.YELLOW)
    , GRAY (android.graphics.Color.GRAY)
    , LIGHTGRAY (android.graphics.Color.LTGRAY)
    , BLACK (android.graphics.Color.BLACK)
    , BORDER (colorFunctions.convertFromRGB(0,0,1))
    , FORBIDDENAREA (-65537);

    /**
     * Used to create border color.
     */
    private object colorFunctions {
        fun convertFromRGB (red: Int, green: Int, blue: Int, transparency: Int = 100): Int {
            return (transparency and 0xff) shl 24 or (red and 0xff) shl 16 or (green and 0xff) shl 8 or (blue and 0xff)
        }
    }

    //todo: (POST-ALPHA) is the code below needed or can it be deleted?
    /*
    IN JAVA NOT KOTLIN

    @ColorInt public static final int BLACK       = 0xFF000000;
    @ColorInt public static final int DKGRAY      = 0xFF444444;
    @ColorInt public static final int GRAY        = 0xFF888888;
    @ColorInt public static final int LTGRAY      = 0xFFCCCCCC;
    @ColorInt public static final int WHITE       = 0xFFFFFFFF;
    @ColorInt public static final int RED         = 0xFFFF0000;
    @ColorInt public static final int GREEN       = 0xFF00FF00;
    @ColorInt public static final int BLUE        = 0xFF0000FF;
    @ColorInt public static final int YELLOW      = 0xFFFFFF00;
    @ColorInt public static final int CYAN        = 0xFF00FFFF;
    @ColorInt public static final int MAGENTA     = 0xFFFF00FF;
     */
}

/**
 * Turn phases.
 */
enum class Phase(val phase: Int) {
    COMMAND(1)
    , COMBAT(2)
    , RETREAT(3)
    , COMBATPOSTRETREAT (4)
    , BUILD (5)
}

/**
 * Unit classes.
 */
enum class UnitClass(val ID: Int) {
    Land (1)
    , Naval (2)
    , Air (3)
}

/**
 * Unit subclasses.
 */
enum class UnitSubClass(val ID: Int) {
    Infantry (1)
    , Mounted (2)
    , Siege (3)
    , Coastal (4)
    , DeepSea (5)
    , Transport (6)
    , Trade (7)
    , Speacilty (8)
    , Fighter (9)
    , Bomber (10)
}

/**
 * Individual unit types.
 */
enum class UnitType(val ID: Int, val shortName: String) {
    Clubman (1, "Clubman")
    , Archer (2, "Archer")
    , BatteringRam (3, "Battering Ram")
}

/**
 * Notifications for illegal orders.
 */
enum class OrderMoveResult(val result: Int, val text: String) {
    SUCCESS(1, "ERROR: This should not show")
    , CANNOTGIVEORDERS (2, "This unit cannot receive orders during this phase")
    , ORDERSFULL (3, "All orders have already been given for this unit.")
    , NOTADJACENT (4, "The ordered province not adjacent to the last ordered province.")
    , AVANCERETREAT (5, "You cannot retreat into province from which a surviving victor unit came from.");
}