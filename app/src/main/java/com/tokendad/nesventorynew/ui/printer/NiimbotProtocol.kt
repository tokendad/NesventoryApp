package com.tokendad.nesventorynew.ui.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor

enum class PrinterModel(val width: Int, val dpi: Int) {
    D110(96, 203), // Standard D110
    D11_H(142, 300), // Standard D11_H
    D110M_V4(96, 203) // D11_H pretending to be D110M
}

object NiimbotProtocol {

    private const val HEAD = 0x55
    private const val TAIL = 0xAA

    // Command Types
    private const val CMD_CONNECT = 0xC1
    private const val CMD_SET_DENSITY = 0x21
    private const val CMD_SET_LABEL_TYPE = 0x23
    private const val CMD_SET_DIMENSION = 0x13 
    private const val CMD_PRINT_START = 0x01 
    private const val CMD_PRINT_BITMAP_ROW = 0x85 
    private const val CMD_PRINT_EMPTY_ROW = 0x84
    private const val CMD_PRINT_CLEAR = 0x20
    private const val CMD_START_PAGE_PRINT = 0x03
    private const val CMD_END_PAGE_PRINT = 0xE3
    private const val CMD_PRINT_END = 0xF3
    private const val CMD_SET_QUANTITY = 0x15
    private const val CMD_HEARTBEAT = 0xDC
    private const val CMD_STATUS = 0xA0

    fun createConnectPacket(): ByteArray {
        val packet = createPacket(CMD_CONNECT, byteArrayOf(0x01))
        // V5/V4 Handshake requires 0x03 prefix
        val out = ByteArray(packet.size + 1)
        out[0] = 0x03
        packet.copyInto(out, 1)
        return out
    }

    fun createHeartbeatPacket(): ByteArray {
        return createPacket(CMD_HEARTBEAT, byteArrayOf(0x01))
    }

    fun createSetDensityPacket(density: Int): ByteArray {
        return createPacket(CMD_SET_DENSITY, byteArrayOf(density.toByte()))
    }

    /**
     * Converts a bitmap to Niimbot print commands based on the printer model.
     */
    fun createPrintData(bitmap: Bitmap, model: PrinterModel, density: Int = 3): List<ByteArray> {
        return when (model) {
            PrinterModel.D11_H -> createPrintDataD11H(bitmap, density)
            PrinterModel.D110 -> createPrintDataD110(bitmap, density)
            PrinterModel.D110M_V4 -> createPrintDataD110MV4(bitmap, density)
        }
    }

    private fun createPrintDataD110MV4(bitmap: Bitmap, density: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        val width = 96
        val height = bitmap.height
        val bytesPerRow = 12 // 96 / 8
        
        // 1. Preparation
        packets.add(createSetDensityPacket(density))
        packets.add(createPacket(CMD_SET_LABEL_TYPE, byteArrayOf(0x01)))
        
        // 2. Print Start (9 bytes)
        // [Pages(2), TaskID(4), Color(1), Quality(1), Flag(1)]
        val startPayload = ByteBuffer.allocate(9).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(1) // Total Pages
            putInt(0)   // Task ID
            put(0)      // Color
            put(1)      // Quality
            put(0)      // Flag
        }.array()
        packets.add(createPacket(CMD_PRINT_START, startPayload))
        
