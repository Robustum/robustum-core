package dev.robustum.core.extension

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.text.Text

fun PlayerEntity.openSimpleScreen(title: Text, factory: (Int, PlayerInventory, PlayerEntity) -> ScreenHandler) {
    openHandledScreen(SimpleNamedScreenHandlerFactory(factory, title))
}
