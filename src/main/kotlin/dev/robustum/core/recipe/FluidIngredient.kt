package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.getEntryOrThrow
import dev.robustum.core.registry.RegistryEntryList
import dev.robustum.core.registry.RegistryEntryListCodec
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.tag.Tag
import net.minecraft.util.registry.Registry
import java.util.function.BiPredicate

@Suppress("UnstableApiUsage")
class FluidIngredient private constructor(val entryList: RegistryEntryList<Fluid>, val amount: Long) : BiPredicate<Fluid, Long> {
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

    constructor(tag: Tag<Fluid>, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.tag(tag), amount)

    constructor(fluid: Fluid, amount: Long = FluidConstants.BUCKET) : this(
        RegistryEntryList.of(fluid, Registry.FLUID::getEntryOrThrow),
        amount,
    )

    val isEmpty: Boolean
        get() = entryList.isEmpty || amount <= 0

    override fun test(fluid: Fluid, amount: Long): Boolean = when {
        fluid == Fluids.EMPTY || amount <= 0 -> this.isEmpty
        else -> fluid in entryList && amount >= this.amount
    }
}
