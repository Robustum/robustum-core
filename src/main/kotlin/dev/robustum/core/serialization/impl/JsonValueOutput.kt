package dev.robustum.core.serialization.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import dev.robustum.core.extensions.onSucceeded
import dev.robustum.core.serialization.ValueOutput

internal class JsonValueOutput(private val jsonObject: JsonObject) : ValueOutput {
    //    HTValueOutput    //

    override fun <T : Any> write(key: String, codec: Codec<T>, value: T?) {
        if (value == null) return
        codec.encodeStart(JsonOps.INSTANCE, value).onSucceeded { jsonObject.add(key, it) }
    }

    override fun isEmpty(): Boolean = jsonObject.size() == 0

    override fun putBoolean(key: String, value: Boolean) {
        jsonObject.addProperty(key, value)
    }

    override fun putByte(key: String, value: Byte) {
        jsonObject.addProperty(key, value)
    }

    override fun putShort(key: String, value: Short) {
        jsonObject.addProperty(key, value)
    }

    override fun putInt(key: String, value: Int) {
        jsonObject.addProperty(key, value)
    }

    override fun putLong(key: String, value: Long) {
        jsonObject.addProperty(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        jsonObject.addProperty(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        jsonObject.addProperty(key, value)
    }

    override fun putString(key: String, value: String) {
        jsonObject.addProperty(key, value)
    }

    override fun child(key: String): ValueOutput {
        val jsonIn = JsonObject()
        jsonObject.add(key, jsonIn)
        return JsonValueOutput(jsonIn)
    }

    override fun childrenList(key: String): ValueOutput.ValueOutputList {
        val list = JsonArray()
        jsonObject.add(key, list)
        return ValueOutputList(list)
    }

    override fun <T : Any> list(key: String, codec: Codec<T>): ValueOutput.TypedOutputList<T> {
        val list = JsonArray()
        jsonObject.add(key, list)
        return TypedOutputList(list, codec)
    }

    //    ValueOutputList    //

    private class ValueOutputList(private val list: JsonArray) : ValueOutput.ValueOutputList {
        override val isEmpty: Boolean get() = list.none()

        override fun addChild(): ValueOutput {
            val jsonIn = JsonObject()
            list.add(jsonIn)
            return JsonValueOutput(jsonIn)
        }

        override fun discardLast() {
            list.remove(list.size() - 1)
        }
    }

    //    TypedOutputList    //

    private class TypedOutputList<T : Any>(private val list: JsonArray, private val codec: Codec<T>) : ValueOutput.TypedOutputList<T> {
        override val isEmpty: Boolean get() = list.none()

        override fun add(element: T) {
            codec.encodeStart(JsonOps.INSTANCE, element).onSucceeded(list::add)
        }
    }
}
