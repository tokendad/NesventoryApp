package com.tokendad.nesventory.util

import android.net.Uri

object PhotoUrlValidator {
    private fun normalizeBase(serverBaseUrl: String): String =
        serverBaseUrl.trim().trimEnd('/')

    private fun joinRelative(base: String, relativePath: String): String {
        val cleanPath = relativePath.removePrefix("/")
        return "$base/$cleanPath"
    }

    fun buildPhotoUrl(path: String, serverBaseUrl: String): String {
        val normalizedBase = normalizeBase(serverBaseUrl)
        val baseUri = Uri.parse(normalizedBase)
        val photoUri = Uri.parse(path)
        val scheme = photoUri.scheme?.lowercase()

        if (scheme == null) {
            return joinRelative(normalizedBase, path)
        }

        val isHttp = scheme == "http" || scheme == "https"
        if (!isHttp) {
            return joinRelative(normalizedBase, path)
        }

        val sameHost = photoUri.host?.equals(baseUri.host, ignoreCase = true) == true
        if (sameHost) {
            return path
        }

        val fallbackPath = photoUri.encodedPath?.takeIf { it.isNotBlank() } ?: path
        return joinRelative(normalizedBase, fallbackPath)
    }

    fun buildThumbnailUrl(thumbnailPath: String?, fullPath: String, serverBaseUrl: String): String {
        val targetPath = thumbnailPath ?: fullPath
        return buildPhotoUrl(targetPath, serverBaseUrl)
    }
}
