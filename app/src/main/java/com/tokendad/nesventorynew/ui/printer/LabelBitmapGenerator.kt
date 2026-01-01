package com.tokendad.nesventorynew.ui.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelBitmapGenerator @Inject constructor(
    // Context might be needed for loading resources later
    // @ApplicationContext private val context: Context 
) {

    fun generateLabel(
        width: Int,
        height: Int,
        title: String,
        subtitle: String,
        qrContent: String,
        iconType: String? = null // "box", "location", "item"
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Clear background

        // Configuration (Scalable based on width)
        val isSmallLabel = width < 150
        val padding = if (isSmallLabel) 4f else 8f
        val qrSize = height - (padding * 2).toInt() // QR takes full height minus padding
        
        // 1. Draw QR Code (Right aligned)
        val qrBitmap = createQrCode(qrContent, qrSize)
        if (qrBitmap != null) {
            val qrX = width - qrSize - padding
            val qrY = padding
            canvas.drawBitmap(qrBitmap, qrX, qrY, null)
        }

        // 2. Text Area (Left side)
        val textWidth = width - qrSize - (padding * 3)
        val textX = padding
        var currentY = padding

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // Title (Bold, Large)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = if (isSmallLabel) 20f else 28f
        val titleLines = wrapText(title, paint, textWidth)
        for (line in titleLines) {
            currentY += paint.textSize
            if (currentY > height) break
            canvas.drawText(line, textX, currentY, paint)
            currentY += 4f // Line spacing
        }

        // Subtitle (Normal, Small)
        currentY += 4f
        paint.typeface = Typeface.MONOSPACE
        paint.textSize = if (isSmallLabel) 14f else 18f
        val subLines = wrapText(subtitle, paint, textWidth)
        for (line in subLines) {
            currentY += paint.textSize
            if (currentY > height - padding) break
            canvas.drawText(line, textX, currentY, paint)
            currentY += 2f
        }
        
        // Icon (Optional - draw in bottom left if space)
        if (iconType != null && currentY < height - 20) {
             drawIcon(canvas, iconType, textX, height - 24f, 20f, paint)
        }

        return bitmap
    }

    private fun createQrCode(content: String, size: Int): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 0 // No margin
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
                // Handle super long words
                while (paint.measureText(currentLine) > maxWidth) {
                    // Primitive character wrap
                    val n = paint.breakText(currentLine, true, maxWidth, null)
                    lines.add(currentLine.substring(0, n))
                    currentLine = currentLine.substring(n)
                }
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
    
    private fun drawIcon(canvas: Canvas, type: String, x: Float, y: Float, size: Float, paint: Paint) {
        // Simple shape drawing for placeholder icons
        val prevStyle = paint.style
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        
        when(type) {
            "box" -> {
                canvas.drawRect(x, y, x + size, y + size, paint)
                canvas.drawLine(x, y, x+size, y+size, paint)
                canvas.drawLine(x+size, y, x, y+size, paint)
            }
            "location" -> {
                canvas.drawCircle(x + size/2, y + size/2, size/2, paint)
                canvas.drawPoint(x + size/2, y + size/2, paint)
            }
        }
        paint.style = prevStyle
    }
}
