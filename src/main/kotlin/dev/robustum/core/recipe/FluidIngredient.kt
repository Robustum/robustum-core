package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.codec.RegistryEntryListCodec
import dev.robustum.core.registry.RegistryEntryList
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.tag.Tag
import java.util.function.BiPredicate

@Suppress("UnstableApiUsage")
class FluidIngredient(val entryList: RegistryEntryList<Fluid>, val amount: Long = FluidConstants.BUCKET) : BiPredicate<Fluid, Long> {
    companion object {
        @JvmField
        val EMPTY = FluidIngredient(RegistryEntryList.empty(), 0)

        @JvmField
        val CODEC: Codec<FluidIngredient> = RecordCodecBuilder.create { instance ->
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

    val isEmpty: Boolean
        get() = entryList.isEmpty || amount <= 0

    override fun test(fluid: Fluid, amount: Long): Boolean = when {
        fluid == Fluids.EMPTY || amount <= 0 -> this.isEmpty
        else -> fluid in entryList && amount >= this.amount
    }
}
