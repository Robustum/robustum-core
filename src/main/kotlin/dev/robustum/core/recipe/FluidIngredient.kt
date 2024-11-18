package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.tag.ServerTagManagerHolder
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
                    RegistryEntryList
                        .codec(Registry.FLUID, ServerTagManagerHolder.getTagManager()::getFluids)
                        .fieldOf("fluids")
                        .forGetter(FluidIngredient::entryList),
                    Codec.LONG.optionalFieldOf("amount", FluidConstants.BUCKET).forGetter(FluidIngredient::amount),
                ).apply(instance, ::FluidIngredient)
        }
    }

    constructor(tag: Tag<Fluid>, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.tag(tag), amount)

    constructor(fluid: Fluid, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.of(fluid), amount)

    constructor(fluids: List<Fluid>, amount: Long = FluidConstants.BUCKET) : this(RegistryEntryList.of(fluids), amount)

    val isEmpty: Boolean
        get() = entryList.isEmpty || amount <= 0

    override fun test(fluid: Fluid, amount: Long): Boolean = when {
        fluid == Fluids.EMPTY || amount <= 0 -> this.isEmpty
        else -> fluid in entryList && amount >= this.amount
    }
}
