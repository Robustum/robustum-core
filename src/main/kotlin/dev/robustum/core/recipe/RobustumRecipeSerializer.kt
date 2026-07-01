package dev.robustum.core.recipe

import com.google.gson.JsonObject
import dev.robustum.core.serialization.ValueIOAccess
import dev.robustum.core.serialization.ValueInput
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

interface RobustumRecipeSerializer<RECIPE : Recipe<*>> : RecipeSerializer<RECIPE> {
    override fun read(id: Identifier, json: JsonObject): RECIPE = read(id, ValueIOAccess.createInput(json))

    fun read(id: Identifier, input: ValueInput): RECIPE
}
