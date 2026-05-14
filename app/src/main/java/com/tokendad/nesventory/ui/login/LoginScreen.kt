package com.tokendad.nesventory.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventory.ui.components.NesPasswordField
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.theme.NesSpacing
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onServerSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LoginScreenContent(
        username = viewModel.username,
        onUsernameChange = { viewModel.username = it },
        password = viewModel.password,
        onPasswordChange = { viewModel.password = it },
        rememberCredentials = viewModel.rememberCredentials,
        onRememberCredentialsChange = { viewModel.rememberCredentials = it },
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        serverConfigured = viewModel.serverConfigured,
        onLoginClick = { viewModel.login(onLoginSuccess) },
        onSsoLoginClick = {
            scope.launch {
                val url = viewModel.getSsoUrl()
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
                context.startActivity(intent)
            }
        },
        onServerSettingsClick = onServerSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberCredentials: Boolean,
    onRememberCredentialsChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    serverConfigured: Boolean = true,
    onLoginClick: () -> Unit,
    onSsoLoginClick: () -> Unit,
    onServerSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NesVentory Login") },
                actions = {
                    IconButton(onClick = onServerSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Server Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = NesSpacing.xl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )

            // No server configured banner
            AnimatedVisibility(visible = !serverConfigured) {
                Column {
                    Spacer(modifier = Modifier.height(NesSpacing.lg))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(NesSpacing.lg)) {
                            Text(
                                text = "No Server Configured",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(NesSpacing.xs))
                            Text(
                                text = "Tap ⚙ in the top-right corner to add your NesVentory server URL.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NesSpacing.xxl))

            // Username field
            NesTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Email"
            )

            Spacer(modifier = Modifier.height(NesSpacing.lg))

            // Password field
            NesPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password"
            )

            // Remember credentials checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NesSpacing.sm)
            ) {
                Checkbox(
                    checked = rememberCredentials,
                    onCheckedChange = onRememberCredentialsChange
                )
                Text(text = "Remember credentials")
            }

            Spacer(modifier = Modifier.height(NesSpacing.lg))

            // Login button
            NesPrimaryButton(
                text = "Login",
                onClick = onLoginClick,
                loading = isLoading,
                enabled = serverConfigured
            )

            Spacer(modifier = Modifier.height(NesSpacing.lg))

            // SSO Login button
            NesSecondaryButton(
                text = "Login with SSO",
                onClick = onSsoLoginClick,
                enabled = !isLoading && serverConfigured
            )

            // Error message
            AnimatedVisibility(visible = errorMessage != null) {
                Column {
                    Spacer(modifier = Modifier.height(NesSpacing.lg))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(NesSpacing.xxl))

            Spacer(modifier = Modifier.height(NesSpacing.xxl))
        }
    }
}
