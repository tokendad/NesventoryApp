package com.tokendad.nesventorynew.ui.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor
import kotlin.text.Charsets

enum class PrinterModel(val width: Int, val dpi: Int) {
    D110M_V4(96, 300) // Niimbot D11-H using V4 protocol (96px width, 300 DPI)
}

object NiimbotProtocol {

    private const val HEAD = 0x55
    private const val TAIL = 0xAA

    // Command Types (D110M_V4 Protocol)
    private const val CMD_CONNECT = 0xC1
    private const val CMD_SET_DENSITY = 0x21
    private const val CMD_SET_LABEL_TYPE = 0x23
    private const val CMD_SET_DIMENSION = 0x13
    private const val CMD_PRINT_START = 0x01
    private const val CMD_PRINT_BITMAP_ROW = 0x85
    private const val CMD_PRINT_EMPTY_ROW = 0x84
    private const val CMD_END_PAGE_PRINT = 0xE3
    private const val CMD_PRINT_END = 0xF3
    private const val CMD_PRINT_STATUS = 0xA5
    private const val CMD_HEARTBEAT = 0xDC
    private const val CMD_GET_RFID = 0x1A

    data class RfidInfo(
        val uuid: String,
        val barcode: String,
        val serialNumber: String,
        val totalPaper: Int,
        val usedPaper: Int,
        val type: Int
    )

    fun createConnectPacket(): ByteArray {
        val packet = createPacket(CMD_CONNECT, byteArrayOf(0x01))
        // V5/V4 Handshake requires 0x03 prefix
        val out = ByteArray(packet.size + 1)
        out[0] = 0x03
        packet.copyInto(out, 1)
        return out
    }

    fun createGetRfidPacket(): ByteArray {
        // 0x01 = PrinterInfoType.Rfid (from niimbluelib)
        return createPacket(CMD_GET_RFID, byteArrayOf(0x01))
    }

