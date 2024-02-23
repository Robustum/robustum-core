package dev.robustum.core.extension

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.entrypoint.EntrypointContainer
import net.fabricmc.loader.api.metadata.ModMetadata
import java.nio.file.Path

inline fun <reified T> getEntrypoints(key: String): List<T> = FabricLoader.getInstance().getEntrypoints(key, T::class.java)

inline fun <reified T> getEntrypointContainers(key: String): List<EntrypointContainer<T>> =
    FabricLoader.getInstance().getEntrypointContainers(key, T::class.java)

inline fun <reified T> invokeEntrypoints(key: String, noinline invoker: (T) -> Unit) =
    FabricLoader.getInstance().invokeEntrypoints(key, T::class.java, invoker)

fun getModContainer(id: String): ModContainer? = FabricLoader.getInstance().getModContainer(id).orElse(null)

val allMods: Collection<ModContainer> = FabricLoader.getInstance().allMods

val allModsId: Collection<String> = allMods.map(ModContainer::getMetadata).map(ModMetadata::getId)

fun isModLoaded(id: String): Boolean = FabricLoader.getInstance().isModLoaded(id)

val currentEnvType: EnvType = FabricLoader.getInstance().environmentType

val gameDir: Path = FabricLoader.getInstance().gameDir

val configDir: Path = FabricLoader.getInstance().configDir
