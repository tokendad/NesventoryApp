# Niimbot Printer Integration Guide

## Overview
NesVentory integrates natively with Niimbot Bluetooth thermal printers to allow on-the-go printing of item labels. The implementation handles proprietary communication protocols, including image rasterization and RFID tag reading.

## Supported Hardware
| Model | Protocol | DPI | Width | Notes |
|-------|----------|-----|-------|-------|
| **D11-H** | D110M_V4 | 300 | 96px | Uses V4-style packet format (`0x85` for rows). |

## Protocol Implementation

### Core Commands
- **Connect (`0xC1`)**: Handshake packet. V5 requires `0x03` prefix.
- **Set Density (`0x21`)**: Sets print darkness (1-5).
- **Set Dimension (`0x13`)**: Defines print area.
- **Print Start (`0x01`)**: Begins a print job.
- **Heartbeat (`0xDC`)**: Keep-alive signal.
- **Get RFID (`0x1A`)**: Requests consumable info.

### Image Printing
Images are converted to 1-bit monochrome bitmaps and sent as 12-byte rows (96 pixels) with byte count headers for each 32px chunk.

### RFID Reading
The application requests tag info using `0x1A`. The printer responds with `0x1B`, which contains the raw memory dump of the tag.
- **UUID**: Offset 28 (8 bytes).
- **Serial Number**: Offset 36 (16 bytes, ASCII).
- **Barcode**: Offset 60 (8 bytes).

## Architecture
- **`BluetoothPrinterManager`**: Handles low-level GATT operations, service discovery, and packet transmission. Exposes `receivedData` Flow.
- **`NiimbotProtocol`**: Pure Kotlin object for packet construction and parsing. Contains protocol constants and byte logic.
- **`PrinterViewModel`**: Manages UI state, handles connection logic, and orchestrates the print flow.

## Debugging
Connect the device via USB and use `adb logcat -s BluetoothPrinterManager NiimbotProtocol` to view packet transmission logs.
