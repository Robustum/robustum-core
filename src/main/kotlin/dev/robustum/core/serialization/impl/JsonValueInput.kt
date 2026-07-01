package dev.robustum.core.serialization.impl

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import dev.robustum.core.extensions.onErrored
import dev.robustum.core.serialization.ValueIOAccess
import dev.robustum.core.serialization.ValueInput
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.optionals.getOrNull

internal class JsonValueInput(private val jsonObject: JsonObject) : ValueInput {
    companion object {
        @JvmStatic
        private val LOGGER: Logger = LogManager.getLogger(JsonValueInput::class.java)
    }

    private fun getJsonPrimitive(key: String): JsonPrimitive? {
        val jsonIn: JsonElement = jsonObject.get(key) ?: return null
        return jsonIn.takeIf(JsonElement::isJsonPrimitive)?.asJsonPrimitive
    }

    //    HTNbtInput    //

    override fun <T : Any> read(key: String, codec: Codec<T>): T? {
        val jsonIn: JsonElement = jsonObject.get(key) ?: return null
        return codec
            .parse(JsonOps.INSTANCE, jsonIn)
            .onErrored(LOGGER::error)
            .result()
            .getOrNull()
    }

    override fun child(key: String): ValueInput? {
        val jsonIn: JsonObject = jsonObject.get(key) as? JsonObject ?: return null
        return ValueIOAccess.createInput(jsonIn)
    }

    override fun childOrEmpty(key: String): ValueInput = child(key) ?: EmptyValueInput

    override fun childrenList(key: String): Iterable<ValueInput>? {
        val list: JsonArray = jsonObject.get(key) as? JsonArray ?: return null
        return when {
            list.none() -> null
            else -> list.filterIsInstance<JsonObject>().map(ValueIOAccess::createInput)
        }
    }

    override fun childrenListOrEmpty(key: String): Iterable<ValueInput> = childrenList(key) ?: emptySet()

    override fun <T : Any> list(key: String, codec: Codec<T>): Iterable<T>? {
        val list: JsonArray = jsonObject.get(key) as? JsonArray ?: return null
        return when {
            list.none() -> null
            else -> TypedInputList(list, codec)
        }
    }

    override fun <T : Any> listOrEmpty(key: String, codec: Codec<T>): Iterable<T> = list(key, codec) ?: emptySet()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isBoolean)?.asBoolean ?: defaultValue

    override fun getByte(key: String, defaultValue: Byte): Byte =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asByte ?: defaultValue

    override fun getShort(key: String, defaultValue: Short): Short =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asShort ?: defaultValue

    override fun getInt(key: String): Int? = getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asInt

    override fun getInt(key: String, defaultValue: Int): Int = getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asInt ?: defaultValue

    override fun getLong(key: String): Long? = getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asLong

    override fun getLong(key: String, defaultValue: Long): Long =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asLong ?: defaultValue

    override fun getFloat(key: String, defaultValue: Float): Float =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asFloat ?: defaultValue

    override fun getDouble(key: String, defaultValue: Double): Double =
        getJsonPrimitive(key)?.takeIf(JsonPrimitive::isNumber)?.asDouble ?: defaultValue

    override fun getString(key: String): String? = getJsonPrimitive(key)?.takeIf(JsonPrimitive::isString)?.asString

    override fun getString(key: String, defaultValue: String): String = getString(key) ?: defaultValue

    //    TypedInputList    //

    private class TypedInputList<T : Any>(private val list: JsonArray, private val codec: Codec<T>) : Iterable<T> {
        override fun iterator(): Iterator<T> = list
            .mapNotNull { json: JsonElement ->
                codec
                    .parse(JsonOps.INSTANCE, json)
                    .onErrored(LOGGER::error)
                    .result()
                    .getOrNull()
            }.iterator()
    }
}
