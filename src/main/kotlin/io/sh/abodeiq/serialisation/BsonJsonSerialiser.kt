package io.sh.abodeiq.serialisation

import com.github.jershell.kbson.BsonFlexibleDecoder
import io.ktor.server.util.*
import io.ktor.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import org.bson.json.JsonObject
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = JsonObject::class)
object BsonJsonSerializer : KSerializer<JsonObject> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("BsonJson", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JsonObject) {
        encoder.encodeString(value.json)
    }

    override fun deserialize(decoder: Decoder): JsonObject {
        val str = when (decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().toString()
            is BsonFlexibleDecoder -> decoder.decodeString()
            else -> throw IllegalStateException("BsonJsonSerialiser: Unexpected decoder class: ${decoder::class.java.canonicalName}")
        }
        return JsonObject(str)
    }
}
