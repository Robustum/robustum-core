package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier

fun interface RecipeCodec<T : Recipe<*>> {
    fun createCodec(id: Identifier): Codec<T>
}
