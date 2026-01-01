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
        iconType: String? = null
    ): Bitmap {
        // Determine layout orientation
        // If height > width (e.g. 12mm x 40mm strip), we must draw horizontally and rotate.
        val needRotation = height > width
        
        val drawWidth = if (needRotation) height else width
        val drawHeight = if (needRotation) width else height
        
        val bitmap = Bitmap.createBitmap(drawWidth, drawHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val isSmallLabel = drawHeight < 150
        val padding = if (isSmallLabel) 4f else 8f
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // Layout: QR Left, Text Right (becomes QR Top, Text Bottom after 90deg rotation)
        val qrSize = drawHeight - (padding * 2).toInt()
        val qrX = padding
        val qrY = padding
        
        val textX = qrX + qrSize + padding
        val textWidth = drawWidth - textX - padding
        var currentY = padding

        // 1. Draw QR Code
        val qrBitmap = createQrCode(qrContent, qrSize)
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, qrX, qrY, null)
        }

        // 2. Text Area
        if (textWidth > 0) {
            // Title
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = if (isSmallLabel) 20f else 28f
            val titleLines = wrapText(title, paint, textWidth)
            for (line in titleLines) {
                if (currentY + paint.textSize > drawHeight) break
                canvas.drawText(line, textX, currentY + paint.textSize, paint)
                currentY += paint.textSize + 4f
            }

            // Subtitle
            currentY += 4f
            paint.typeface = Typeface.MONOSPACE
            paint.textSize = if (isSmallLabel) 14f else 18f
            val subLines = wrapText(subtitle, paint, textWidth)
            for (line in subLines) {
                if (currentY + paint.textSize > drawHeight) break
                canvas.drawText(line, textX, currentY + paint.textSize, paint)
                currentY += paint.textSize + 2f
            }
            
            // 3. Icon
            if (iconType != null) {
                val iconSize = if (isSmallLabel) 16f else 24f
                // Place icon at bottom right of text area
                val iconX = drawWidth - iconSize - padding
                val iconY = drawHeight - iconSize - padding
                
                // Only draw if not overlapping text vertically (simple check)
                // or just draw it anyway as overlay/watermark style if tight
                drawIcon(canvas, iconType, iconX, iconY, iconSize, paint)
            }
        }

        return if (needRotation) {
            rotateBitmap(bitmap, 90f)
        } else {
            bitmap
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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
