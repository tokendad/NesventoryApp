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
    ser.write(create_packet(0x21, [0x03])) 
    ser.write(create_packet(0x23, [0x01])) 
    time.sleep(0.2)

    # 3. Print Start (9-byte)
    start_payload = struct.pack(">H I B B B", 1, 0, 0, 1, 0)
    ser.write(create_packet(0x01, start_payload))
    time.sleep(0.5)

    # 4. Set Dimension (96x100)
    width = 96
    height = 100
    midPoint = width // 2 # 48
    
    dim_payload = struct.pack(">HHHHBBBH", height, width, 1, 0, 0, 0, 0, 0)
    ser.write(create_packet(0x13, dim_payload))
    time.sleep(0.2)

    # 5. Image Data (White Pixel Counts)
    print("Sending Image Data (White Counts Mode)...")
    row_bytes = 12
    # Pattern: Solid Black
    pixel_data = bytes([0xFF] * row_bytes) 
    
    # Left Half (Bytes 0-5): 48 pixels. All Black. LeftBlack=48.
    # Right Half (Bytes 6-11): 48 pixels. All Black. RightBlack=48.
    left_black = 48
    right_black = 48
    
    # Header logic from niimbotjs:
    # writeUInt8(midPoint - left)
    # writeUInt8(midPoint - right)
    
    val2 = midPoint - left_black # 48 - 48 = 0
    val3 = midPoint - right_black # 48 - 48 = 0
    
    for y in range(height):
        # Header: [RowH, RowL, Val2, Val3, RepH, RepL]
        header = bytes([y >> 8, y & 0xFF, val2, val3, 0x00, 0x01])
        ser.write(create_packet(0x85, header + pixel_data))
        time.sleep(0.005)

    # 6. End
    ser.write(create_packet(0xE3, [0x01]))
    time.sleep(0.1)
    ser.write(create_packet(0xF3, [0x01]))
    
    print("Done!")
    ser.close()

if __name__ == "__main__":
    main()
