package dev.robustum.core.mixin;

import dev.robustum.core.RobustumCore;
import dev.robustum.core.tag.RobustumTagEvents;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin<T> {
    @Final
    @Shadow
    private String entryType;

    @Nullable
    @Unique
    private Registry<?> findRegistry() {
        return switch (entryType) {
            case "block" -> Registry.BLOCK;
            case "item" -> Registry.ITEM;
            case "fluid" -> Registry.FLUID;
            case "entity_type" -> Registry.ENTITY_TYPE;
            default -> null;
        };
    }

    @Inject(method = "method_18243", at = @At("RETURN"))
    private void robustum$method_18243(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, Tag.Builder>> cir) {
        Registry<?> registry = findRegistry();
        if (registry == null) return;
        RobustumTagEvents.REGISTER.invoker().onRegister(new RobustumTagEvents.Helper(registry, (@NotNull Identifier tagId, Tag.@NotNull Entry entry) -> cir.getReturnValue().computeIfAbsent(tagId, k -> Tag.Builder.create()).add(entry, RobustumCore.MOD_NAME)));
    }
}
