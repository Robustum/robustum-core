package test.registry

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
    fun testRegistry() {
        // lookup
        RegistryLookup.BLOCK
            .getId(Blocks.STONE)
            .onRight { it.asString() shouldBe "minecraft:stone" }
            .onLeft(::error)

        RegistryLookup.BLOCK[Identifier("diamond_block")]
            .onRight { it shouldBe Blocks.DIAMOND_BLOCK }
            .onLeft(::error)

        RegistryLookup.BLOCK
            .getId(BlockTags.SLABS)
            .onRight { it.asString() shouldBe "#minecraft:slabs" }
            .onLeft(::error)
    }
}
