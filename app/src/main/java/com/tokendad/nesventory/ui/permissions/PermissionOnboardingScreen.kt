package com.tokendad.nesventory.ui.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesTextButton
import com.tokendad.nesventory.ui.theme.NesSpacing

@Composable
fun PermissionOnboardingScreen(onComplete: () -> Unit) {
    val permissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { onComplete() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NesSpacing.xl)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(NesSpacing.xxl))

        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(NesSpacing.lg))

        Text(
            text = "App Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(NesSpacing.sm))

        Text(
            text = "Nesventory needs a few permissions to work its best. Here's what we'll use them for.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(NesSpacing.xl))

        PermissionCard(
            icon = Icons.Default.CameraAlt,
            title = "Camera",
            description = "Take photos of your items for easy identification and record-keeping."
        )

        Spacer(modifier = Modifier.height(NesSpacing.md))

        PermissionCard(
            icon = Icons.Default.LocationOn,
            title = "Location",
            description = "Detect your home Wi-Fi network to automatically switch between local and remote server access."
        )

        Spacer(modifier = Modifier.height(NesSpacing.md))

        PermissionCard(
            icon = Icons.Default.Bluetooth,
            title = "Nearby Devices",
            description = "Connect to your Niimbot label printer over Bluetooth to print item tags."
        )

        Spacer(modifier = Modifier.height(NesSpacing.md))

        PermissionCard(
            icon = Icons.Default.Nfc,
            title = "NFC (Coming Soon)",
            description = "Scan NFC tags attached to items for instant lookup. No permission required yet.",
            isInformational = true
        )

        Spacer(modifier = Modifier.height(NesSpacing.xxl))

        NesPrimaryButton(
            text = "Grant Permissions",
            onClick = { permissionLauncher.launch(permissions) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NesSpacing.sm))

        NesTextButton(
            text = "Not Now",
            onClick = onComplete
        )

        Spacer(modifier = Modifier.height(NesSpacing.xl))
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isInformational: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInformational)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(NesSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NesSpacing.lg)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isInformational)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
