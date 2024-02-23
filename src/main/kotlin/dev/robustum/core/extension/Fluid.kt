@file:Suppress("UnstableApiUsage")

package dev.robustum.core.extension

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.Block
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

val Fluid.id: Identifier
    get() = Registry.FLUID.getId(this)

private val EMPTY_RENDER_HANDLER = FluidRenderHandler { _, _, _ -> arrayOf() }

val Fluid.renderer: FluidRenderHandler
    get() = FluidRenderHandlerRegistry.INSTANCE.get(this) ?: EMPTY_RENDER_HANDLER

val Fluid.variantRenderer: FluidVariantRenderHandler
    get() = FluidVariantRendering.getHandlerOrDefault(this)

val FluidVariant.renderer: FluidVariantRenderHandler
    get() = this.fluid.variantRenderer

fun Fluid.asBlock(): Block = this.defaultState.blockState.block

fun Fluid.isEmpty(): Boolean = this == Fluids.EMPTY

fun Fluid.notEmptyOrNull(): Fluid? = takeUnless { isEmpty() }

fun Fluid.isFlowable(): Boolean = this is FlowableFluid

fun Fluid.asFlowableOrNull(): FlowableFluid? = this.castTo()
