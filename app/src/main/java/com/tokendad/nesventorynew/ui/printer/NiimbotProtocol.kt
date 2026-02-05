package com.tokendad.nesventorynew.ui.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor
import kotlin.text.Charsets

/**
 * Protocol variants for different Niimbot printer families.
 */
enum class ProtocolVariant {
    V5,          // D11-H, D110, D101, B21 (9-byte StartPrint, 13-byte Dimension)
    B1_CLASSIC   // B1 (7-byte StartPrint, 6-byte SetPageSize, requires SetLabelType)
}

/**
 * Supported Niimbot printer models with their specifications.
 */
enum class PrinterModel(
    val displayName: String,
    val width: Int,           // Print width in pixels
    val dpi: Int,             // Resolution
    val maxWidthMm: Int,      // Max label width in mm
    val defaultHeightPx: Int, // Default label height in pixels (based on common label sizes)
    val protocol: ProtocolVariant
) {
    // D-Series (narrow labels - 15mm, typically 40mm length)
    // 40mm @ 300dpi = 472px, 40mm @ 203dpi = 320px
    D11_H("D11-H", 96, 300, 15, 472, ProtocolVariant.V5),
    D110("D110", 96, 203, 15, 320, ProtocolVariant.V5),

    // D-Series (wider - 25mm)
    D101("D101", 96, 203, 25, 320, ProtocolVariant.V5),

    // B-Series (wide labels - 50mm x 30mm typical)
    // 30mm @ 203dpi = 240px
    B1("B1", 384, 203, 50, 240, ProtocolVariant.B1_CLASSIC),
    B21("B21", 384, 203, 50, 240, ProtocolVariant.V5);

    companion object {
        fun fromString(name: String): PrinterModel? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}

object NiimbotProtocol {

    private const val HEAD = 0x55
    private const val TAIL = 0xAA

    // Command Types (Niimbot Protocol)
    private const val CMD_CONNECT = 0xC1
    private const val CMD_SET_DENSITY = 0x21
    private const val CMD_SET_LABEL_TYPE = 0x23
    private const val CMD_SET_DIMENSION = 0x13
    private const val CMD_PRINT_START = 0x01
    private const val CMD_PAGE_START = 0x03
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
     * Creates a SetLabelType packet (required for B1 protocol).
     */
    fun createSetLabelTypePacket(type: Int = 1): ByteArray {
        return createPacket(CMD_SET_LABEL_TYPE, byteArrayOf(type.toByte()))
    }

    /**
     * Converts a bitmap to Niimbot print commands based on printer model's protocol.
     */
    fun createPrintData(bitmap: Bitmap, model: PrinterModel, density: Int = 3): List<ByteArray> {
        return when (model.protocol) {
            ProtocolVariant.V5 -> createV5PrintData(bitmap, model, density)
            ProtocolVariant.B1_CLASSIC -> createB1PrintData(bitmap, model, density)
        }
    }

    /**
     * V5 Protocol implementation for D11-H, D110, D101, B21.
     * - 9-byte StartPrint payload
     * - 13-byte SetDimension payload
     * - PrintStatus (0xA5) before image data
     */
    private fun createV5PrintData(bitmap: Bitmap, model: PrinterModel, density: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        val width = model.width
        val height = bitmap.height
        val bytesPerRow = width / 8

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

        // 3. Set Dimension (13 bytes)
        // [Rows(2), Cols(2), Copies(2), CutH(2), CutType(1), Pad(1), SendAll(1), PartH(2)]
        val dimPayload = ByteBuffer.allocate(13).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(height.toShort())
            putShort(width.toShort())
            putShort(1) // Copies
            putShort(0) // Cut Height
            put(0) // Cut Type
            put(0) // Pad
            put(1) // Send All
            putShort(0) // Part Height
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, dimPayload))

        // 3a. Print Status (0xA5) - Required for V5 flow
        packets.add(createPacket(CMD_PRINT_STATUS, byteArrayOf(0x01)))

        // 4. Image Data
        packets.addAll(encodeImageRows(bitmap, width, bytesPerRow))

        // 5. End Sequence
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))

        return packets
    }

    /**
     * B1 Classic Protocol implementation for B1 printer.
     * - 7-byte StartPrint payload
     * - 6-byte SetPageSize payload (rows, cols, qty)
     * - Requires SetLabelType before printing
     * - No rotation needed (verified from backend implementation)
     * - Uses simple row encoding with zeros for chunk counts
     * Based on verified implementation from /data/NesVentory/backend/app/niimbot/printer.py
     */
    private fun createB1PrintData(bitmap: Bitmap, model: PrinterModel, density: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()

        // No rotation for B1 - backend confirms "no rotation gives correct orientation"
        val width = bitmap.width
        val height = bitmap.height
        val bytesPerRow = (width + 7) / 8  // Round up for B1

        // 1. SetDensity
        packets.add(createSetDensityPacket(density))

        // 2. SetLabelType (required for B1)
        packets.add(createPacket(CMD_SET_LABEL_TYPE, byteArrayOf(0x01)))

        // 3. Print Start (7 bytes) - B1 Classic format
        // Payload: 00 01 00 00 00 00 00
        val startPayload = byteArrayOf(0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00)
        packets.add(createPacket(CMD_PRINT_START, startPayload))

        // 4. Page Start
        packets.add(createPacket(CMD_PAGE_START, byteArrayOf(0x01)))

        // 5. Set Page Size (6 bytes) - B1 uses (height, width, qty)
        val sizePayload = ByteBuffer.allocate(6).apply {
            order(ByteOrder.BIG_ENDIAN)
            putShort(height.toShort())  // rows = height
            putShort(width.toShort())   // cols = width
            putShort(1)                 // quantity
        }.array()
        packets.add(createPacket(CMD_SET_DIMENSION, sizePayload))

        // 6. Image Data - B1 uses simple encoding with zeros for chunk counts
        packets.addAll(encodeB1ImageRows(bitmap, width, height, bytesPerRow))

        // 7. End Page
        packets.add(createPacket(CMD_END_PAGE_PRINT, byteArrayOf(0x01)))

        return packets
    }

    /**
     * Encodes bitmap rows for B1 printer using simple encoding.
     * Backend uses: header = struct.pack(">H B B B B", y, 0, 0, 0, 1)
     * This means: row_number(2 bytes), 0, 0, 0, repeat_count(1)
     */
    private fun encodeB1ImageRows(bitmap: Bitmap, width: Int, height: Int, bytesPerRow: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()

        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)

            // Convert row to 1-bit per pixel (threshold at 128)
            for (x in 0 until width) {
                if (x < bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    // Check if pixel is dark (should print)
                    val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    if (Color.alpha(pixel) > 128 && gray < 128) {
                        val byteIndex = x / 8
                        val bitIndex = 7 - (x % 8)
                        pixelData[byteIndex] = (pixelData[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                    }
                }
            }

            // B1 simple encoding: header is [row_hi, row_lo, 0, 0, 0, 1] + pixel_data
            // Always send as bitmap row (0x85), even for empty rows
            val rowPayload = ByteBuffer.allocate(6 + bytesPerRow).apply {
                order(ByteOrder.BIG_ENDIAN)
                putShort(y.toShort())  // Row number
                put(0)                  // Count 0 (not used in simple mode)
                put(0)                  // Count 1 (not used in simple mode)
                put(0)                  // Count 2 (not used in simple mode)
                put(1)                  // Repeat count
                put(pixelData)
            }.array()
            packets.add(createPacket(CMD_PRINT_BITMAP_ROW, rowPayload))
        }

        return packets
    }

    /**
     * Encodes bitmap rows into print packets for V5 protocol.
     * Uses chunk-based encoding with calculated byte counts.
     */
    private fun encodeImageRows(bitmap: Bitmap, width: Int, bytesPerRow: Int): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        val height = bitmap.height
        val chunkSize = bytesPerRow / 3

        for (y in 0 until height) {
            val pixelData = ByteArray(bytesPerRow)

            // Render Row - center the bitmap if narrower than print width
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

            // Calculate Byte Counts (B1..B3) for split mode header
            var b1 = 0; var b2 = 0; var b3 = 0
            for (i in 0 until bytesPerRow) {
                if (pixelData[i] != 0.toByte()) {
                    if (i < chunkSize) b1++
                    else if (i < chunkSize * 2) b2++
                    else b3++
                }
            }

            if (b1 + b2 + b3 == 0) {
                // Empty row
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