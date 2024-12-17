package dev.robustum.core.mixin.tag;

import dev.robustum.core.tag.RobustumTagEvents;
import net.fabricmc.api.EnvType;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.TagManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerTagManagerHolder.class)
public abstract class ServerTagManagerHolderMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger(ServerTagManagerHolderMixin.class);
    
    @Inject(method = "setTagManager", at = @At("TAIL"))
    private static void robustum$setTagManager(TagManager tagManager, CallbackInfo ci) {
        RobustumTagEvents.RELOAD.invoker().onReload(tagManager, EnvType.SERVER);
        LOGGER.info("Server TagManager reloaded!");
    }
}
