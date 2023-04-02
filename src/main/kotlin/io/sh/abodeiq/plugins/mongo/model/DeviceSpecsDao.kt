package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.DeviceSpecs
import io.sh.abodeiq.model.NetworkProtocol
import io.sh.abodeiq.serialisation.BsonJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class DeviceSpecsDao(
    @BsonId @Transient
    val _id: Id<DeviceSpecsDao>? = null,
    val networkProtocol: NetworkProtocol,
    val manufacturerId: Int,
    val productTypeId: Int,
    val productId: Int,
    val label: String,
    val description: String?,
    @Serializable(BsonJsonSerializer::class)
    val details: org.bson.json.JsonObject? = null
) {
    companion object {
        fun of(d: DeviceSpecs) = DeviceSpecsDao(
            null,
            d.networkProtocol,
            d.manufacturerId,
            d.productTypeId,
            d.productId,
            d.label,
            d.description,
            org.bson.json.JsonObject(d.details.toString())
        )
    }

    fun getDeviceSpecs() = DeviceSpecs(
        networkProtocol,
        manufacturerId,
        productTypeId,
        productId,
        label,
        description,
        details?.let { Json.parseToJsonElement(it.json) }
    )
}
