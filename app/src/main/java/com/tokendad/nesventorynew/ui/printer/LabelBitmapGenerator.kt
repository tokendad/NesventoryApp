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

        // Configuration
        val isSmallLabel = width < 150
        val padding = if (isSmallLabel) 4f else 8f
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // Determine Layout (Landscape vs Portrait)
        // For D110 (width=96), we usually have a long strip (height > width).
        val isPortrait = height > width

        val qrSize: Int
        val qrX: Float
        val qrY: Float
        val textWidth: Float
        val textX: Float
        var currentY: Float

        if (isPortrait) {
            // Stacked: Text Top, QR Bottom
            qrSize = width - (padding * 2).toInt()
            qrX = padding
            qrY = height - qrSize - padding
            
            textWidth = width - (padding * 2)
            textX = padding
            currentY = padding
        } else {
            // Side-by-Side: Text Left, QR Right
            qrSize = height - (padding * 2).toInt()
            qrX = width - qrSize - padding
            qrY = padding
            
            textWidth = width - qrSize - (padding * 3)
            textX = padding
            currentY = padding
        }
        
        // Safety check for layout
        if (textWidth <= 0) {
             // Fallback to minimal layout to prevent crash
             return bitmap 
        }

        // 1. Text Area
        // Title (Bold, Large)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = if (isSmallLabel) 20f else 28f
        val titleLines = wrapText(title, paint, textWidth)
        for (line in titleLines) {
            // Stop if we encroach on QR area in portrait mode
            if (isPortrait && currentY + paint.textSize > qrY) break
            // Stop if we encroach on bottom in landscape
            if (!isPortrait && currentY + paint.textSize > height) break
            
            canvas.drawText(line, textX, currentY + paint.textSize, paint)
            currentY += paint.textSize + 4f
        }

        // Subtitle (Normal, Small)
        currentY += 4f
        paint.typeface = Typeface.MONOSPACE
        paint.textSize = if (isSmallLabel) 14f else 18f
        val subLines = wrapText(subtitle, paint, textWidth)
        for (line in subLines) {
            if (isPortrait && currentY + paint.textSize > qrY) break
            if (!isPortrait && currentY + paint.textSize > height) break
            
            canvas.drawText(line, textX, currentY + paint.textSize, paint)
            currentY += paint.textSize + 2f
        }
        
        // 2. Draw QR Code
        val qrBitmap = createQrCode(qrContent, qrSize)
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, qrX, qrY, null)
        }
        
        // 3. Icon (Optional)
        if (iconType != null) {
            val iconSize = if (isSmallLabel) 16f else 24f
            // Place icon at the bottom of the text area
            val iconX = padding
            val iconY = if (isPortrait) qrY - iconSize - 4f else height - iconSize - padding
            
            if (iconY > currentY) { // Only draw if we haven't already filled the space with text
                drawIcon(canvas, iconType, iconX, iconY, iconSize, paint)
            }
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
            "christmas" -> {
                // Simple Tree
                canvas.drawLine(x + size/2, y, x, y + size, paint)
                canvas.drawLine(x + size/2, y, x + size, y + size, paint)
                canvas.drawLine(x, y + size, x + size, y + size, paint)
            }
            "halloween" -> {
                // Simple Pumpkin (Circle with stem)
                canvas.drawCircle(x + size/2, y + size/2 + 2, size/2 - 2, paint)
                canvas.drawLine(x + size/2, y, x + size/2, y + 4, paint)
            }
            "easter" -> {
                // Simple Egg (Oval)
                canvas.drawOval(x + 2, y, x + size - 2, y + size, paint)
            }
        }
        paint.style = prevStyle
    }
}
