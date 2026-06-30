package dev.robustum.core.text

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.SelectorText
import net.minecraft.util.Identifier

object SelectorTextSerializer : TextSerializer<SelectorText> {
    override val id: Identifier = Identifier("selector")
    override val codec: Codec<SelectorText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("selector").forGetter(SelectorText::asString),
            ).apply(instance, ::SelectorText)
    }
}
