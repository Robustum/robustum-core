package dev.robustum.core.mixin;

import dev.robustum.core.impl.TagGroupLoaderMixinImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin {
    @Shadow @Final private String dataType;

    @Inject(method = "prepareReload", at = @At("HEAD"), cancellable = true)
    private void ht_materials$prepareReload(ResourceManager manager, Executor prepareExecutor, CallbackInfoReturnable<CompletableFuture<Map<Identifier, Tag.Builder>>> cir) {
        cir.setReturnValue(CompletableFuture.supplyAsync(() -> TagGroupLoaderMixinImpl.INSTANCE.loadTagMap(manager, dataType), prepareExecutor));
    }
}