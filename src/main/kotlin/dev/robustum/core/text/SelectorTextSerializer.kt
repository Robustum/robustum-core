package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.SelectorText
import net.minecraft.util.Identifier

object SelectorTextSerializer : TextSerializer<SelectorText> {
    override val id: Identifier = Identifier("selector")
    override val codec: Codec<SelectorText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("selector").forGetter(SelectorText::asString),
            ).apply(instance, ::SelectorText)
    }
}
