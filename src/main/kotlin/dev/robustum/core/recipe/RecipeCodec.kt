package dev.robustum.core.recipe

import com.mojang.serialization.MapCodec
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier

/**
 * レシピの[MapCodec]を提供するインターフェースです。
 * @param T レシピのクラス
 */
fun interface RecipeCodec<T : Recipe<*>> {
    /**
     * 指定された[id]をコンストラクタに渡すような[MapCodec]を返します。
     * @see RobustumRecipeSerializers
     */
    fun createCodec(id: Identifier): MapCodec<T>
}
