package dev.robustum.core

import dev.robustum.core.recipe.RobustumRecipeSerializers
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object RobustumCore : ModInitializer {
    const val MOD_ID = "robustum_core"
    const val MOD_NAME = "Robustum Core"

    @JvmStatic
    fun id(path: String): Identifier = Identifier(MOD_ID, path)

    private val logger: Logger = LogManager.getLogger(RobustumCore::class.java)

    override fun onInitialize() {
        RobustumRecipeSerializers

        logger.info("Robustum Core is loaded!")
    }
}
