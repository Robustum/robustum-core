package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.ScoreText
import net.minecraft.util.Identifier

object ScoreTextSerializer : TextSerializer<ScoreText> {
    override val id: Identifier = Identifier("score")
    override val codec: Codec<ScoreText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("name").forGetter(ScoreText::getName),
                Codec.STRING.fieldOf("objective").forGetter(ScoreText::getObjective),
            ).apply(instance, ::ScoreText)
    }
}
