package dev.robustum.core.mixin;

import dev.robustum.core.tool.RobustumToolManager;
import dev.robustum.core.tool.RobustumTool;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin {

    @Unique
    private ToolMaterial toolMaterial;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void robustum$init(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings, CallbackInfo ci) {
        this.toolMaterial = toolMaterial;
    }

    /**
     * @author Hiiragi283
     * @reason Replace with RobustumToolManager
     */
    @Overwrite
    public boolean isSuitableFor(BlockState state) {
        return RobustumToolManager.INSTANCE.canMine(RobustumTool.PICKAXE, state, toolMaterial);
    }

    /**
     * @author Hiiragi283
     * @reason Replace with RobustumToolManager
     */
    @Overwrite
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return RobustumToolManager.INSTANCE.getMiningSpeed(RobustumTool.PICKAXE, state, toolMaterial);
    }

}