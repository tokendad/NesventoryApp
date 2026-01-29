package com.tokendad.nesventorynew.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search

/**
 * Standard text input field with consistent styling.
 *
 * @param value Current text value
 * @param onValueChange Value change callback
 * @param label Field label
 * @param modifier Modifier
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param isError Whether field is in error state
 * @param errorMessage Error message to display
 * @param supportingText Helper text below field
 * @param enabled Whether field is enabled
 * @param readOnly Whether field is read-only
 * @param singleLine Whether to restrict to single line
 * @param maxLines Maximum lines for multiline input
 * @param keyboardType Keyboard type (text, number, email, etc.)
 * @param imeAction IME action (done, next, search, etc.)
 * @param onImeAction Callback when IME action triggered
 */
@Composable
fun NesTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = when {
            isError && errorMessage != null -> {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            }
            supportingText != null -> {
                { Text(supportingText) }
            }
            else -> null
        },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = onImeAction?.let { { it() } },
            onNext = onImeAction?.let { { it() } },
            onSearch = onImeAction?.let { { it() } },
            onGo = onImeAction?.let { { it() } }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Password input field with hidden text.
 *
 * @param value Current password value
 * @param onValueChange Value change callback
 * @param label Field label
 * @param modifier Modifier
 * @param placeholder Optional placeholder
 * @param isError Whether field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether field is enabled
 * @param imeAction IME action
 * @param onImeAction Callback when IME action triggered
 */
@Composable
fun NesPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = onImeAction?.let { { it() } }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Search input field with search icon.
 *
 * @param value Current search query
 * @param onValueChange Query change callback
 * @param modifier Modifier
 * @param placeholder Placeholder text
 * @param onSearch Callback when search action triggered
 */
@Composable
fun NesSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = onSearch?.let { { it() } }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Multiline text area for longer input.
 *
 * @param value Current text value
 * @param onValueChange Value change callback
 * @param label Field label
 * @param modifier Modifier
 * @param placeholder Optional placeholder
 * @param minLines Minimum visible lines
 * @param maxLines Maximum visible lines
 * @param isError Whether field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether field is enabled
 */
@Composable
fun NesTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    minLines: Int = 3,
    maxLines: Int = 5,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth()
    )
}
