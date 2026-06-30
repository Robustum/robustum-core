package dev.robustum.core.text

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

object LiteralTextSerializer : TextSerializer<LiteralText> {
    override val id: Identifier = Identifier("literal")

    override val codec: Codec<LiteralText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("text").forGetter(LiteralText::asString),
            ).apply(instance, ::LiteralText)
    }
}
