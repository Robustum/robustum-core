package dev.robustum.core.mixin;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Inject(method = "getItemStack", at = @At("HEAD"), cancellable = true)
    private static void robustum$getItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> cir) {
        if (json.has("tag")) {
            String tagId = JsonHelper.getString(json, "tag");
            int count = JsonHelper.getInt(json, "count", 1);
            Optional.ofNullable(ServerTagManagerHolder.getTagManager().getItems().getTag(new Identifier(tagId)))
                    .map(Tag::values)
                    .map(Collection::stream)
                    .flatMap(Stream::findFirst)
                    .ifPresent(item -> cir.setReturnValue(new ItemStack(item, count)));
        }
    }
}