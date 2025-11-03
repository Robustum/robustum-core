package dev.robustum.core.tag

import dev.robustum.core.extensions.idOrNull
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroupLoader
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import java.util.function.BiConsumer

object RobustumTagEvents {
    /**
     * 動的に[Tag]を登録するイベントです。
     *
     * [TagGroupLoader.prepareReload]の最後で呼び出されます。
     */
    @JvmField
    val REGISTER: Event<Register> =
        EventFactory.createArrayBacked(Register::class.java) { callbacks: Array<out Register> ->
            Register { helper: Helper -> callbacks.forEach { it.onRegister(helper) } }
        }

    /**
     * [TagManager]がリロードされた時に呼び出されるイベントです。
     *
     * [ServerTagManagerHolder.setTagManager]，[ClientPlayNetworkHandler.onSynchronizeTags]の最後でそれぞれ呼び出されます。
     */
    @JvmField
    val RELOAD: Event<Reload> = EventFactory.createArrayBacked(Reload::class.java) { callbacks: Array<out Reload> ->
        Reload { manager: TagManager, environment: EnvType -> callbacks.forEach { it.onReload(manager, environment) } }
    }

    //    Register    //

    fun interface Register {
        fun onRegister(helper: Helper)
    }

    //    Helper    //

    /**
     * 動的なタグの登録を補助するクラスです。
     * @param registry 現在のレジストリ
     * @param consumer 渡されたタグの[Identifier]とそのエントリ[Tag.Entry]を受け取るブロック
     */
    class Helper(private val registry: Registry<*>, private val consumer: BiConsumer<Identifier, Tag.Entry>) {
        /**
         * [Tag]から[Identifier]を取得して登録します。
         * @param registryKey 登録しようとしているレジストリのキー
         * @param tag [Tag.Identified]を実装している必要があります。
         */
        fun <T : Any> add(registryKey: RegistryKey<out Registry<T>>, tag: Tag<T>, vararg values: T) {
            val tagId: Identifier = tag.idOrNull ?: return
            add(registryKey, tagId, *values)
        }

        /**
         * [tagId]に[values]を登録します。
         * @param registryKey 登録しようとしているレジストリのキー
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> add(registryKey: RegistryKey<out Registry<T>>, tagId: Identifier, vararg values: T) {
            if (registryKey == registry.key) {
                val fixedRegistry: Registry<T> = (registry as? Registry<T>) ?: return
                values
                    .mapNotNull(fixedRegistry::getId)
                    .map { Tag.ObjectEntry(it) }
                    .forEach { consumer.accept(tagId, it) }
            }
        }
    }

    //    Reload    //

    fun interface Reload {
        /**
         * @param manager 各サイドにおける[TagManager]
         * @param environment [ServerTagManagerHolder]では[EnvType.SERVER]，[ClientPlayNetworkHandler.onSynchronizeTags]では[EnvType.CLIENT]
         */
        fun onReload(manager: TagManager, environment: EnvType)
    }
}
