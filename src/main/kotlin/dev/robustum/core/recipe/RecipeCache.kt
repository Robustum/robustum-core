package dev.robustum.core.recipe

import com.mojang.serialization.DataResult
import dev.robustum.core.extensions.filter
import dev.robustum.core.util.Option
import dev.robustum.core.util.TextResult
import dev.robustum.core.util.kotlin
import dev.robustum.core.util.toTextResult
import net.minecraft.inventory.Inventory
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.world.World

/**
 * 最後に一致した[Recipe]の[Identifier]を保持するクラスです。
 * @param I [Inventory]のクラス
 * @param R [Recipe]のクラス
 * @param recipeType [Recipe]の種類
 */
class RecipeCache<I : Inventory, R : Recipe<I>>(private val recipeType: RecipeType<R>) {
    /**
     * 最後にキャッシュされた[Recipe]の[Identifier]です。
     *
     * 以前のキャッシュがない場合はnullを返します。
     */
    private var id: Identifier? = null

    /**
     * 指定された[inventory]と[world]に一致する最初のレシピを返します。
     * @return [id]がある場合は[RecipeManager.get]から，それ以外の場合は[RecipeManager.getFirstMatch]から取得
     */
    @Suppress("UNCHECKED_CAST")
    fun getFirstMatch(inventory: I, world: World): TextResult<R> = world.recipeManager
        .let { recipeManager: RecipeManager ->
            when (id) {
                null -> recipeManager.getFirstMatch(recipeType, inventory, world).kotlin
                else -> recipeManager.get(id).kotlin.flatMap { Option.fromNullable(it as? R) }
            }
        }.getOrNull()
        .toTextResult { "Failed to find matching recipe!" }
        .onRight { this.id = it.id }
        .onLeft { this.id = null }

    /**
     * 指定された[inventory]と[world]に一致するすべてのレシピを返します。
     * @return [RecipeManager.getAllMatches]の戻り値が空の場合は[DataResult.error]，それ以外は[DataResult.success]
     */
    fun getAllMatches(inventory: I, world: World): DataResult<List<R>> = world.recipeManager
        .getAllMatches(recipeType, inventory, world)
        .let(DataResult<List<R>>::success)
        .filter(List<R>::isNotEmpty, "Failed to find matching recipes!")
}
