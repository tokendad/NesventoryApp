package com.tokendad.nesventory.ui.editlocation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventory.ui.addlocation.LocationCategorySelector
import com.tokendad.nesventory.ui.components.NesDropdown
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.PhotoUrlValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    onBackClick: () -> Unit,
    onLocationUpdated: () -> Unit,
    viewModel: EditLocationViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Media", "Insurance")
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadPhoto(context.contentResolver, uri)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Edit Location", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> GeneralTab(viewModel, onLocationUpdated)
                1 -> MediaTab(
                    viewModel = viewModel,
                    onAddPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                2 -> InsuranceTab(viewModel)
            }
        }
    }
}

@Composable
fun GeneralTab(viewModel: EditLocationViewModel, onLocationUpdated: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.sm)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
    ) {
        // Name & Friendly Name
        Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
            NesTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = "Name *",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.friendlyName,
                onValueChange = { viewModel.friendlyName = it },
                label = "Friendly Name",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        // Parent Location Selector
        val selectedParentName = viewModel.availableLocations
            .find { it.id == viewModel.selectedParentId }?.name ?: "None (Root)"

        val parentOptions = listOf("None (Root)") + viewModel.availableLocations
            .filter { it.id != viewModel.locationId }
            .map { it.name }

        NesDropdown(
            label = "Parent Location",
            options = parentOptions,
            selectedOption = selectedParentName,
            onOptionSelected = { selectedName ->
                if (selectedName == "None (Root)") {
                    viewModel.selectedParentId = null
                } else {
                    viewModel.availableLocations.find { it.name == selectedName }?.let {
                        viewModel.selectedParentId = it.id
                    }
                }
            }
        )

        // Location Category
        LocationCategorySelector(
            selected = viewModel.locationCategory,
            categories = viewModel.locationCategories,
            onSelect = { viewModel.locationCategory = it }
        )

        // Address
        NesTextField(
            value = viewModel.address,
            onValueChange = { viewModel.address = it },
            label = "Address",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            textStyle = MaterialTheme.typography.bodySmall
        )

        // Estimated Value
        NesTextField(
            value = viewModel.estimatedPropertyValue,
            onValueChange = { viewModel.estimatedPropertyValue = it },
            label = "Estimated Property Value",
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )

        // Flags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Primary?", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = viewModel.isPrimaryLocation,
                    onCheckedChange = { viewModel.isPrimaryLocation = it }
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Container?", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = viewModel.isContainer,
                    onCheckedChange = { viewModel.isContainer = it }
                )
            }
        }

        // Description
        NesTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = "Description",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            textStyle = MaterialTheme.typography.bodySmall
        )

        viewModel.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        NesPrimaryButton(
            text = "Update Location",
            onClick = { viewModel.updateLocation(onSuccess = onLocationUpdated) },
            enabled = !viewModel.isLoading,
            loading = viewModel.isLoading
        )
    }
}

@Composable
fun MediaTab(
    viewModel: EditLocationViewModel,
    onAddPhoto: () -> Unit
) {
    if (viewModel.locationPhotos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NesEmptyState(
                title = "No photos",
                message = "Add photos for this location.",
                icon = Icons.Default.PhotoLibrary,
                action = {
                    NesPrimaryButton(
                        text = "Add Photo",
                        onClick = onAddPhoto,
                        fullWidth = false
                    )
                }
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            NesSecondaryButton(
                text = "Add Photo",
                onClick = onAddPhoto,
                fullWidth = false
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            items(viewModel.locationPhotos) { photo ->
                val imageUrl = PhotoUrlValidator.buildPhotoUrl(photo.path, viewModel.serverUrl)
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.deletePhoto(photo.id) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete photo")
                    }
                    if (photo.is_primary) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                "Primary",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsuranceTab(viewModel: EditLocationViewModel) {
    if (!viewModel.isPrimaryLocation) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NesEmptyState(
                title = "Insurance unavailable",
                message = "Insurance details are only available for primary locations."
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.sm)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.lg)
        ) {
            Text("Company Details", style = MaterialTheme.typography.titleMedium)
            NesTextField(
                value = viewModel.companyName,
                onValueChange = { viewModel.companyName = it },
                label = "Company Name",
                textStyle = MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.companyAddress,
                onValueChange = { viewModel.companyAddress = it },
                label = "Company Address",
                textStyle = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesTextField(
                    value = viewModel.companyEmail,
                    onValueChange = { viewModel.companyEmail = it },
                    label = "Company Email",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                NesTextField(
                    value = viewModel.companyPhone,
                    onValueChange = { viewModel.companyPhone = it },
                    label = "Company Phone",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
            NesTextField(
                value = viewModel.agentName,
                onValueChange = { viewModel.agentName = it },
                label = "Agent Name",
                textStyle = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()
            Text("Policy Details", style = MaterialTheme.typography.titleMedium)
            NesTextField(
                value = viewModel.policyNumber,
                onValueChange = { viewModel.policyNumber = it },
                label = "Policy Number",
                textStyle = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()
            Text("Primary Holder Details", style = MaterialTheme.typography.titleMedium)
            NesTextField(
                value = viewModel.primaryHolderName,
                onValueChange = { viewModel.primaryHolderName = it },
                label = "Name",
                textStyle = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesTextField(
                    value = viewModel.primaryHolderEmail,
                    onValueChange = { viewModel.primaryHolderEmail = it },
                    label = "Email",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                NesTextField(
                    value = viewModel.primaryHolderPhone,
                    onValueChange = { viewModel.primaryHolderPhone = it },
                    label = "Phone",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
            NesTextField(
                value = viewModel.primaryHolderAddress,
                onValueChange = { viewModel.primaryHolderAddress = it },
                label = "Address",
                textStyle = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()
            Text("Property Details", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesTextField(
                    value = viewModel.insurancePurchaseDate,
                    onValueChange = { viewModel.insurancePurchaseDate = it },
                    label = "Purchase Date",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                NesTextField(
                    value = viewModel.insurancePurchasePrice,
                    onValueChange = { viewModel.insurancePurchasePrice = it },
                    label = "Purchase Price",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
            NesTextField(
                value = viewModel.insuranceBuildDate,
                onValueChange = { viewModel.insuranceBuildDate = it },
                label = "Build Date",
                textStyle = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(NesSpacing.lg))
            NesPrimaryButton(
                text = "Save Insurance Info",
                onClick = { viewModel.updateLocation(onSuccess = {}) },
                enabled = !viewModel.isLoading,
                loading = viewModel.isLoading
            )
            Spacer(modifier = Modifier.height(NesSpacing.xxl))
        }
    }
}
