package dev.robustum.core.mixin;

import dev.robustum.core.event.TagsUpdatedEvent;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.tag.TagManagerLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerResourceManager.class)
public class ServerResourceManagerMixin {
    @Shadow @Final private TagManagerLoader registryTagManager;

    @Inject(method = "loadRegistryTags", at = @At("TAIL"))
    private void ht_materials$loadRegistryTags(CallbackInfo ci) {
        TagsUpdatedEvent.EVENT.invoker().onUpdated(this.registryTagManager.getTagManager(), false);
    }
}