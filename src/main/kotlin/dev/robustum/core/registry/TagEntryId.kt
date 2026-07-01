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
@JvmRecord
data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
    companion object {
        @JvmField
        val CODEC: Codec<TagEntryId> = Codec.STRING.comapFlatMap(
            { value: String ->
                if (value.startsWith("#")) {
                    Identifier.method_29186(value.removePrefix("#")).map { TagEntryId(it, true) }
                } else {
                    Identifier.method_29186(value).map { TagEntryId(it, false) }
                }
            },
            TagEntryId::asString,
        )

        /**
         * [entry]から[TagEntryId]を返します。
         * @param T 値のクラス
         * @param transform [entry]を[Identifier]に変換するブロック
         * @return [isTag]がfalseとなる[TagEntryId]
         */
        @JvmStatic
        fun <T : Any> of(entry: T, transform: (T) -> Identifier): TagEntryId = TagEntryId(transform(entry), false)

        /**
         * [tag]から[TagEntryId]を返します。
         * @param T 値のクラス
         * @param transform [tag]を[Identifier]に変換するブロック
         * @return [isTag]がtrueとなる[TagEntryId]
         */
        @JvmStatic
        fun <T : Any> ofTag(tag: Tag<T>, transform: (Tag<T>) -> Identifier): TagEntryId = TagEntryId(transform(tag), true)
    }

    /**
     * [isTag]がtrueの場合は#で[id]を前置し，それ以外の場合はそのまま返します。
     */
    override fun asString(): String = when (isTag) {
        true -> "#$id"
        false -> id.toString()
    }
}
