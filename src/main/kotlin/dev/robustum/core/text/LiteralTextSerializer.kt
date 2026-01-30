package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

object LiteralTextSerializer : TextSerializer<LiteralText> {
    override val id: Identifier = Identifier("literal")

    override val codec: Codec<LiteralText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("text").forGetter(LiteralText::asString),
            ).apply(instance, ::LiteralText)
    }
}
