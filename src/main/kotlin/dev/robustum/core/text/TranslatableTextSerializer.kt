package dev.robustum.core.text

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import java.util.function.Function

object TranslatableTextSerializer : TextSerializer<TranslatableText> {
    @JvmField
    val ARGS: Codec<in Any> = Codec
        .either(RobustumCodecs.ANY, TextSerializerRegistry.TEXT_CODEC)
        .xmap(
            { either: Either<in Any, Text> -> either.map(Function.identity(), Text::asString) },
            { arg: Any -> if (arg is Text) Either.right(arg) else Either.left(arg) },
        )

    override val id: Identifier = Identifier("translatable")

    override val codec: Codec<TranslatableText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("translate").forGetter(TranslatableText::getKey),
                ARGS.listOf().optionalFieldOf("with", listOf()).forGetter { it.args.toList() },
            ).apply(instance) { key: String, args: List<Any> -> TranslatableText(key, *args.toTypedArray()) }
    }
}
