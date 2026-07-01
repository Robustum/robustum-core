package dev.robustum.core

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data object RobustumCore : ModInitializer {
    const val MOD_ID = "robustum_core"
    const val MOD_NAME = "Robustum Core"

    @JvmStatic
    fun id(path: String): Identifier = Identifier(MOD_ID, path)

    @JvmStatic
    private val LOGGER: Logger = LogManager.getLogger(RobustumCore::class.java)

    override fun onInitialize() {
        LOGGER.info("Robustum Core is loaded!")
    }
}
