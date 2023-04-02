package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.DeviceClass
import io.sh.abodeiq.model.NetworkProtocol
import io.sh.abodeiq.serialisation.BsonJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class DeviceClassDao(
    @BsonId @Transient
    val _id: Id<DeviceClassDao>? = null,
    val networkProtocol: NetworkProtocol,
    val genericClassKey: Int?,
    val specificClassKey: Int?,
    val label: String,
    @Serializable(BsonJsonSerializer::class)
    val details: org.bson.json.JsonObject? = null
) {
    companion object {
        fun of(d: DeviceClass) = DeviceClassDao(
            null,
            d.networkProtocol,
            d.genericClassKey,
            d.specificClassKey,
            d.label,
            org.bson.json.JsonObject(d.details.toString())
        )
    }

    fun getDeviceClass() = DeviceClass(
        networkProtocol,
        genericClassKey,
        specificClassKey,
        label,
        details?.let { Json.parseToJsonElement(it.json) }
    )
}
