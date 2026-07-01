package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.extensions.dispatchByMap
import dev.robustum.core.extensions.isSucceeded
import dev.robustum.core.util.Either
import dev.robustum.core.util.identity
import net.minecraft.text.KeybindText
import net.minecraft.text.LiteralText
import net.minecraft.text.NbtText
import net.minecraft.text.ScoreText
import net.minecraft.text.SelectorText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import java.util.stream.Stream
import kotlin.streams.asStream

/**
 * [Text]の[Codec]を管理するクラスです。
 */
data object TextSerializerRegistry {
    @JvmStatic
    private val classRegistry: MutableMap<Class<*>, Serializer<*>> = mutableMapOf()

    @JvmStatic
    private val idRegistry: MutableMap<Identifier, Serializer<*>> = mutableMapOf()

    @JvmStatic
    private val SERIALIZER_CODEC: Codec<Serializer<*>> = Identifier.CODEC.comapFlatMap(
        { id: Identifier ->
            idRegistry[id]?.let { DataResult.success(it) } ?: DataResult.error("Unknown text serializer: $id")
        },
        Serializer<*>::id,
    )

    /**
     * [Text]向けの[Codec]です。
     */
    @JvmField
    val TEXT_CODEC: Codec<Text> = RobustumCodecs.lazy {
        SERIALIZER_CODEC.dispatchByMap(TextSerializerRegistry::getSerializer, Serializer<*>::codec)
    }

    @JvmStatic
    private fun getSerializer(text: Text): Serializer<*> =
        classRegistry[text::class.java] ?: error("Unknown text class: ${text::class.java}")

    @JvmStatic
    inline fun <reified T : Text> register(id: Identifier, codec: MapCodec<T>) {
        register(T::class.java, id, codec)
    }

    @JvmStatic
    fun <T : Text> register(clazz: Class<T>, id: Identifier, codec: MapCodec<T>) {
        val serializer: Serializer<T> = Serializer(id, codec)
        check(idRegistry.put(id, serializer) == null) { "Duplicated serializer: $id" }
        check(classRegistry.put(clazz, serializer) == null) { "Duplicated class: $clazz" }
    }

    init {
        register<LiteralText>(
            Identifier("literal"),
            Codec.STRING.fieldOf("text").xmap(::LiteralText, LiteralText::asString),
        )
        register<TranslatableText>(
            Identifier("translatable"),
            run {
                val argCodec: Codec<in Any> = RobustumCodecs
                    .either(RobustumCodecs.ANY, TextSerializerRegistry.TEXT_CODEC)
                    .xmap(
                        { either: Either<Any, Text> -> either.fold(identity(), Text::asString) },
                        { arg: Any -> if (arg is Text) Either.Right(arg) else Either.Left(arg) },
                    )
                RobustumCodecs.recordMap { instance ->
                    instance
                        .group(
                            Codec.STRING.fieldOf("translate").forGetter(TranslatableText::getKey),
                            argCodec.listOf().optionalFieldOf("with", listOf()).forGetter { it.args.toList() },
                        ).apply(instance) { key: String, args: List<Any> -> TranslatableText(key, *args.toTypedArray()) }
                }
            },
        )
        register<ScoreText>(
            Identifier("score"),
            RobustumCodecs.recordMap { instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("name").forGetter(ScoreText::getName),
                        Codec.STRING.fieldOf("objective").forGetter(ScoreText::getObjective),
                    ).apply(instance, ::ScoreText)
            },
        )
        register<SelectorText>(
            Identifier("selector"),
            Codec.STRING.fieldOf("selector").xmap(::SelectorText, SelectorText::asString),
        )
        register<KeybindText>(
            Identifier("keybind"),
            Codec.STRING.fieldOf("keybind").xmap(::KeybindText, KeybindText::asString),
        )
        register<NbtText>(
            Identifier("nbt"),
            run {
                val block: MapCodec<NbtText.BlockNbtText> = RobustumCodecs.recordMap { instance ->
                    instance
                        .group(
                            Codec.STRING.fieldOf("nbt").forGetter(NbtText.BlockNbtText::getPath),
                            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.BlockNbtText::shouldInterpret),
                            Codec.STRING.fieldOf("block").forGetter(NbtText.BlockNbtText::getPos),
                        ).apply(instance, NbtText::BlockNbtText)
                }

                val entity: MapCodec<NbtText.EntityNbtText> = RobustumCodecs.recordMap { instance ->
                    instance
                        .group(
                            Codec.STRING.fieldOf("nbt").forGetter(NbtText.EntityNbtText::getPath),
                            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.EntityNbtText::shouldInterpret),
                            Codec.STRING.fieldOf("entity").forGetter(NbtText.EntityNbtText::getSelector),
                        ).apply(instance, NbtText::EntityNbtText)
                }

                val storage: MapCodec<NbtText.StorageNbtText> = RobustumCodecs.recordMap { instance ->
                    instance
                        .group(
                            Codec.STRING.fieldOf("nbt").forGetter(NbtText.StorageNbtText::getPath),
                            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.StorageNbtText::shouldInterpret),
                            Identifier.CODEC.fieldOf("storage").forGetter(NbtText.StorageNbtText::getId),
                        ).apply(instance, NbtText::StorageNbtText)
                }
                object : MapCodec<NbtText>() {
                    override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> = sequence<T> {
                        yieldAll(block.keys(ops).iterator())
                        yieldAll(entity.keys(ops).iterator())
                        yieldAll(storage.keys(ops).iterator())
                    }.distinct().asStream()

                    override fun <T : Any> decode(ops: DynamicOps<T>, input: MapLike<T>): DataResult<NbtText> {
                        val blockResult: DataResult<NbtText.BlockNbtText> = block.decode(ops, input)
                        if (blockResult.isSucceeded) {
                            return blockResult.map { it as NbtText }
                        }
                        val entityResult: DataResult<NbtText.EntityNbtText> = entity.decode(ops, input)
                        if (entityResult.isSucceeded) {
                            return entityResult.map { it as NbtText }
                        }
                        return storage.decode(ops, input).map { it as NbtText }
                    }

                    override fun <T : Any> encode(input: NbtText, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
                        when (input) {
                            is NbtText.BlockNbtText -> block.encode(input, ops, prefix)
                            is NbtText.EntityNbtText -> entity.encode(input, ops, prefix)
                            is NbtText.StorageNbtText -> storage.encode(input, ops, prefix)
                            else -> prefix
                        }
                }
            },
        )
    }

    @JvmRecord
    private data class Serializer<T : Text>(val id: Identifier, val codec: MapCodec<T>)
}
