package com.tokendad.nesventory.ui.items

enum class LivingItemType {
    PERSON,
    PET,
    PLANT,
    NON_LIVING;

    companion object {
        fun from(isLiving: Boolean, relationshipType: String?): LivingItemType {
            if (!isLiving) return NON_LIVING
            return when (relationshipType?.trim()?.lowercase()) {
                "pet" -> PET
                "plant" -> PLANT
                else -> PERSON
            }
        }
    }
}
