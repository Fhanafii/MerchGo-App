package com.fhanafi.pitjarus.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object ImageCompressor {
    private const val MAX_DIMENSION = 1280
    private const val MIN_QUALITY = 20

    fun compressJpegToMaxSize(file: File, maxSizeKb: Int = 100): ByteArray {
        val maxBytes = maxSizeKb * 1024
        val bitmap = decodeScaledBitmap(file)
        var quality = 90
        var compressed = bitmap.toJpegBytes(quality)

        while (compressed.size > maxBytes && quality > MIN_QUALITY) {
            quality -= 5
            compressed = bitmap.toJpegBytes(quality)
        }

        if (compressed.size <= maxBytes) {
            bitmap.recycle()
            return compressed
        }

        var resized = bitmap
        while (compressed.size > maxBytes && max(resized.width, resized.height) > 160) {
            val ratio = 0.85f
            val nextWidth = (resized.width * ratio).toInt().coerceAtLeast(1)
            val nextHeight = (resized.height * ratio).toInt().coerceAtLeast(1)
            val nextBitmap = Bitmap.createScaledBitmap(resized, nextWidth, nextHeight, true)
            if (resized !== bitmap) resized.recycle()
            resized = nextBitmap
            quality = 85
            compressed = resized.toJpegBytes(quality)

            while (compressed.size > maxBytes && quality > MIN_QUALITY) {
                quality -= 5
                compressed = resized.toJpegBytes(quality)
            }
        }

        if (resized !== bitmap) resized.recycle()
        bitmap.recycle()
        require(compressed.size <= maxBytes) {
            "Ukuran gambar tetap di atas ${maxSizeKb}KB setelah kompresi"
        }
        return compressed
    }

    private fun decodeScaledBitmap(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, bounds)

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return requireNotNull(BitmapFactory.decodeFile(file.absolutePath, options)) {
            "Gagal membaca file gambar"
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        var currentWidth = width
        var currentHeight = height
        while (currentWidth / 2 >= MAX_DIMENSION || currentHeight / 2 >= MAX_DIMENSION) {
            sampleSize *= 2
            currentWidth /= 2
            currentHeight /= 2
        }
        return sampleSize
    }

    private fun Bitmap.toJpegBytes(quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }
}
