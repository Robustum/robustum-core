package dev.robustum.core.recipe

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

/**
 * クライアントへの同期を[delegated]に委譲した[RecipeSerializer]です。
 * @param T レシピのクラス
 * @param recipeCodec レシピのコーデック
 * @see RobustumRecipeSerializers
 */
class DelegatedRecipeSerializer<T : Recipe<*>>(private val delegated: RecipeSerializer<T>, private val recipeCodec: RecipeCodec<T>) :
    RecipeSerializer<T> {
    private fun createCodec(id: Identifier): Codec<T> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Registry.RECIPE_SERIALIZER.fieldOf("type").forGetter { this },
                recipeCodec.createCodec(id).forGetter { it },
            ).apply(instance) { _: RecipeSerializer<*>, recipe: T -> recipe }
    }

    override fun read(id: Identifier, json: JsonObject): T = createCodec(id)
        .parse(JsonOps.INSTANCE, json)
        .result()
        .orElseThrow()

    /**
     * 指定された[recipe]を[dynamicOps]で[O]に変換します。
     * @param O 変換先のクラス
     * @return [DataResult]で包まれた[O]
     */
    fun <O : Any> write(dynamicOps: DynamicOps<O>, recipe: T): DataResult<O> = createCodec(recipe.id).encodeStart(dynamicOps, recipe)

    override fun read(id: Identifier, buf: PacketByteBuf): T = delegated.read(id, buf)

    override fun write(buf: PacketByteBuf, recipe: T) {
        delegated.write(buf, recipe)
    }
}
