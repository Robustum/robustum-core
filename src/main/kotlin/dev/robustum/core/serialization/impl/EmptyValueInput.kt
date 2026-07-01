package dev.robustum.core.serialization.impl

import com.mojang.serialization.Codec
import dev.robustum.core.serialization.ValueInput

internal object EmptyValueInput : ValueInput {
    override fun <T : Any> read(key: String, codec: Codec<T>): T? = null

    override fun child(key: String): ValueInput? = null

    override fun childOrEmpty(key: String): ValueInput = this

    override fun childrenList(key: String): Iterable<ValueInput>? = null

    override fun childrenListOrEmpty(key: String): Iterable<ValueInput> = emptySet()

    override fun <T : Any> list(key: String, codec: Codec<T>): Iterable<T>? = null

    override fun <T : Any> listOrEmpty(key: String, codec: Codec<T>): Iterable<T> = emptySet()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue

    override fun getByte(key: String, defaultValue: Byte): Byte = defaultValue

    override fun getShort(key: String, defaultValue: Short): Short = defaultValue

    override fun getInt(key: String): Int? = null

    override fun getInt(key: String, defaultValue: Int): Int = defaultValue

    override fun getLong(key: String): Long? = null

    override fun getLong(key: String, defaultValue: Long): Long = defaultValue

    override fun getFloat(key: String, defaultValue: Float): Float = defaultValue

    override fun getDouble(key: String, defaultValue: Double): Double = defaultValue

    override fun getString(key: String): String? = null

    override fun getString(key: String, defaultValue: String): String = defaultValue
}
