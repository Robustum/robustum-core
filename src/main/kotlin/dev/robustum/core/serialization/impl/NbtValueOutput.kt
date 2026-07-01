package dev.robustum.core.serialization.impl

import com.mojang.serialization.Codec
import dev.robustum.core.extensions.onSucceeded
import dev.robustum.core.serialization.ValueOutput
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps

internal class NbtValueOutput(private val compoundTag: NbtCompound) : ValueOutput {
    //    ValueOutput    //

    override fun <T : Any> write(key: String, codec: Codec<T>, value: T?) {
        if (value == null) return
        codec.encodeStart(NbtOps.INSTANCE, value).onSucceeded { compoundTag.put(key, it) }
    }

    override fun isEmpty(): Boolean = compoundTag.isEmpty

    override fun putBoolean(key: String, value: Boolean) {
        compoundTag.putBoolean(key, value)
    }

    override fun putByte(key: String, value: Byte) {
        compoundTag.putByte(key, value)
    }

    override fun putShort(key: String, value: Short) {
        compoundTag.putShort(key, value)
    }

    override fun putInt(key: String, value: Int) {
        compoundTag.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        compoundTag.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        compoundTag.putFloat(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        compoundTag.putDouble(key, value)
    }

    override fun putString(key: String, value: String) {
        compoundTag.putString(key, value)
    }

    override fun child(key: String): ValueOutput {
        val nbtIn = NbtCompound()
        compoundTag.put(key, nbtIn)
        return NbtValueOutput(nbtIn)
    }

    override fun childrenList(key: String): ValueOutput.ValueOutputList {
        val list = NbtList()
        compoundTag.put(key, list)
        return ValueOutputList(list)
    }

    override fun <T : Any> list(key: String, codec: Codec<T>): ValueOutput.TypedOutputList<T> {
        val list = NbtList()
        compoundTag.put(key, list)
        return TypedOutputList(list, codec)
    }

    //    ValueOutputList    //

    private class ValueOutputList(private val list: NbtList) : ValueOutput.ValueOutputList {
        override val isEmpty: Boolean get() = list.isEmpty()

        override fun addChild(): ValueOutput {
            val nbtIn = NbtCompound()
            list.add(nbtIn)
            return NbtValueOutput(nbtIn)
        }

        override fun discardLast() {
            list.removeLast()
        }
    }

    //    TypedOutputList    //

    private class TypedOutputList<T : Any>(private val list: NbtList, private val codec: Codec<T>) : ValueOutput.TypedOutputList<T> {
        override val isEmpty: Boolean get() = list.isEmpty()

        override fun add(element: T) {
            codec.encodeStart(NbtOps.INSTANCE, element).onSucceeded(list::add)
        }
    }
}
