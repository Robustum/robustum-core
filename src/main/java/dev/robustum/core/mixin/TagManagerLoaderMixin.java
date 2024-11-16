package dev.robustum.core.mixin;

import dev.robustum.core.tag.RobustumTagGroupLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagManager;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TagManagerLoader.class)
public abstract class TagManagerLoaderMixin {

    @Unique
    private final RobustumTagGroupLoader<Block> blockLoader = new RobustumTagGroupLoader<>(Registry.BLOCK, "tags/blocks");

    @Unique
    private final RobustumTagGroupLoader<Item> itemLoader = new RobustumTagGroupLoader<>(Registry.ITEM, "tags/items");

    @Unique
    private final RobustumTagGroupLoader<Fluid> fluidLoader = new RobustumTagGroupLoader<>(Registry.FLUID, "tags/fluids");

    @Unique
    private final RobustumTagGroupLoader<EntityType<?>> entityLoader = new RobustumTagGroupLoader<>(Registry.ENTITY_TYPE, "tags/entity_types");

    @Shadow
    private TagManager tagManager;
    
    @SuppressWarnings("unchecked")
    @Inject(method = "reload", at = @At("HEAD"), cancellable = true)
    private void robustumCore$reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        List<CompletableFuture<? extends TagGroup<?>>> rawMaps = List.of(
                CompletableFuture.supplyAsync(() -> blockLoader.load(manager), prepareExecutor),
                CompletableFuture.supplyAsync(() -> itemLoader.load(manager), prepareExecutor),
                CompletableFuture.supplyAsync(() -> fluidLoader.load(manager), prepareExecutor),
                CompletableFuture.supplyAsync(() -> entityLoader.load(manager), prepareExecutor)
        );
        cir.setReturnValue(
                CompletableFuture.allOf(rawMaps.toArray(CompletableFuture[]::new))
                        .thenCompose(synchronizer::whenPrepared)
                        .thenAcceptAsync(void1 -> {
                            TagGroup<Block> blockGroup = (TagGroup<Block>) rawMaps.getFirst().join();
                            TagGroup<Item> itemGroup = (TagGroup<Item>) rawMaps.get(1).join();
                            TagGroup<Fluid> fluidGroup = (TagGroup<Fluid>) rawMaps.get(2).join();
                            TagGroup<EntityType<?>> entityGroup = (TagGroup<EntityType<?>>) rawMaps.get(3).join();
                            TagManager tagManager = TagManager.create(blockGroup, itemGroup, fluidGroup, entityGroup);
                            ServerTagManagerHolder.setTagManager(tagManager);
                            this.tagManager = tagManager;
                        }, applyExecutor)
        );
    }

}
