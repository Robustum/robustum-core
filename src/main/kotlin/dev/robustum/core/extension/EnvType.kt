package dev.robustum.core.extension

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

inline fun EnvType.runWhenOn(runnable: () -> Unit) {
    if (FabricLoader.getInstance().environmentType == this) runnable()
}

inline fun <T> runForEnv(clientRun: () -> T, serverRun: () -> T) = when (currentEnvType) {
    EnvType.CLIENT -> clientRun()
    EnvType.SERVER -> serverRun()
}
