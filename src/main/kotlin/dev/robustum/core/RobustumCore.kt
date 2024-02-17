package dev.robustum.core

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

class RobustumCore : ModInitializer {
    companion object {
        const val MOD_ID = "robustum_core"
        const val MOD_NAME = "Robustum Core"
        private val logger = LogManager.getLogger(RobustumCore::class.java)

        @JvmOverloads
        @JvmStatic
        fun log(message: String, level: Level = Level.INFO) {
            logger.log(level, "[$MOD_NAME] $message")
        }
    }

    override fun onInitialize() {
        log("Robustum Core is loaded!")
    }
}
