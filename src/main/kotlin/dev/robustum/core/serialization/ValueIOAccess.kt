package dev.robustum.core.serialization

import com.google.gson.JsonObject
import dev.robustum.core.serialization.impl.EmptyValueInput
import dev.robustum.core.serialization.impl.JsonValueInput
import dev.robustum.core.serialization.impl.JsonValueOutput
import dev.robustum.core.serialization.impl.NbtValueInput
import dev.robustum.core.serialization.impl.NbtValueOutput
import net.minecraft.nbt.NbtCompound

/**
 * [ValueInput]や[ValueOutput]のインスタンスを作成するクラスです。
 */
data object ValueIOAccess {
    /**
     * 指定した[JSON][jsonObject]から[ValueInput]を作成します。
     */
    fun createInput(jsonObject: JsonObject): ValueInput = when {
        jsonObject.size() == 0 -> EmptyValueInput
        else -> JsonValueInput(jsonObject)
    }

    /**
     * 指定した[JSON][jsonObject]から[ValueOutput]を作成します。
     */
    fun createOutput(jsonObject: JsonObject): ValueOutput = JsonValueOutput(jsonObject)

    /**
     * 指定した[NBT][nbtCompound]から[ValueInput]を作成します。
     */
    fun createInput(nbtCompound: NbtCompound): ValueInput = when {
        nbtCompound.isEmpty -> EmptyValueInput
        else -> NbtValueInput(nbtCompound)
    }

    /**
     * 指定した[NBT][nbtCompound]から[ValueOutput]を作成します。
     */
    fun createOutput(nbtCompound: NbtCompound): ValueOutput = NbtValueOutput(nbtCompound)
}
