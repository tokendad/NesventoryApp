package com.tokendad.nesventorynew.ui.printer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventorynew.ui.components.NesDropdown
import com.tokendad.nesventorynew.ui.components.NesInlineLoading
import com.tokendad.nesventorynew.ui.components.NesMessageBanner
import com.tokendad.nesventorynew.ui.components.MessageType
import com.tokendad.nesventorynew.ui.components.NesPrimaryButton
import com.tokendad.nesventorynew.ui.components.NesSecondaryButton
import com.tokendad.nesventorynew.ui.components.NesTextField
import com.tokendad.nesventorynew.ui.theme.NesSpacing
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startScan()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Printer Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(NesSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.lg)
        ) {
            NesInlineLoading(isLoading = viewModel.isLoading)

            // Print Method Selector
            Text("Print Method", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = viewModel.printMethod == "local",
                    onClick = { viewModel.printMethod = "local" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Local Bluetooth")
                }
                SegmentedButton(
                    selected = viewModel.printMethod == "server",
                    onClick = { viewModel.printMethod = "server" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Server Printer")
                }
            }

            HorizontalDivider()

            if (viewModel.printMethod == "server") {
                // Server Configuration
                Text("Server Printer Configuration", style = MaterialTheme.typography.titleMedium)
                
                // Model Selector from Server
                NesDropdown(
                    label = "Server Printer Model",
                    options = viewModel.serverPrinterModels.map { it.label },
                    selectedOption = viewModel.serverPrinterModels.find { it.value == viewModel.config.model }?.label ?: viewModel.config.model,
                    onOptionSelected = { label ->
                        viewModel.serverPrinterModels.find { it.label == label }?.let { 
                            viewModel.onModelChange(it.value)
                        }
                    }
                )

                NesDropdown(
                    label = "Interface Type",
                    options = viewModel.supportedInterfaces,
                    selectedOption = viewModel.config.interface_type,
                    onOptionSelected = viewModel::onInterfaceChange
                )

                NesTextField(
                    value = viewModel.config.address ?: "",
                    onValueChange = viewModel::onAddressChange,
                    label = "Address (USB Port, IP, or MAC)"
                )

                NesSecondaryButton(
                    text = "Test Connection",
                    onClick = { viewModel.printTest() }
                )

            } else {
                // Local Configuration
                // Model Selector
                NesDropdown(
                    label = "Local Printer Model",
                    options = viewModel.supportedModels,
                    selectedOption = viewModel.config.model,
                    onOptionSelected = viewModel::onModelChange
                )

                // Bluetooth UI
                HorizontalDivider()
                Text("Bluetooth Devices", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NesSecondaryButton(
                        text = "Scan",
                        onClick = {
                            val permissions = if (android.os.Build.VERSION.SDK_INT >= 31) {
                                listOf(
                                    android.Manifest.permission.BLUETOOTH_SCAN,
                                    android.Manifest.permission.BLUETOOTH_CONNECT
                                )
                            } else {
                                listOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        },
                        fullWidth = false
                    )

                    val statusText = when (connectionState) {
                        0 -> "Disconnected"
                        1 -> "Connecting..."
                        2 -> "Connected"
                        3 -> "Disconnecting..."
                        else -> "Unknown"
                    }
                    Text("Status: $statusText")
                }

                // Device List (condensed)
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(NesSpacing.xs)) {
                        items(scannedDevices) { device ->
                            @android.annotation.SuppressLint("MissingPermission")
                            val deviceName = device.name ?: "Unknown Device"
                            ListItem(
                                headlineContent = { Text(deviceName, style = MaterialTheme.typography.bodySmall) },
                                supportingContent = { Text(device.address, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.clickable { 
                                    viewModel.connect(device) 
                                    viewModel.onAddressChange(device.address)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
                
                if (connectionState == 2) { // Connected
                    NesSecondaryButton(
                        text = "Test Local Print",
                        onClick = { viewModel.printTest() }
                    )
                }
            }

            HorizontalDivider()

            // Density Slider
            Text("Print Density: ${viewModel.config.density}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = viewModel.config.density.toFloat(),
                onValueChange = { viewModel.onDensityChange(it.roundToInt()) },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(modifier = Modifier.height(NesSpacing.sm))

            viewModel.errorMessage?.let {
                NesMessageBanner(message = it, type = MessageType.Error)
            }
            viewModel.successMessage?.let {
                NesMessageBanner(message = it, type = MessageType.Success)
            }

            NesPrimaryButton(
                text = "Save Server Configuration",
                onClick = { viewModel.saveConfig() },
                enabled = !viewModel.isLoading,
                loading = viewModel.isLoading
            )
        }
    }
}