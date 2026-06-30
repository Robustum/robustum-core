package dev.robustum.core.text

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.util.Either
import dev.robustum.core.util.identity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

object TranslatableTextSerializer : TextSerializer<TranslatableText> {
    @JvmField
    val ARGS: Codec<in Any> = RobustumCodecs
        .either(RobustumCodecs.ANY, TextSerializerRegistry.TEXT_CODEC)
        .xmap(
            { either: Either<Any, Text> -> either.fold(identity(), Text::asString) },
            { arg: Any -> if (arg is Text) Either.Right(arg) else Either.Left(arg) },
        )

    override val id: Identifier = Identifier("translatable")

    override val codec: Codec<TranslatableText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("translate").forGetter(TranslatableText::getKey),
                ARGS.listOf().optionalFieldOf("with", listOf()).forGetter { it.args.toList() },
            ).apply(instance) { key: String, args: List<Any> -> TranslatableText(key, *args.toTypedArray()) }
    }
}
