package dev.robustum.core.recipe

import com.google.gson.JsonObject
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class DelegatedRecipeSerializer<T : Recipe<*>>(private val delegated: RecipeSerializer<T>, private val recipeCodec: RecipeCodec<T>) :
    RecipeSerializer<T> {
    override fun read(id: Identifier, json: JsonObject): T = recipeCodec
        .createCodec(id)
        .parse(JsonOps.INSTANCE, json)
        .result()
        .orElseThrow()

    fun <O : Any> write(dynamicOps: DynamicOps<O>, recipe: T): DataResult<O> =
        recipeCodec.createCodec(recipe.id).encodeStart(dynamicOps, recipe)

    override fun read(id: Identifier, buf: PacketByteBuf): T = delegated.read(id, buf)

    override fun write(buf: PacketByteBuf, recipe: T) {
        delegated.write(buf, recipe)
    }
}
