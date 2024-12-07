package dev.robustum.core.mixin;

import dev.robustum.core.tag.RobustumTagEvents;
import net.fabricmc.api.EnvType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger(ClientPlayNetworkHandlerMixin.class);

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getSearchableContainer(Lnet/minecraft/client/search/SearchManager$Key;)Lnet/minecraft/client/search/SearchableContainer;"))
    private void robustum$onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        RobustumTagEvents.RELOAD.invoker().onReload(packet.getTagManager(), EnvType.CLIENT);
        LOGGER.info("Client TagManager reloaded!");
    }
}
