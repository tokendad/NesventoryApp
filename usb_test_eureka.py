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

def count_bits(byte_val):
    return bin(byte_val).count('1')

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
    dim_payload = struct.pack(">HHHHBBBH", height, width, 1, 0, 0, 0, 0, 0)
    ser.write(create_packet(0x13, dim_payload))
    time.sleep(0.2)

    # 5. Image Data (EUREKA MODE: ChunkSize=5)
    print("Sending Image Data (Eureka Mode)...")
    row_bytes = 12
    pixel_data = bytes([0xFF] * row_bytes) 
    
    # Calculate counts with ChunkSize=5
    # Data: FF FF FF FF FF | FF FF FF FF FF | FF FF
    # C1 (5 bytes): 5*8 = 40
    # C2 (5 bytes): 5*8 = 40
    # C3 (2 bytes): 2*8 = 16
    c1, c2, c3 = 40, 40, 16
    
    for y in range(height):
        # Header: [RowH, RowL, C1, C2, C3, Repeats]
        header = struct.pack(">H B B B B", y, c1, c2, c3, 1)
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
