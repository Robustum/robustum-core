package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.*
import net.minecraft.util.Identifier

/**
 * [TextSerializer]のレジストリです。
 */
object TextSerializerRegistry {
    @JvmStatic
    private val classRegistry: MutableMap<Class<*>, TextSerializer<*>> = mutableMapOf()

    @JvmStatic
    private val idRegistry: MutableMap<Identifier, TextSerializer<*>> = mutableMapOf()

    /**
     * [TextSerializer]向けの[Codec]です。
     */
    @JvmField
    val SERIALIZER_CODEC: Codec<TextSerializer<*>> = Identifier.CODEC.comapFlatMap(
        { id: Identifier ->
            idRegistry[id]?.let { DataResult.success(it) } ?: DataResult.error("Unknown text serializer: $id")
        },
        TextSerializer<*>::id,
    )

    /**
     * [Text]向けの[Codec]です。
     */
    @JvmField
    val TEXT_CODEC: Codec<Text> = RobustumCodecs.lazy {
        SERIALIZER_CODEC.dispatch(TextSerializerRegistry::getSerializer, TextSerializer<*>::codec)
    }

    @JvmStatic
    private fun getSerializer(text: Text): TextSerializer<*> =
        classRegistry[text::class.java] ?: error("Unknown text class: ${text::class.java}")

    /**
     * [serializer]をレジストリに登録します。
     * @param T [Text]を継承したクラス
     * @throws IllegalStateException 重複したIDで登録した場合
     * @throws IllegalStateException 重複した[T]クラスで登録した場合
     */
    @JvmStatic
    fun <T : Text> register(clazz: Class<T>, serializer: TextSerializer<T>) {
        val id: Identifier = serializer.id
        check(idRegistry.put(id, serializer) == null) { "Duplicated serializer: $id" }
        check(classRegistry.put(clazz, serializer) == null) { "Duplicated class: $clazz" }
    }

    init {
        register(LiteralText::class.java, LiteralTextSerializer)
        register(TranslatableText::class.java, TranslatableTextSerializer)
        register(ScoreText::class.java, ScoreTextSerializer)
        register(SelectorText::class.java, SelectorTextSerializer)
        register(KeybindText::class.java, KeyBindTextSerializer)
        register(NbtText::class.java, NbtTextSerializer)
    }
}
