package com.tokendad.nesventorynew.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.login("demouser", "demo123", onLoginSuccess)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NesVentory Auto-Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Logging in as demouser...", style = MaterialTheme.typography.bodyLarge)
        }

        viewModel.errorMessage?.let { msg ->
            Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.login("demouser", "demo123", onLoginSuccess) }) {
                Text("Retry")
            }
        }
    }
}