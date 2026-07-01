package test.recipe

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumRecipes {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testRecipe() {}
}
