# Niimbot Printer Integration Guide

## Overview
NesVentory integrates natively with Niimbot Bluetooth thermal printers to allow on-the-go printing of item labels. The implementation handles proprietary communication protocols, including image rasterization and RFID tag reading.

## Supported Hardware
| Model | Protocol | DPI | Width | Notes |
|-------|----------|-----|-------|-------|
| **D110** | V4 | 203 | 96px | Standard model. Uses packet `0x83`/`0x85`. |
| **D11_H** | V5 | 300 | 142px | High-res model. Requires Dimension command *before* PrintStart. |
| **D11** | V4 | 203 | 96px | Treated as D110. |

## Protocol Implementation

### Core Commands
- **Connect (`0xC1`)**: Handshake packet. V5 requires `0x03` prefix.
- **Set Density (`0x21`)**: Sets print darkness (1-5).
- **Set Dimension (`0x13`)**: Defines print area.
- **Print Start (`0x01`)**: Begins a print job.
- **Heartbeat (`0xDC`)**: Keep-alive signal.
- **Get RFID (`0x1A`)**: Requests consumable info.

### Image Printing
Images are converted to 1-bit monochrome bitmaps.
- **D110**: Sent as 12-byte rows (96 pixels).
- **D11_H**: Sent as 18-byte rows (142 pixels) with specific "population count" headers for density control.

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
