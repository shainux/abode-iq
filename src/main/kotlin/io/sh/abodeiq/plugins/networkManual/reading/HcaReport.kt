package io.sh.abodeiq.plugins.networkManual.reading

import io.sh.abodeiq.model.NodeReading
import kotlinx.serialization.Serializable

@Serializable
data class HcaReport(
    val deviceId: Long,
    val currentConsumption: Int,
    val dueDateValue: Int,
    val checkSum: Int,
    val validationLevel: Int,
    val algorithm: String
): NodeReading
