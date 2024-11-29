package dev.robustum.core.extensions

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.registry.Registry
import java.util.Optional

object RobustumCodecs {
    //    ItemStack    //

    private val NON_AIR_ITEM: Codec<Item> = lazeCodec { Registry.ITEM }.validate { item: Item ->
        when (item) {
            Items.AIR -> DataResult.error("Item must not be minecraft:air")
            else -> DataResult.success(item)
        }
    }

    private val RAW_STACK: Codec<ItemStack> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                NON_AIR_ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
                NbtCompound.CODEC.optionalFieldOf("tag").forGetter { stack: ItemStack -> Optional.ofNullable(stack.tag) },
            ).apply(instance) { item: Item, count: Int, nbt: Optional<NbtCompound> ->
                ItemStack(item, count).apply { nbt.ifPresent(this::setTag) }
            }
    }

    val ITEM_STACK: Codec<ItemStack> = RAW_STACK.optionalOf().xmap(
        { it.orElse(ItemStack.EMPTY) },
        { if (it.isEmpty) Optional.empty() else Optional.of(it) },
    )

    //    Tag    //

    @JvmField
    val TAG_ID: Codec<TagEntryId> = Codec.STRING.xmap({
        when (it.startsWith("#")) {
            true -> TagEntryId(Identifier(it.removePrefix("#")), true)
            false -> TagEntryId(Identifier(it), false)
        }
    }, TagEntryId::asString)

    data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
        companion object {
            @JvmStatic
            fun <T : Any> of(entry: T, transform: (T) -> Identifier): TagEntryId = TagEntryId(transform(entry), false)

            @JvmStatic
            fun <T : Any> tag(tag: Tag<T>, transform: (Tag<T>) -> Identifier): TagEntryId = TagEntryId(transform(tag), true)
        }

        override fun asString(): String = when (isTag) {
            true -> "#$id"
            false -> id.toString()
        }
    }
}
