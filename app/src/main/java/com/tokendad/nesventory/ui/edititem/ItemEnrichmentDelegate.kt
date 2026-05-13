package com.tokendad.nesventory.ui.edititem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tokendad.nesventory.data.repository.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class ItemEnrichmentDelegate(
    private val itemRepository: ItemRepository,
    private val scope: CoroutineScope,
    private val onError: (String) -> Unit,
    private val onLoadingChange: (Boolean) -> Unit
) {
    var isReviewingEnrichment by mutableStateOf(false)
        private set

    private var originalValues = mapOf<String, String>()

    fun isFieldModified(fieldName: String, currentValue: String): Boolean {
        return isReviewingEnrichment && originalValues[fieldName] != currentValue
    }

    fun acceptEnrichment() {
        isReviewingEnrichment = false
        originalValues = emptyMap()
    }

    fun discardEnrichment(restore: (Map<String, String>) -> Unit) {
        if (!isReviewingEnrichment) return
        restore(originalValues)
        isReviewingEnrichment = false
        originalValues = emptyMap()
    }

    fun enrichItem(
        itemId: UUID,
        currentValues: Map<String, String>,
        applyEnrichedValues: (description: String?, brand: String?, modelNumber: String?, serialNumber: String?, estimatedValue: String?) -> Unit
    ) {
        scope.launch {
            onLoadingChange(true)
            try {
                originalValues = currentValues
                val result = itemRepository.enrichItem(itemId)
                val enriched = result.enriched_data.firstOrNull()
                if (enriched != null) {
                    applyEnrichedValues(
                        enriched.description,
                        enriched.brand,
                        enriched.model_number,
                        enriched.serial_number,
                        enriched.estimated_value
                    )
                    isReviewingEnrichment = true
                } else {
                    onError("No enriched data found: ${result.message}")
                }
            } catch (e: Exception) {
                onError("Failed to enrich data: ${e.localizedMessage}")
            } finally {
                onLoadingChange(false)
            }
        }
    }
}
