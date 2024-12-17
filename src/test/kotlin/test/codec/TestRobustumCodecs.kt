package test.codec

import com.mojang.serialization.JsonOps
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.extensions.onErrored
import dev.robustum.core.extensions.onSucceeded
import helper.shouldBe
import io.kotest.matchers.shouldBe
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.ItemTags
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumCodecs {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testBlock() {
        RobustumCodecs.BLOCK
            .encodeStart(JsonOps.INSTANCE, Blocks.STONE)
            .onSucceeded { it.asString shouldBe "minecraft:stone" }
            .onErrored(::error)

        RobustumCodecs.BLOCK
            .encodeStart(JsonOps.INSTANCE, Blocks.AIR)
            .onErrored { it shouldBe "Block must not be minecraft:air" }
    }

    @Test
    fun testItem() {
        RobustumCodecs.ITEM
            .encodeStart(JsonOps.INSTANCE, Items.IRON_INGOT)
            .onSucceeded { it.asString shouldBe "minecraft:iron_ingot" }
            .onErrored(::error)

        RobustumCodecs.ITEM
            .encodeStart(JsonOps.INSTANCE, Items.AIR)
            .onErrored { it shouldBe "Item must not be minecraft:air" }
    }

    @Test
    fun testItemStack() {
        RobustumCodecs.ITEM_STACK
            .encodeStart(JsonOps.INSTANCE, ItemStack(Items.IRON_INGOT, 4))
            .onSucceeded {
                it shouldBe {
                    "id"("minecraft:iron_ingot")
                    "count"(4)
                }
            }.onErrored(::error)

        RobustumCodecs.ITEM_STACK
            .encodeStart(JsonOps.INSTANCE, ItemStack.EMPTY)
            .onSucceeded { it shouldBe JsonOps.INSTANCE.emptyMap() }
            .onErrored(::error)
    }

    @Test
    fun testIngredient() {
        RobustumCodecs.INGREDIENT
            .encodeStart(JsonOps.INSTANCE, Ingredient.ofItems(Items.DIAMOND))
            .onSucceeded { it.asString shouldBe "minecraft:diamond" }

        RobustumCodecs.INGREDIENT
            .encodeStart(JsonOps.INSTANCE, Ingredient.ofItems(Items.DIAMOND, Items.EMERALD))
            .onSucceeded { it.toString() shouldBe "[\"minecraft:diamond\",\"minecraft:emerald\"]" }

        RobustumCodecs.INGREDIENT
            .encodeStart(JsonOps.INSTANCE, Ingredient.fromTag(ItemTags.LOGS))
            .onSucceeded { it.asString shouldBe "#minecraft:logs" }

        RobustumCodecs.NON_EMPTY_INGREDIENT
            .encodeStart(JsonOps.INSTANCE, Ingredient.EMPTY)
            .onErrored { it shouldBe "Empty ingredient is not allowed!" }
    }
}
