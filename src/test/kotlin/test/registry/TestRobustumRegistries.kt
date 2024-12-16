package test.registry

import dev.robustum.core.extensions.onErrored
import dev.robustum.core.extensions.onSucceeded
import dev.robustum.core.registry.RegistryLookup
import io.kotest.matchers.shouldBe
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.block.Blocks
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumRegistries {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testRegistries() {
        // lookup
        RegistryLookup.BLOCK
            .getId(Blocks.STONE)
            .onSucceeded { it.asString() shouldBe "minecraft:stone" }
            .onErrored(::error)

        RegistryLookup.BLOCK
            .getValue(Identifier("diamond_block"))
            .onSucceeded { it shouldBe Blocks.DIAMOND_BLOCK }
            .onErrored(::error)

        RegistryLookup.BLOCK
            .getId(BlockTags.SLABS)
            .onSucceeded { it.asString() shouldBe "#minecraft:slabs" }
            .onErrored(::error)
    }
}
