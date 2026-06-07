package com.edumap.app

import java.text.Normalizer
import java.util.Locale

private val searchWhitespaceRegex = Regex("\\s+")

internal fun normalizeSearchText(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
        .replace('Ё', 'Е')
        .replace('ё', 'е')
        .lowercase(Locale.forLanguageTag("ru-RU"))

    return normalized.replace(searchWhitespaceRegex, " ").trim()
}

internal fun matchesSearch(value: String?, query: String): Boolean {
    val preparedQuery = normalizeSearchText(query)
    if (preparedQuery.isBlank()) return true
    return normalizeSearchText(value.orEmpty()).contains(preparedQuery)
}
