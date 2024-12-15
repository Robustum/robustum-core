package dev.robustum.core.recipe

import com.mojang.serialization.MapCodec
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier

fun interface RecipeCodec<T : Recipe<*>> {
    fun createCodec(id: Identifier): MapCodec<T>
}
