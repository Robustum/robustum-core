package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RegistryEntryListCodec
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.registry.RegistryEntryList
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.tag.Tag
import java.util.function.BiPredicate

/**
 * 液体版の[ItemIngredient]です。
 * @param entryList 条件に一致する液体のリスト
 * @param amount 必要な液体量
 */
@Suppress("UnstableApiUsage")
class FluidIngredient(val entryList: RegistryEntryList<Fluid>, val amount: Long = FluidConstants.BUCKET) : BiPredicate<Fluid, Long> {
    companion object {
        @JvmField
        val EMPTY = FluidIngredient(RegistryEntryList.empty(), 0)

        @JvmField
        val CODEC: Codec<FluidIngredient> = RobustumCodecs.record { instance ->
            instance
                .group(
                    RegistryEntryListCodec.FLUID
                        .fieldOf("fluids")
                        .forGetter(FluidIngredient::entryList),
                    Codec.LONG.optionalFieldOf("amount", FluidConstants.BUCKET).forGetter(FluidIngredient::amount),
                ).apply(instance, ::FluidIngredient)
        }
    }

    constructor(tag: Tag<Fluid>, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.ofTag(tag), amount)

    constructor(fluid: Fluid, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.direct(fluid), amount)

    /**
     * この素材が有効かどうか判定します。
     */
    val isEmpty: Boolean
        get() = entryList.isEmpty || amount <= 0

    override fun test(fluid: Fluid, amount: Long): Boolean = when {
        fluid == Fluids.EMPTY || amount <= 0 -> this.isEmpty
        else -> fluid in entryList && amount >= this.amount
    }
}
