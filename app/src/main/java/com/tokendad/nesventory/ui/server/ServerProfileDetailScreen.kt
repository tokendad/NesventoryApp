package com.tokendad.nesventory.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tokendad.nesventory.data.preferences.ServerProfile
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.theme.NesSpacing
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerProfileDetailScreen(
    profileId: String?,
    onBackClick: () -> Unit,
    viewModel: ServerProfilesViewModel = hiltViewModel()
) {
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val existing = viewModel.getProfileById(profileId)

    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var remoteUrl by remember(existing?.id) { mutableStateOf(existing?.remoteUrl.orEmpty()) }
    var localUrl by remember(existing?.id) { mutableStateOf(existing?.localUrl.orEmpty()) }
    var localSsid by remember(existing?.id) { mutableStateOf(existing?.localSsid.orEmpty()) }
    var prioritizeLocal by remember(existing?.id) { mutableStateOf(existing?.prioritizeLocal ?: false) }
    var makeActive by remember(existing?.id) { mutableStateOf(existing?.id == activeProfileId || existing == null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add Server Profile" else "Edit Server Profile") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            NesTextField(
                value = name,
                onValueChange = { name = it },
                label = "Profile Name"
            )
            NesTextField(
                value = remoteUrl,
                onValueChange = { remoteUrl = it },
                label = "Remote URL"
            )
            NesTextField(
                value = localUrl,
                onValueChange = { localUrl = it },
                label = "Local URL"
            )
            NesTextField(
                value = localSsid,
                onValueChange = { localSsid = it },
                label = "Local SSID"
            )
            androidx.compose.foundation.layout.Row {
                Checkbox(
                    checked = prioritizeLocal,
                    onCheckedChange = { prioritizeLocal = it }
                )
                Text(
                    text = "Prioritize Local",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = NesSpacing.sm)
                )
            }
            androidx.compose.foundation.layout.Row {
                Checkbox(
                    checked = makeActive,
                    onCheckedChange = { makeActive = it }
                )
                Text(
                    text = "Set as active profile",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = NesSpacing.sm)
                )
            }

            NesPrimaryButton(
                text = "Save Profile",
                enabled = name.isNotBlank() && (remoteUrl.isNotBlank() || localUrl.isNotBlank()),
                onClick = {
                    val profile = ServerProfile(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        remoteUrl = remoteUrl.trim(),
                        localUrl = localUrl.trim(),
                        localSsid = localSsid.trim(),
                        prioritizeLocal = prioritizeLocal
                    )
                    viewModel.saveProfile(profile, makeActive = makeActive)
                    onBackClick()
                }
            )
        }
    }
}
