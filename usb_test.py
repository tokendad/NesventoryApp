import serial
import time
import struct

PORT = "/dev/ttyACM0"
BAUDRATE = 115200

def create_packet(cmd, data):
    data_len = len(data)
    checksum = cmd ^ data_len
    for b in data: checksum ^= b
    return bytes([0x55, 0x55, cmd, data_len, *data, checksum, 0xAA, 0xAA])

def main():
    try:
        ser = serial.Serial(PORT, BAUDRATE, timeout=1)
        print(f"Connected to {PORT}")
    except Exception as e:
        print(f"Failed to connect: {e}")
        return

    # 1. Connect
    ser.write(create_packet(0xC1, [0x01]))
    time.sleep(0.5)

    # 2. Setup
    ser.write(create_packet(0x21, [0x03])) # Density 3
    ser.write(create_packet(0x23, [0x01])) # Type 1
    time.sleep(0.2)

    # 3. Print Start (9-byte)
    # Log: 01 09 00 01 00 00 00 00 00 01 00
    start_payload = struct.pack(">H I B B B", 1, 0, 0, 1, 0)
    ser.write(create_packet(0x01, start_payload))
    time.sleep(0.5)

    # 4. Set Dimension (96x100)
    width = 96
    height = 100
    dim_payload = struct.pack(">HHHHBBBH", height, width, 1, 0, 0, 0, 0, 0)
    ser.write(create_packet(0x13, dim_payload))
    time.sleep(0.2)

    # 5. Image Data (0x83 Indexed)
    print("Sending Image Data (0x83 Indexed)...")
    
    # 0x83 Payload:
    # Header: [RowH, RowL, C1, C2, C3, Repeats] (Same 6-byte Split Header)
    # Data: List of INDICES (1-based) of bytes that are non-zero.
    # Pattern: Solid line (All bytes non-zero).
    # Width 96 = 12 bytes. Indices: 1, 2, 3, ... 12.
    
    # Calculate counts:
    # Chunk 1 (Bytes 0-3): 4 bytes set -> C1=4
    # Chunk 2 (Bytes 4-7): 4 bytes set -> C2=4
    # Chunk 3 (Bytes 8-11): 4 bytes set -> C3=4
    c1, c2, c3 = 4, 4, 4
    
    # Indices: 1..12
    indices = bytes(range(0, 12)) # 0x00..0x0B (Wait, log uses specific values?)
    # Log: 55 55 83 0c 00 3c 00 03 00 08 00 3d 00 3e 00 3f 84 aa aa
    # Header: 00 3c (Row), 00 (C1), 03 (C2), 00 (C3), 08 (Rep - Wait, 08 is not rep?)
    # Wait.
    # 0x83 Header is DIFFERENT.
    # Log: 83 0c (len 12)
    # 00 3c (Row)
    # 00 03 00 08 (Counts? Or something else?)
    # 00 3d 00 3e 00 3f (Data?)
    
    # Let's try sending a simple 0x85 packet but with VERY basic payload to ensure we aren't messing up the header.
    # But wait, we've tried 0x85 Split and Total.
    
    # Let's try 0x83 with a KNOWN format from niimbluelib.
    # niimbluelib: `PrintBitmapRowIndexed`:
    # `[...Utils.u16ToBytes(pos), ...counts.parts, repeats, ...indexes]`
    # Header is IDENTICAL to 0x85.
    # Data is indexes.
    
    # My hypothesis: 0x85 failed because `counts` were wrong?
    # In my 0x85 Split test, I used c1=32 (pixels), c2=32, c3=32.
    # But niimbluelib counts PIXELS, not bytes.
    # 0xFF * 4 bytes = 32 pixels. Correct.
    
    # Is it possible `Repeats` is not supported?
    
    for y in range(height):
        # 0x83 Header: [RowH, RowL, C1, C2, C3, Repeats]
        # Counts are BYTES or PIXELS?
        # niimbluelib says: `countPixelsForBitmapPacket` returns total/parts PIXEL count.
        # But `PrintBitmapRowIndexed` checks `counts.total > 6`.
        # "Printer powers off if black pixel count > 6" for Indexed??
        # This implies 0x83 is only for sparse lines.
        
        # So I shouldn't use 0x83 for a solid line.
        pass

    # New Strategy: Test 4 - SIMPLEST POSSIBLE PACKET
    # 0x85, Total Mode, No Repeats, Small Chunk.
    # Try sending just 1 byte of data?
    
    # Better: Replicate the LOG packet exactly.
    # Log: 55 55 85 12 00 44 00 08 00 01 00 00 00 00 00 3f c0 00 00 00 00 00 25 aa aa
    # Row: 00 44 (68)
    # Hdr: 00 08 00 01 (Total Mode: 00, 08=CountL, 00=CountH, 01=Rep)
    # Data: 12 bytes.
    
    print("Replicating exact LOG packet...")
    row_bytes = 12
    data = bytes([0,0,0,0,0,0x3f,0xc0,0,0,0,0,0]) # 8 pixels
    
    for y in range(height):
        # Header: Row, 0, 8, 0, 1
        header = bytes([y >> 8, y & 0xFF, 0x00, 0x08, 0x00, 0x01])
        ser.write(create_packet(0x85, header + data))
        time.sleep(0.005)

    print("End Page...")
    ser.write(create_packet(0xE3, [0x01]))
    time.sleep(0.1)
    ser.write(create_packet(0xF3, [0x01]))
    
    print("Done!")
    ser.close()

if __name__ == "__main__":
    main()