    fun parseRfidResponse(packet: ByteArray): RfidInfo? {
        // Basic Packet Validation
        if (packet.size < 5 || packet[0] != HEAD.toByte() || packet[1] != HEAD.toByte()) return null
        
        // Find Command and Data Length
        // Header: 55 55 Type Len Data... Check AA AA
        val type = packet[2].toInt() and 0xFF
        val len = packet[3].toInt() and 0xFF
        
        // 0x1B is the response to 0x1A
        if (type != 0x1B && type != CMD_GET_RFID) return null 
        
        if (packet.size < 4 + len) return null
        val data = packet.copyOfRange(4, 4 + len)
        
        // Best-effort parsing based on niimbluelib/reverse engineering
        // The data payload likely contains the raw RFID tag memory blocks (4 bytes each).
        // Block 7-8: UUID (8 bytes) -> Offset 28
        // Block 9-12: Serial (16 bytes ASCII) -> Offset 36
        // Block 15-16: Barcode (8 bytes?) -> Offset 60
        
        try {
            var uuid = ""
            var serial = ""
            var barcode = ""
            
            // 1. UUID (Offset 28, 8 bytes)
            if (data.size >= 36) { // Need at least up to byte 35
                uuid = data.copyOfRange(28, 36).joinToString("") { "%02X".format(it) }
            }
            
            // 2. Serial Number (Offset 36, 16 bytes)
            if (data.size >= 52) {
                val serialBytes = data.copyOfRange(36, 52)
                serial = serialBytes.filter { it in 32..126 }.toByteArray().toString(Charsets.US_ASCII).trim()
            }
            
            // 3. Barcode (Offset 60, 8 bytes or more?)
            if (data.size >= 68) {
                 val barcodeBytes = data.copyOfRange(60, 68) // Assuming 2 blocks
                 barcode = barcodeBytes.filter { it in 0x30.toByte()..0x39.toByte() }.toByteArray().toString(Charsets.US_ASCII)
            }
            
            // Fallback if structured parsing failed to find anything useful
            if (serial.isEmpty()) {
                 serial = data.filter { it in 32..126 }.toByteArray().toString(Charsets.US_ASCII).trim()
            }
            
            return RfidInfo(
                uuid = uuid,
                barcode = barcode, 
                serialNumber = serial,
                totalPaper = 0, // specific offsets needed
                usedPaper = 0,  
                type = 1
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun createHeartbeatPacket(): ByteArray {
        return createPacket(CMD_HEARTBEAT, byteArrayOf(0x01))
    }

    fun createSetDensityPacket(density: Int): ByteArray {
        return createPacket(CMD_SET_DENSITY, byteArrayOf(density.toByte()))
    }

    fun createPrintEndPacket(): ByteArray {
        return createPacket(CMD_PRINT_END, byteArrayOf(0x01))
    }

    /**
     * Converts a bitmap to Niimbot print commands for D110M_V4 protocol.
     */
    fun createPrintData(bitmap: Bitmap, model: PrinterModel, density: Int = 3): List<ByteArray> {
        // VERIFIED VIA USB (2026-01-01):
        // Protocol V4/V5 (Niimbot D11_H / D110M)
        // - Width: 96px (Standard Density, 203 DPI)
        // - Start Print: 9-byte Payload
        // - Set Dimension: 13-byte Payload
        // - Row Data: 0x85 with Split Counts [RowH, RowL, B1, B2, B3, Rep]
        // - Empty Row: 0x84 [RowH, RowL, Rep] works perfectly.
        // - B1, B2, B3 are counts of non-zero BYTES in each 4-byte (32px) chunk.
        
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
            put(1) // Send All (Updated to 1)
            putShort(0) // Part Height
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, dimPayload))

        // 3a. Print Status (0xA5) - Required for D110M_V4 flow instead of StartPage
        packets.add(createPacket(CMD_PRINT_STATUS, byteArrayOf(0x01)))

        // 4. Image Data
        val chunkSize = bytesPerRow / 3 // 4 bytes
        
        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)
            
            // Render Row
            val xOffset = (width - bitmap.width) / 2
            for (x in 0 until width) {
                val sourceX = x - xOffset
                if (sourceX in 0 until bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(sourceX, y)
                    if (Color.alpha(pixel) > 128 && (Color.red(pixel) < 128 || Color.green(pixel) < 128 || Color.blue(pixel) < 128)) {
                        val byteIndex = x / 8
                        val bitIndex = 7 - (x % 8)
                        pixelData[byteIndex] = (pixelData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                    }
                }
            }
            
            // Calculate Byte Counts (B1..B3)
            var b1 = 0; var b2 = 0; var b3 = 0
            for (i in 0 until bytesPerRow) {
                if (pixelData[i] != 0.toByte()) {
                    if (i < chunkSize) b1++
                    else if (i < chunkSize * 2) b2++
                    else b3++
                }
            }
            
            if (b1 + b2 + b3 == 0) {
                 val rowPayload = ByteBuffer.allocate(3).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(1) // Repeats
                }.array()
                packets.add(createPacket(CMD_PRINT_EMPTY_ROW, rowPayload))
            } else {
                // Header: [RowH, RowL, B1, B2, B3, Repeats]
                val rowPayload = ByteBuffer.allocate(6 + bytesPerRow).apply {
                    order(ByteOrder.BIG_ENDIAN)
                    putShort(y.toShort())
                    put(b1.toByte())
                    put(b2.toByte())
                    put(b3.toByte())
                    put(1) // Repeats
                    put(pixelData)
                }.array()
                packets.add(createPacket(CMD_PRINT_BITMAP_ROW, rowPayload))
            }
        }

        // 5. End Sequence
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))
        // NOTE: CMD_PRINT_END is omitted here. It MUST be sent after the print is physically complete.
        // The caller (ViewModel) is responsible for waiting and sending CMD_PRINT_END (0xF3).

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