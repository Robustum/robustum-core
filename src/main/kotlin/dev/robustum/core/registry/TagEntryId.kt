package dev.robustum.core.registry

import com.mojang.serialization.Codec
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import kotlin.text.removePrefix
import kotlin.text.startsWith

/**
 * レジストリとタグのIDを共通化して扱うためのデータクラスです
 */
data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
    companion object {
        @JvmField
        val CODEC: Codec<TagEntryId> = Codec.STRING.xmap({
            when (it.startsWith("#")) {
                true -> TagEntryId(Identifier(it.removePrefix("#")), true)
                false -> TagEntryId(Identifier(it), false)
            }
        }, TagEntryId::asString)

        /**
         * [entry]から[TagEntryId]を返します。
         * @param T 値のクラス
         * @param transform [entry]を[Identifier]に変換するブロック
         * @return [TagEntryId.isTag]がfalseとなる[TagEntryId]
         */
        @JvmStatic
        fun <T : Any> of(entry: T, transform: (T) -> Identifier): TagEntryId = TagEntryId(transform(entry), false)

        /**
         * [tag]から[TagEntryId]を返します。
         * @param T 値のクラス
         * @param transform [tag]を[Identifier]に変換するブロック
         * @return [TagEntryId.isTag]がtrueとなる[TagEntryId]
         */
        @JvmStatic
        fun <T : Any> ofTag(tag: Tag<T>, transform: (Tag<T>) -> Identifier): TagEntryId = TagEntryId(transform(tag), true)
    }

    /**
     * [TagEntryId.isTag]がtrueの場合は#で[TagEntryId.id]を前置し，それ以外の場合はそのまま返します。
     */
    override fun asString(): String = when (isTag) {
        true -> "#$id"
        false -> id.toString()
    }
}
