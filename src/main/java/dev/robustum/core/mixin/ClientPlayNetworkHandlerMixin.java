package dev.robustum.core.mixin;

import dev.robustum.core.event.TagsUpdatedEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private TagManager tagManager;

    @Inject(method = "onSynchronizeTags", at = @At("TAIL"))
    private void ht_materials$onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        TagsUpdatedEvent.EVENT.invoker().onUpdated(this.tagManager, true);
    }
}