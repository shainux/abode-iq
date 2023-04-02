package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.Manufacturer
import io.sh.abodeiq.model.NetworkProtocol
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class ManufacturerDao (
    @BsonId @Transient
    val _id: Id<ManufacturerDao>? = null,
    val networkProtocol: NetworkProtocol,
    val code: Int,
    val label: String
){
    companion object {
        fun of(d: Manufacturer) = ManufacturerDao(
            null,
            d.networkProtocol,
            d.code,
            d.label
        )
    }

    fun getManufacturer() = Manufacturer(
        networkProtocol,
        code,
        label
    )
}
