package dev.robustum.core.mixin;

import dev.robustum.core.tag.GlobalTagManager;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerTagManagerHolder.class)
public class ServerTagManagerHolderMixin {

    @Shadow
    private static volatile TagManager tagManager;

    @Inject(method = "getTagManager", at = @At("TAIL"), cancellable = true)
    private static void robustum$getTagManager(CallbackInfoReturnable<TagManager> cir) {
        cir.setReturnValue(GlobalTagManager.createWrappedManager(tagManager));
    }

}