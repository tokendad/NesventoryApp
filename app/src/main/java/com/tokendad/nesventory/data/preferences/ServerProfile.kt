package com.tokendad.nesventory.data.preferences

import java.util.UUID

data class ServerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val remoteUrl: String,
    val localUrl: String,
    val localSsid: String = "",
    val prioritizeLocal: Boolean = false
)

data class ServerProfileList(
    val profiles: List<ServerProfile> = emptyList(),
    val activeProfileId: String? = null
) {
    val activeProfile: ServerProfile?
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.firstOrNull()
}
