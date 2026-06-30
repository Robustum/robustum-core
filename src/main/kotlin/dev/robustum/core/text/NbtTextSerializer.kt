package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.extensions.isSucceeded
import dev.robustum.core.util.DFUPair
import net.minecraft.text.NbtText
import net.minecraft.util.Identifier

object NbtTextSerializer : TextSerializer<NbtText> {
    @JvmField
    val BLOCK: Codec<NbtText.BlockNbtText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.BlockNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.BlockNbtText::shouldInterpret),
                Codec.STRING.fieldOf("block").forGetter(NbtText.BlockNbtText::getPos),
            ).apply(instance, NbtText::BlockNbtText)
    }

    @JvmField
    val ENTITY: Codec<NbtText.EntityNbtText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.EntityNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.EntityNbtText::shouldInterpret),
                Codec.STRING.fieldOf("entity").forGetter(NbtText.EntityNbtText::getSelector),
            ).apply(instance, NbtText::EntityNbtText)
    }

    @JvmField
    val STORAGE: Codec<NbtText.StorageNbtText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.StorageNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.StorageNbtText::shouldInterpret),
                Identifier.CODEC.fieldOf("storage").forGetter(NbtText.StorageNbtText::getId),
            ).apply(instance, NbtText::StorageNbtText)
    }

    override val id: Identifier = Identifier("nbt")
    override val codec: Codec<NbtText> = object : Codec<NbtText> {
        override fun <T : Any> encode(input: NbtText, ops: DynamicOps<T>, prefix: T): DataResult<T> = when (input) {
            is NbtText.BlockNbtText -> BLOCK.encode(input, ops, prefix)
            is NbtText.EntityNbtText -> ENTITY.encode(input, ops, prefix)
            is NbtText.StorageNbtText -> STORAGE.encode(input, ops, prefix)
            else -> DataResult.error("Unsupported type of NbtText $input")
        }

        override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<DFUPair<NbtText, T>> {
            val blockResult: DataResult<DFUPair<NbtText.BlockNbtText, T>> = BLOCK.decode(ops, input)
            if (blockResult.isSucceeded) {
                return blockResult.map { it.mapFirst { text: NbtText.BlockNbtText -> text as NbtText } }
            }
            val entityResult: DataResult<DFUPair<NbtText.EntityNbtText, T>> = ENTITY.decode(ops, input)
            if (entityResult.isSucceeded) {
                return entityResult.map { it.mapFirst { text: NbtText.EntityNbtText -> text as NbtText } }
            }
            return STORAGE.decode(ops, input).map { it.mapFirst { text: NbtText.StorageNbtText -> text as NbtText } }
        }
    }
}
