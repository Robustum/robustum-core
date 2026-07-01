package test.registry

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

class TestRobustumRegistries {
    @BeforeTest
    fun setup() {
        SharedConstants.getGameVersion()
        Bootstrap.initialize()
    }

    @Test
    fun testRegistry() {}
}
