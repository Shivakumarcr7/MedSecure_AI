package com.example.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {

    /**
     * Standard domain for medicine verification URLs
     */
    const val VERIFY_URL_BASE = "https://medsecure.ai/verify/"

    fun getVerificationUrl(medicineId: String): String {
        return "$VERIFY_URL_BASE${medicineId.trim().uppercase()}"
    }

    /**
     * Generates a high resolution QR Bitmap for a medicine verification URL or ID
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        moduleColor: Int = Color.rgb(15, 118, 110), // Teal dark
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )

            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) moduleColor else backgroundColor
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
