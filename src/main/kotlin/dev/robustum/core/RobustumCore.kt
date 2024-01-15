package dev.robustum.core

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

class RobustumCore : ModInitializer {
    companion object {
        const val MOD_ID = "robustum_core"
        private val logger = LogManager.getLogger(RobustumCore::class.java)
    }

    override fun onInitialize() {
        logger.info("Robustum Core is loaded!")
    }
}
