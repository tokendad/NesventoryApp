package com.tokendad.nesventory.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysFields() {
        composeTestRule.setContent {
            LoginScreenContent(
                username = "",
                onUsernameChange = {},
                password = "",
                onPasswordChange = {},
                rememberCredentials = false,
                onRememberCredentialsChange = {},
                isLoading = false,
                errorMessage = null,
                onLoginClick = {},
                onSsoLoginClick = {},
                onServerSettingsClick = {}
            )
        }

        composeTestRule.onNodeWithText("NesVentory Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login with SSO").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_displaysError() {
        composeTestRule.setContent {
            LoginScreenContent(
                username = "",
                onUsernameChange = {},
                password = "",
                onPasswordChange = {},
                rememberCredentials = false,
                onRememberCredentialsChange = {},
                isLoading = false,
                errorMessage = "Invalid credentials",
                onLoginClick = {},
                onSsoLoginClick = {},
                onServerSettingsClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
}
