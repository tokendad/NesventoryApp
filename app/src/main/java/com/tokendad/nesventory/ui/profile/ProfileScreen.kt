package com.tokendad.nesventory.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.components.NesPasswordField
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile = viewModel.profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading && profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NesLoadingState(message = "Loading profile...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NesSpacing.md, vertical = NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.md)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NesSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
                ) {
                    val avatarUrl = profile?.avatar_url?.takeIf { it.isNotBlank() }
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "User avatar",
                            modifier = Modifier
                                .size(NesSize.thumbnailLarge)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(NesSize.thumbnailLarge)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.username?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = profile?.full_name?.ifBlank { null } ?: profile?.username.orEmpty(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    profile?.email?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    profile?.username?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            viewModel.successMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            NesSectionCard(title = "Edit Profile") {
                NesTextField(
                    value = viewModel.fullName,
                    onValueChange = {
                        viewModel.fullName = it
                        viewModel.clearMessages()
                    },
                    label = "Full Name"
                )
                NesTextField(
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                        viewModel.clearMessages()
                    },
                    label = "Email"
                )
                NesPrimaryButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile() },
                    loading = viewModel.isSavingProfile
                )
            }

            NesSectionCard(title = "Security", icon = Icons.Default.Lock) {
                if (profile?.has_password == true) {
                    NesPasswordField(
                        value = viewModel.currentPassword,
                        onValueChange = {
                            viewModel.currentPassword = it
                            viewModel.clearMessages()
                        },
                        label = "Current Password"
                    )
                }
                NesPasswordField(
                    value = viewModel.newPassword,
                    onValueChange = {
                        viewModel.newPassword = it
                        viewModel.clearMessages()
                    },
                    label = "New Password"
                )
                NesPasswordField(
                    value = viewModel.confirmPassword,
                    onValueChange = {
                        viewModel.confirmPassword = it
                        viewModel.clearMessages()
                    },
                    label = "Confirm New Password"
                )
                NesPrimaryButton(
                    text = "Update Password",
                    onClick = { viewModel.changePassword() },
                    loading = viewModel.isChangingPassword
                )
            }
        }
    }
}
