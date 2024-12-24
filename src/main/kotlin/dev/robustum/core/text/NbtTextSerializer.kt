package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.anyCodec
import net.minecraft.text.NbtText
import net.minecraft.util.Identifier

object NbtTextSerializer : TextSerializer<NbtText> {
    @JvmField
    val BLOCK: Codec<NbtText.BlockNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.BlockNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.BlockNbtText::shouldInterpret),
                Codec.STRING.fieldOf("block").forGetter(NbtText.BlockNbtText::getPos),
            ).apply(instance, NbtText::BlockNbtText)
    }

    @JvmField
    val ENTITY: Codec<NbtText.EntityNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.EntityNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.EntityNbtText::shouldInterpret),
                Codec.STRING.fieldOf("entity").forGetter(NbtText.EntityNbtText::getSelector),
            ).apply(instance, NbtText::EntityNbtText)
    }

    @JvmField
    val STORAGE: Codec<NbtText.StorageNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.StorageNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.StorageNbtText::shouldInterpret),
                Identifier.CODEC.fieldOf("storage").forGetter(NbtText.StorageNbtText::getId),
            ).apply(instance, NbtText::StorageNbtText)
    }

    override val id: Identifier = Identifier("nbt")
    override val codec: Codec<NbtText> = anyCodec(
        BLOCK,
        ENTITY,
        STORAGE,
    )
}
