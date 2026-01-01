import asyncio
from bleak import BleakClient, BleakScanner
import struct

CHAR_UUID = "bef8d6c9-9c21-4c9e-b632-bd58c1009f9f"

def create_packet(cmd, data):
    data_len = len(data)
    checksum = cmd ^ data_len
    for b in data: checksum ^= b
    return bytes([0x55, 0x55, cmd, data_len, *data, checksum, 0xAA, 0xAA])

async def find_printer():
    print("Scanning for Niimbot printers...")
    devices = await BleakScanner.discover()
    for d in devices:
        name = d.name or "Unknown"
        print(f"Found: {name} ({d.address})")
        if "D11" in name or "Niimbot" in name or "B21" in name:
            return d
    return None

async def main():
    device = await find_printer()
    if not device:
        print("No Niimbot printer found.")
        return

    print(f"Connecting to {device.name} ({device.address})...")
    
    async with BleakClient(device.address) as client:
        print("Connected!")
        await client.start_notify(CHAR_UUID, lambda s, d: print(f"Notif: {d.hex()}"))

        # 1. Connect
        await client.write_gatt_char(CHAR_UUID, bytes([0x03]) + create_packet(0xC1, [0x01]))
        await asyncio.sleep(0.5)

        # 2. Setup
        await client.write_gatt_char(CHAR_UUID, create_packet(0x21, [0x03])) # Density 3
        await client.write_gatt_char(CHAR_UUID, create_packet(0x23, [0x01])) # Type 1
        await asyncio.sleep(0.2)

        # 3. Print Start (9-byte)
        start_payload = struct.pack(">H I B B B", 1, 0, 0, 1, 0)
        await client.write_gatt_char(CHAR_UUID, create_packet(0x01, start_payload))
        await asyncio.sleep(0.5)

        # 4. Set Dimension (96x100)
        width = 96
        height = 100
        dim_payload = struct.pack(">HHHHBBBH", height, width, 1, 0, 0, 0, 0, 0)
        await client.write_gatt_char(CHAR_UUID, create_packet(0x13, dim_payload))
        await asyncio.sleep(0.2)

        # 5. Image Data (Split Counts)
        print("Sending Image Data...")
        row_bytes = 12
        # Pattern: Solid Black
        pixel_data = bytes([0xFF] * row_bytes) 
        
        # Calculate split counts for 0xFF * 12
        # Chunk 1 (4 bytes): 0xFF FF FF FF -> 32 pixels
        # Chunk 2 (4 bytes): 0xFF FF FF FF -> 32 pixels
        # Chunk 3 (4 bytes): 0xFF FF FF FF -> 32 pixels
        c1, c2, c3 = 32, 32, 32
        
        for y in range(height):
            # Header: [RowH, RowL, C1, C2, C3, Repeats]
            row_header = struct.pack(">H B B B B", y, c1, c2, c3, 1)
            
            await client.write_gatt_char(CHAR_UUID, create_packet(0x85, row_header + pixel_data))
            
            if y % 10 == 0: await asyncio.sleep(0.02)
            else: await asyncio.sleep(0.005)

        # 6. End
        print("Sending End Page...")
        await client.write_gatt_char(CHAR_UUID, create_packet(0xE3, [0x01]))
        await asyncio.sleep(0.1)
        await client.write_gatt_char(CHAR_UUID, create_packet(0xF3, [0x01]))
        
        print("Done!")
        await asyncio.sleep(1.0)

if __name__ == "__main__":
    asyncio.run(main())