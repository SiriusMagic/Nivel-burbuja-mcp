package com.zirakusa.nivel.data

/** Regla: millimetres and inches to screen pixels, from the screen's real density (xdpi / ydpi, dots per inch). */
object Ruler {
    const val MM_PER_INCH = 25.4

    fun pxPerMm(dpi: Float): Double = dpi / MM_PER_INCH
    fun mmToPx(mm: Double, dpi: Float): Double = mm * pxPerMm(dpi)
    fun pxToMm(px: Double, dpi: Float): Double = px / pxPerMm(dpi)

    /** One mark of the scale: where it is (px from the start), how long (1 = longest) and its number, if any. */
    data class Tick(val px: Double, val size: Double, val label: String?)

    /** Centimetres: a mark every mm, longer at 5 mm, the longest with its number every cm. */
    fun cmTicks(lengthPx: Double, dpi: Float): List<Tick> {
        val count = pxToMm(lengthPx, dpi).toInt()
        return (0..count).map { mm ->
            when {
                mm % 10 == 0 -> Tick(mmToPx(mm.toDouble(), dpi), 1.0, (mm / 10).toString())
                mm % 5 == 0 -> Tick(mmToPx(mm.toDouble(), dpi), 0.7, null)
                else -> Tick(mmToPx(mm.toDouble(), dpi), 0.45, null)
            }
        }
    }

    /** Inches: a mark every 1/16″, longer at 1/8, 1/4 and 1/2, the longest with its number every inch. */
    fun inchTicks(lengthPx: Double, dpi: Float): List<Tick> {
        val count = (lengthPx / dpi * 16).toInt()
        return (0..count).map { i ->
            val px = i * dpi / 16.0
            when {
                i % 16 == 0 -> Tick(px, 1.0, (i / 16).toString())
                i % 8 == 0 -> Tick(px, 0.75, null)
                i % 4 == 0 -> Tick(px, 0.6, null)
                i % 2 == 0 -> Tick(px, 0.45, null)
                else -> Tick(px, 0.3, null)
            }
        }
    }
}
