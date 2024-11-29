package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.RobustumCore
import dev.robustum.core.extensions.RobustumCodecs
import dev.robustum.core.extensions.toDefaultedList
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object RobustumRecipeSerializers {
    @JvmField
    val SHAPELESS: DelegatedRecipeSerializer<ShapelessRecipe> =
        registerDelegated(
            "shapeless",
            RecipeSerializer.SHAPELESS,
        ) { id: Identifier ->
            RecordCodecBuilder.create<ShapelessRecipe> { instance ->
                instance
                    .group(
                        Codec.STRING
                            .optionalFieldOf("group", "")
                            .forGetter(ShapelessRecipe::getGroup),
                        ItemIngredient.VANILLA_CODEC
                            .listOf()
                            .fieldOf("ingredients")
                            .forGetter(ShapelessRecipe::getIngredients),
                        RobustumCodecs.ITEM_STACK
                            .fieldOf("result")
                            .forGetter(ShapelessRecipe::getOutput),
                    ).apply(instance) { group: String, ingredients: List<Ingredient>, result: ItemStack ->
                        ShapelessRecipe(id, group, result, ingredients.toDefaultedList(Ingredient.EMPTY))
                    }
            }
        }

    @JvmField
    val SMELTING: DelegatedRecipeSerializer<SmeltingRecipe> =
        registerDelegated(
            "smelting",
            RecipeSerializer.SMELTING,
            createCookingRecipe(::SmeltingRecipe),
        )

    @JvmField
    val BLASTING: DelegatedRecipeSerializer<BlastingRecipe> =
        registerDelegated(
            "blasting",
            RecipeSerializer.BLASTING,
            createCookingRecipe(::BlastingRecipe),
        )

    @JvmField
    val SMOKING: DelegatedRecipeSerializer<SmokingRecipe> =
        registerDelegated(
            "smoking",
            RecipeSerializer.SMOKING,
            createCookingRecipe(::SmokingRecipe),
        )

    @JvmField
    val CAMPFIRE_COOKING: DelegatedRecipeSerializer<CampfireCookingRecipe> =
        registerDelegated(
            "campfire_cooking",
            RecipeSerializer.CAMPFIRE_COOKING,
            createCookingRecipe(::CampfireCookingRecipe),
        )

    @JvmField
    val STONECUTTING: DelegatedRecipeSerializer<StonecuttingRecipe> = registerDelegated(
        "stonecutting",
        RecipeSerializer.STONECUTTING,
    ) { id: Identifier ->
        RecordCodecBuilder.create<StonecuttingRecipe> { instance ->
            instance
                .group(
                    Codec.STRING
                        .optionalFieldOf("group", "")
                        .forGetter(StonecuttingRecipe::getGroup),
                    ItemIngredient.VANILLA_CODEC
                        .fieldOf("ingredient")
                        .forGetter { it.ingredients[0] },
                    RobustumCodecs.ITEM_STACK
                        .fieldOf("result")
                        .forGetter(StonecuttingRecipe::getOutput),
                ).apply(instance) { group: String, ingredient: Ingredient, result: ItemStack ->
                    StonecuttingRecipe(id, group, ingredient, result)
                }
        }
    }

    @JvmStatic
    private fun <T : AbstractCookingRecipe> createCookingRecipe(
        factory: (Identifier, String, Ingredient, ItemStack, Float, Int) -> T,
    ): RecipeCodec<T> = RecipeCodec<T> { id: Identifier ->
        RecordCodecBuilder.create<T> { instance ->
            instance
                .group(
                    Codec.STRING
                        .optionalFieldOf("group", "")
                        .forGetter { it.group },
                    ItemIngredient.VANILLA_CODEC
                        .fieldOf("ingredient")
                        .forGetter { it.ingredients[0] },
                    RobustumCodecs.ITEM_STACK
                        .fieldOf("result")
                        .forGetter { it.output },
                    Codec.FLOAT
                        .optionalFieldOf("exp", 0.0f)
                        .forGetter { it.experience },
                    Codec
                        .intRange(0, Short.MAX_VALUE.toInt())
                        .optionalFieldOf("time", 200)
                        .forGetter { it.cookTime },
                ).apply(instance) { group: String, ingredient: Ingredient, result: ItemStack, exp: Float, time: Int ->
                    factory(id, group, ingredient, result, exp, time)
                }
        }
    }

    @JvmStatic
    private fun <T : Recipe<*>> registerDelegated(
        path: String,
        serializer: RecipeSerializer<T>,
        recipeCodec: RecipeCodec<T>,
    ): DelegatedRecipeSerializer<T> = register(path, DelegatedRecipeSerializer(serializer, recipeCodec))

    @JvmStatic
    private fun <T : Recipe<*>, R : RecipeSerializer<T>> register(path: String, serializer: R): R = Registry.register(
        Registry.RECIPE_SERIALIZER,
        RobustumCore.id(path),
        serializer,
    )
}