        // 3. Set Dimension
        // [Rows(2), Cols(2), Copies(2), CutH(2), CutType(1), Pad(1), SendAll(1), PartH(2)]
        val dimPayload = ByteBuffer.allocate(13).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(height.toShort()) 
            putShort(width.toShort())  
            putShort(1) // Copies
            putShort(0) // Cut Height
            put(0) // Cut Type
            put(0) // Pad
            put(0) // Send All
            putShort(0) // Part Height
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, dimPayload))

        // 4. Image Data (Split Counts)
        val chunkSize = bytesPerRow / 3 // 4 bytes
        
        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)
            var c1 = 0
            var c2 = 0
            var c3 = 0
            
            // Center the image
            val xOffset = (width - bitmap.width) / 2
            
            for (x in 0 until width) {
                val sourceX = x - xOffset
                if (sourceX in 0 until bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(sourceX, y)
                    // Check for black
                    if (Color.alpha(pixel) > 128 && (Color.red(pixel) < 128 || Color.green(pixel) < 128 || Color.blue(pixel) < 128)) {
                        // Set bit
                        val byteIndex = x / 8
                        val bitIndex = 7 - (x % 8)
                        pixelData[byteIndex] = (pixelData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                        
                        // Increment Chunk Count
                        if (byteIndex < chunkSize) c1++
                        else if (byteIndex < chunkSize * 2) c2++
                        else c3++
                    }
                }
            }
            
            val totalBlack = c1 + c2 + c3
            if (totalBlack == 0) {
                 val rowPayload = ByteBuffer.allocate(3).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(1) // Repeats
                }.array()
                packets.add(createPacket(CMD_PRINT_EMPTY_ROW, rowPayload))
            } else {
                // Header: [RowH, RowL, C1, C2, C3, Repeats]
                val rowPayload = ByteBuffer.allocate(6 + bytesPerRow).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(c1.toByte())
                    put(c2.toByte())
                    put(c3.toByte())
                    put(1) // Repeats
                    put(pixelData)
                }.array()
                packets.add(createPacket(CMD_PRINT_BITMAP_ROW, rowPayload))
            }
        }

        // 5. End Sequence
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))
        packets.add(createPacket(CMD_PRINT_END, byteArrayOf(0x01)))

        return packets
    }

    private fun createPrintDataD110(bitmap: Bitmap, density: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        val width = PrinterModel.D110.width
        val height = bitmap.height
        val bytesPerRow = width / 8
        
        // 1. Preparation
        // D110/V4 Sequence: Density -> LabelType -> Start -> Dimension -> Data
        packets.add(createSetDensityPacket(density))
        packets.add(createPacket(CMD_SET_LABEL_TYPE, byteArrayOf(0x01)))
        
        // 2. Print Start (9 bytes)
        // Log: 01 09 00 01 00 00 00 00 00 01 00
        val startPayload = ByteBuffer.allocate(9).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(1) // Total Pages
            putInt(0)   // Task ID / Reserved
            put(0)      // Color
            put(1)      // Quality (from log)
            put(0)      // Flag
        }.array()
        packets.add(createPacket(CMD_PRINT_START, startPayload))
        
        // 3. Set Dimension
        // Log: 13 0d 00 f0 00 60 00 01 00 00 00 00 00 00 00 00 00
        val dimPayload = ByteBuffer.allocate(13).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(height.toShort()) 
            putShort(width.toShort())  
            putShort(1) // Copies
            putShort(0) // Cut Height
            put(0) // Cut Type
            put(0) // Pad
            put(0) // Send All
            putShort(0) // Part Height
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, dimPayload))

        // 4. Image Data
        // D110 seems to accept simple 0x85 packets or 0x84 for empty rows.
        // We will process the bitmap to fit 96px width.
        
        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)
            var blackPixelCount = 0
            
            // Center the image if it's smaller than 96px, or crop if larger
            val xOffset = (width - bitmap.width) / 2
            
            for (x in 0 until width) {
                val sourceX = x - xOffset
                if (sourceX in 0 until bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(sourceX, y)
                    // Check for black (non-transparent, dark)
                    if (Color.alpha(pixel) > 128 && (Color.red(pixel) < 128 || Color.green(pixel) < 128 || Color.blue(pixel) < 128)) {
                        blackPixelCount++
                        val byteIndex = x / 8
                        val bitIndex = 7 - (x % 8)
                        pixelData[byteIndex] = (pixelData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                    }
                }
            }
            
            if (blackPixelCount == 0) {
                val rowPayload = ByteBuffer.allocate(3).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(1) // Repeats
                }.array()
                packets.add(createPacket(CMD_PRINT_EMPTY_ROW, rowPayload))
            } else {
                // Use Standard 0x85: Row(2) + Data(12)
                // If this fails, we might need the "Mystery" header from the log.
                // But standard Niimbot usually accepts this.
                val rowPayload = ByteBuffer.allocate(2 + bytesPerRow).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(pixelData)
                }.array()
                packets.add(createPacket(CMD_PRINT_BITMAP_ROW, rowPayload))
            }
        }

        // 5. End Sequence
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))
        packets.add(createPacket(CMD_PRINT_END, byteArrayOf(0x01)))

        return packets
    }

    private fun createPrintDataD11H(bitmap: Bitmap, density: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        
        // D11_H native specs (300 DPI)
        val width = PrinterModel.D11_H.width
        val height = bitmap.height
        val bytesPerRow = 18 
        
        // 1. Preparation
        packets.add(createPacket(CMD_PRINT_CLEAR, byteArrayOf(0x01)))
        packets.add(createSetDensityPacket(density))
        packets.add(createPacket(CMD_SET_LABEL_TYPE, byteArrayOf(0x01)))
        
        // 2. Set Dimension FIRST (Crucial for V5)
        // [Rows(2), Cols(2), Copies(2), CutH(2), CutType(1), 0x00, SendAll(1), PartH(2)]
        val dimPayload = ByteBuffer.allocate(13).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(height.toShort()) 
            putShort(width.toShort())  
            putShort(1) // Copies
            putShort(0) // Cut Height
            put(0) // Cut Type
            put(0) // Pad
            put(0) // Send All (0 worked in verification)
            putShort(0) // Part Height
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, dimPayload))

        // 3. Print Start (9 bytes payload)
        val startPayload = ByteBuffer.allocate(9).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(1) // Total Pages
            put(0); put(0); put(0); put(0)
            put(0) // Color
            put(0) // Quality
            put(0) // Flag
        }.array()
        packets.add(createPacket(CMD_PRINT_START, startPayload))
        
        // 4. Image Data (Rows)
        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)
            var blackPixelCount = 0
            
            val xOffset = (width - bitmap.width) / 2

            for (x in 0 until width) {
                val sourceX = x - xOffset
                if (sourceX in 0 until bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(sourceX, y)
                     if (Color.alpha(pixel) > 128 && (Color.red(pixel) < 128 || Color.green(pixel) < 128 || Color.blue(pixel) < 128)) {
                        blackPixelCount++
                        val byteIndex = x / 8
                        val bitIndex = 7 - (x % 8)
                        if (byteIndex < bytesPerRow) {
                            pixelData[byteIndex] = (pixelData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                        }
                    }
                }
            }
            
            if (blackPixelCount == 0) {
                val rowPayload = ByteBuffer.allocate(3).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(1) // Repeats
                }.array()
                packets.add(createPacket(CMD_PRINT_EMPTY_ROW, rowPayload))
            } else {
                // Header: [RowH, RowL, 0 (Total Mode), CountL, CountH, Repeats]
                val rowPayload = ByteBuffer.allocate(6 + bytesPerRow).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(0) // Total mode flag
                    put((blackPixelCount and 0xFF).toByte())
                    put(((blackPixelCount shr 8) and 0xFF).toByte())
                    put(1) // Repeats
                    put(pixelData)
                }.array()
                packets.add(createPacket(CMD_PRINT_BITMAP_ROW, rowPayload))
            }
        }
        
        // 5. End Sequence
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))
        packets.add(createPacket(CMD_PRINT_END, byteArrayOf(0x01)))
        
        return packets
    }

    private fun createPacket(type: Int, data: ByteArray): ByteArray {
        val stream = ByteArrayOutputStream()
        stream.write(HEAD)
        stream.write(HEAD)
        stream.write(type)
        stream.write(data.size)
        stream.write(data)
        
        var checksum = type.toByte()
        checksum = checksum xor data.size.toByte()
        for (b in data) {
            checksum = checksum xor b
        }
        stream.write(checksum.toInt())
        stream.write(TAIL)
        stream.write(TAIL)
        
        return stream.toByteArray()
    }
}