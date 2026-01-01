package com.tokendad.nesventorynew.ui.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState.asStateFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    // Common Niimbot UUIDs
    private val SERVICE_UUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb")
    private val WRITE_UUID = UUID.fromString("0000fee8-0000-1000-8000-00805f9b34fb") // Often used for write
    
    private val TAG = "BluetoothPrinterManager"
    
    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null) {
                 Log.d(TAG, "Found device: ${device.name} (${device.address})")
            }
            val currentList = _scannedDevices.value.toMutableList()
            if (device.name != null && !currentList.any { it.address == device.address }) {
                currentList.add(device)
                _scannedDevices.value = currentList
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (adapter == null || isScanning) return
        
        Log.d(TAG, "Starting scan...")
        _scannedDevices.value = emptyList()
        adapter.bluetoothLeScanner?.startScan(scanCallback)
        isScanning = true

        // Stop scan after 10 seconds
        handler.postDelayed({
            stopScan()
        }, 10000)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (isScanning && adapter != null) {
            Log.d(TAG, "Stopping scan.")
            adapter.bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        stopScan()
        Log.d(TAG, "Connecting to ${device.address}...")
        _connectionState.value = BluetoothProfile.STATE_CONNECTING
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.d(TAG, "Disconnecting...")
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
        _connectionState.value = BluetoothProfile.STATE_DISCONNECTED
    }

    @SuppressLint("MissingPermission")
    fun sendData(data: ByteArray): Boolean {
        if (bluetoothGatt == null) {
            Log.e(TAG, "sendData: BluetoothGatt is null")
            return false
        }
        val char = writeCharacteristic
        if (char == null) {
             Log.e(TAG, "sendData: Write Characteristic is null.")
             return false
        }
        
        Log.d(TAG, "Sending ${data.size} bytes...")
        
        // Use WRITE_TYPE_DEFAULT for acknowledged writes (slower but safer)
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        
        val success = if (android.os.Build.VERSION.SDK_INT >= 33) {
            bluetoothGatt?.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothGatt.GATT_SUCCESS
        } else {
            char.value = data
            bluetoothGatt?.writeCharacteristic(char) == true
        }
        
        if (!success) {
            Log.e(TAG, "Failed to write characteristic")
        }
        return success
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "Connection state changed: $newState (Status: $status)")
            _connectionState.value = newState
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected. Requesting MTU 512...")
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected.")
                bluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "MTU changed to $mtu (Status: $status). Discovering services...")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered. Listing all:")
                
                var foundWriteChar: BluetoothGattCharacteristic? = null
                
                for (service in gatt.services) {
                    Log.d(TAG, "Service: ${service.uuid}")
                    for (characteristic in service.characteristics) {
                        val props = characteristic.properties
                        val canWrite = (props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                        val canWriteNoResp = (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
                        val canNotify = (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                        Log.d(TAG, "  Char: ${characteristic.uuid} (Write: $canWrite, WriteNoResp: $canWriteNoResp, Notify: $canNotify)")
                        
                        // Priority 1: Legacy Niimbot (fee7 service -> fee8 char)
                        if (service.uuid.toString().startsWith("0000fee7") && characteristic.uuid.toString().startsWith("0000fee8")) {
                            foundWriteChar = characteristic
                            if (canNotify) enableNotification(gatt, characteristic)
                        }
                        
                        // Priority 2: Newer Niimbot (e781... service -> bef8... char)
                        if (service.uuid.toString() == "e7810a71-73ae-499d-8c15-faa9aef0c3f2" && 
                            characteristic.uuid.toString() == "bef8d6c9-9c21-4c9e-b632-bd58c1009f9f") {
                            foundWriteChar = characteristic
                             if (canNotify) enableNotification(gatt, characteristic)
                        }

                        // Fallback: If we haven't found a specific one yet, and this one is writable, keep it as candidate
                        if (foundWriteChar == null && (canWrite || canWriteNoResp)) {
                            foundWriteChar = characteristic
                            // Try to find a separate notify char if this one isn't? 
                            // For now, if this one notifies, enable it.
                            if (canNotify) enableNotification(gatt, characteristic)
                        }
                    }
                }
                
                writeCharacteristic = foundWriteChar
                
                if (writeCharacteristic != null) {
                    Log.d(TAG, "Selected Write Characteristic: ${writeCharacteristic?.uuid}")
                } else {
                    Log.e(TAG, "No writable characteristic found!")
                }
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
            }
        }
        
        @SuppressLint("MissingPermission")
        private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val success = gatt.setCharacteristicNotification(characteristic, true)
            if (success) {
                // Standard descriptor for notifications
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                if (descriptor != null) {
                    descriptor.value = android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    Log.d(TAG, "Notification descriptor written for ${characteristic.uuid}")
                } else {
                     Log.w(TAG, "Could not find CCCD descriptor for ${characteristic.uuid}")
                }
            } else {
                 Log.e(TAG, "Failed to enable notification locally for ${characteristic.uuid}")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value ?: return
            val hex = value.joinToString("") { "%02x".format(it) }
            Log.d(TAG, "Received: $hex")
        }
    }
}
