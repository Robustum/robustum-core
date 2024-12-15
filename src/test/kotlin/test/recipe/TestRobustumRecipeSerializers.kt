package test.recipe

import com.mojang.serialization.JsonOps
import dev.robustum.core.RobustumCore.id
import dev.robustum.core.extensions.onErrored
import dev.robustum.core.extensions.onSucceeded
import dev.robustum.core.recipe.RobustumRecipeSerializers
import helper.shouldBe
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.SmeltingRecipe
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumRecipeSerializers {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testSmelting() {
        RobustumRecipeSerializers.SMELTING
            .write(
                JsonOps.INSTANCE,
                SmeltingRecipe(
                    id("test_smelting"),
                    "",
                    Ingredient.ofItems(Items.DIRT),
                    ItemStack(Items.DIAMOND),
                    32767f,
                    200,
                ),
            ).onSucceeded {
                it shouldBe {
                    "type"("robustum_core:smelting")
                    "output" {
                        "id"("minecraft:diamond")
                    }
                    "exp"(32767.0)
                    "input"("minecraft:dirt")
                }
            }.onErrored {
                error(it.message())
            }
    }
}
