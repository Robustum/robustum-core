package dev.robustum.core.serialization.impl

import com.mojang.serialization.Codec
import dev.robustum.core.extensions.onErrored
import dev.robustum.core.serialization.ValueIOAccess
import dev.robustum.core.serialization.ValueInput
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.nbt.NbtType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.optionals.getOrNull

@JvmRecord
internal data class NbtValueInput(private val compoundTag: NbtCompound) : ValueInput {
    companion object {
        @JvmStatic
        private val LOGGER: Logger = LogManager.getLogger(NbtValueInput::class.java)
    }

    private inline fun <reified T : NbtElement> getTypedNbt(key: String, type: NbtType<T>): T? {
        val nbtIn: NbtElement = compoundTag.get(key) ?: return null
        val tagType: NbtType<*> = nbtIn.nbtType
        return if (tagType == type) nbtIn as T else null
    }

    private fun getNbtNumber(key: String): AbstractNbtNumber? {
        val nbtIn: NbtElement = compoundTag.get(key) ?: return null
        return nbtIn as? AbstractNbtNumber
    }

    //    HTNbtInput    //

    override fun <T : Any> read(key: String, codec: Codec<T>): T? {
        val nbtIn: NbtElement = compoundTag.get(key) ?: return null
        return codec
            .parse(NbtOps.INSTANCE, nbtIn)
            .onErrored(LOGGER::error)
            .result()
            .getOrNull()
    }

    override fun child(key: String): ValueInput? {
        val nbtIn: NbtCompound = getTypedNbt(key, NbtCompound.TYPE) ?: return null
        return ValueIOAccess.createInput(nbtIn)
    }

    override fun childOrEmpty(key: String): ValueInput = child(key) ?: EmptyValueInput

    override fun childrenList(key: String): Iterable<ValueInput>? {
        val nbtIn: NbtList = getTypedNbt(key, NbtList.TYPE) ?: return null
        return when {
            nbtIn.isEmpty() -> null
            else -> nbtIn.filterIsInstance<NbtCompound>().map(ValueIOAccess::createInput)
        }
    }

    override fun childrenListOrEmpty(key: String): Iterable<ValueInput> = childrenList(key) ?: emptySet()

    override fun <T : Any> list(key: String, codec: Codec<T>): Iterable<T>? {
        val nbtIn: NbtList = getTypedNbt(key, NbtList.TYPE) ?: return null
        return when {
            nbtIn.isEmpty() -> null
            else -> TypedInputList(nbtIn, codec)
        }
    }

    override fun <T : Any> listOrEmpty(key: String, codec: Codec<T>): Iterable<T> = list(key, codec) ?: emptySet()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.byteValue() != 0.toByte()
    }

    override fun getByte(key: String, defaultValue: Byte): Byte {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.byteValue()
    }

    override fun getShort(key: String, defaultValue: Short): Short {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.shortValue()
    }

    override fun getInt(key: String): Int? {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return null
        return nbtIn.intValue()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.intValue()
    }

    override fun getLong(key: String): Long? {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return null
        return nbtIn.longValue()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.longValue()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.floatValue()
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        val nbtIn: AbstractNbtNumber = getNbtNumber(key) ?: return defaultValue
        return nbtIn.doubleValue()
    }

    override fun getString(key: String): String? {
        val nbtIn: NbtString = getTypedNbt(key, NbtString.TYPE) ?: return null
        return nbtIn.asString()
    }

    override fun getString(key: String, defaultValue: String): String = getString(key) ?: defaultValue

    //    TypedInputList    //

    @JvmRecord
    private data class TypedInputList<T : Any>(private val list: NbtList, private val codec: Codec<T>) : Iterable<T> {
        override fun iterator(): Iterator<T> = list
            .mapNotNull { nbt: NbtElement ->
                codec
                    .parse(NbtOps.INSTANCE, nbt)
                    .onErrored(LOGGER::error)
                    .result()
                    .getOrNull()
            }.iterator()
    }
}
