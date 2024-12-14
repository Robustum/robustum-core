package dev.robustum.core.extensions

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.registry.Registry
import java.util.*

object RobustumCodecs {
    //    Block    //
    /**
     * [Blocks.AIR]を受け付けない[Block]の[Codec]です。
     */
    @JvmField
    val BLOCK: Codec<Block> = lazyCodec { Registry.BLOCK }.validate { block: Block ->
        when (block) {
            Blocks.AIR -> DataResult.error("Block must not be minecraft:air")
            else -> DataResult.success(block)
        }
    }

    //    ItemStack    //
    /**
     * [Items.AIR]を受け付けない[Item]の[Codec]です。
     */
    @JvmField
    val ITEM: Codec<Item> = lazyCodec { Registry.ITEM }.validate { item: Item ->
        when (item) {
            Items.AIR -> DataResult.error("Item must not be minecraft:air")
            else -> DataResult.success(item)
        }
    }

    @JvmStatic
    private val RAW_STACK: Codec<ItemStack> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
                NbtCompound.CODEC.optionalFieldOf("tag").forGetter { stack: ItemStack -> Optional.ofNullable(stack.tag) },
            ).apply(instance) { item: Item, count: Int, nbt: Optional<NbtCompound> ->
                ItemStack(item, count).apply { nbt.ifPresent(this::setTag) }
            }
    }

    /**
     * [ItemStack.isEmpty]を返す[ItemStack]も受け付ける[ItemStack]の[Codec]です。
     */
    @JvmField
    val ITEM_STACK: Codec<ItemStack> = RAW_STACK.optionalOf().xmap(
        { it.orElse(ItemStack.EMPTY) },
        { if (it.isEmpty) Optional.empty() else Optional.of(it) },
    )

    //    Tag    //
    /**
     * タグのIDを#で前置する[TagEntryId]の[Codec]です。
     */
    @JvmField
    val TAG_ID: Codec<TagEntryId> = Codec.STRING.xmap({
        when (it.startsWith("#")) {
            true -> TagEntryId(Identifier(it.removePrefix("#")), true)
            false -> TagEntryId(Identifier(it), false)
        }
    }, TagEntryId::asString)

    /**
     * レジストリとタグのIDを共通化して扱うためのデータクラスです
     */
    data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
        companion object {
            /**
             * [entry]から[RobustumCodecs.TagEntryId]を返します。
             * @param T 値のクラス
             * @param transform [entry]を[Identifier]に変換するブロック
             * @return [RobustumCodecs.TagEntryId.isTag]がfalseとなる[RobustumCodecs.TagEntryId]
             */
            @JvmStatic
            fun <T : Any> of(entry: T, transform: (T) -> Identifier): TagEntryId = TagEntryId(transform(entry), false)

            /**
             * [tag]から[RobustumCodecs.TagEntryId]を返します。
             * @param T 値のクラス
             * @param transform [tag]を[Identifier]に変換するブロック
             * @return [RobustumCodecs.TagEntryId.isTag]がtrueとなる[RobustumCodecs.TagEntryId]
             */
            @JvmStatic
            fun <T : Any> ofTag(tag: Tag<T>, transform: (Tag<T>) -> Identifier): TagEntryId = TagEntryId(transform(tag), true)
        }

        /**
         * [RobustumCodecs.TagEntryId.isTag]がtrueの場合は#で[RobustumCodecs.TagEntryId.id]を前置し，それ以外の場合はそのまま返します。
         */
        override fun asString(): String = when (isTag) {
            true -> "#$id"
            false -> id.toString()
        }
    }
}
