package test.text

import com.mojang.serialization.JsonOps
import dev.robustum.core.extensions.onErrored
import dev.robustum.core.extensions.onSucceeded
import dev.robustum.core.text.TextSerializerRegistry
import helper.shouldBe
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.item.Items
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumTexts {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testText() {
        // encode
        TextSerializerRegistry.TEXT_CODEC
            .encodeStart(JsonOps.INSTANCE, LiteralText("Hello World!"))
            .onSucceeded {
                it shouldBe {
                    "type"("minecraft:literal")
                    "text"("Hello World!")
                }
            }.onErrored(::error)

        TextSerializerRegistry.TEXT_CODEC
            .encodeStart(
                JsonOps.INSTANCE,
                TranslatableText(
                    Items.IRON_INGOT.translationKey,
                    "abc",
                    334,
                    LiteralText("debug"),
                ),
            ).onSucceeded {
                it shouldBe {
                    "type"("minecraft:translatable")
                }
            }.onErrored(::error)
    }
}
