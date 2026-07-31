package com.fhanafi.pitjarus.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

object BarcodeRenderer {
    fun render(value: String, width: Int = 480, height: Int = 120): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val content = value.trim()
        if (content.isBlank()) {
            bitmap.eraseColor(Color.WHITE)
            return bitmap
        }

        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.CODE_128,
            width,
            height,
            hints
        )

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
