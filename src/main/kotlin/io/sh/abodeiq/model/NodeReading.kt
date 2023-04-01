package io.sh.abodeiq.model

import kotlinx.serialization.json.JsonElement

interface NodeReading {
    fun toJson(): JsonElement
}
