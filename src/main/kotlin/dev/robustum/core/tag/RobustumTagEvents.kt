package dev.robustum.core.tag

import dev.robustum.core.extensions.idOrNull
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.tag.Tag
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import java.util.function.BiConsumer

object RobustumTagEvents {
    /**
     * [net.minecraft.tag.TagGroupLoader.prepareReload]の最後で呼び出される
     */
    @JvmField
    val REGISTER: Event<Register> =
        EventFactory.createArrayBacked(Register::class.java) { callbacks: Array<out Register> ->
            Register { helper: Helper -> callbacks.forEach { it.onRegister(helper) } }
        }

    /**
     * [net.minecraft.tag.ServerTagManagerHolder.setTagManager]，[net.minecraft.client.network.ClientPlayNetworkHandler.onSynchronizeTags]の最後でそれぞれ呼び出される
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
     * @param registry 現在のレジストリ
     * @param consumer 渡されたタグの[Identifier]とそのエントリ[Tag.Entry]を受け取る
     */
    class Helper(private val registry: Registry<*>, private val consumer: BiConsumer<Identifier, Tag.Entry>) {
        /**
         * @param registryKey 登録しようとしているレジストリのキー
         * @param tag 登録しようとしているタグ，[Tag.Identified]を実装している必要がある
         * @param values 指定したタグに紐づけようとしている値
         */
        fun <T : Any> add(registryKey: RegistryKey<out Registry<T>>, tag: Tag<T>, vararg values: T) {
            val tagId: Identifier = tag.idOrNull ?: return
            add(registryKey, tagId, *values)
        }

        /**
         * @param registryKey 登録しようとしているレジストリのキー
         * @param tagId 登録しようとしているタグの[Identifier]
         * @param values 指定したタグに紐づけようとしている値
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

    /**
     * @see [net.minecraft.tag.ServerTagManagerHolder.setTagManager]
     * @see [net.minecraft.client.network.ClientPlayNetworkHandler.onSynchronizeTags]
     */
    fun interface Reload {
        /**
         * @param manager 各サイドにおける[TagManager]
         * @param environment 各サイドにおける[EnvType]
         */
        fun onReload(manager: TagManager, environment: EnvType)
    }
}
