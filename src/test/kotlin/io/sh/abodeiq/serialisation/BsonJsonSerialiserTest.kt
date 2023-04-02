package io.sh.abodeiq.serialisation

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BsonJsonSerialiserTest {

   @Test
    fun deserialisationTest(){
        val expectedString =  """{"key1":"value1","key2":{"key3":"value3","key4":"value4"}}"""
        val testJson = Json.parseToJsonElement(expectedString)
        val bsonJson: org.bson.json.JsonObject = Json.decodeFromJsonElement(BsonJsonSerializer, testJson)
        assertEquals(expectedString, bsonJson.json)
    }
}
