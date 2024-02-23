package dev.robustum.core.mixin;

import dev.robustum.core.tool.RobustumTool;
import dev.robustum.core.tool.RobustumToolManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;

@Mixin(HoeItem.class)

public class HoeItemMixin extends MiningToolItem {
    protected HoeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return RobustumToolManager.INSTANCE.canMine(RobustumTool.HOE, state, getMaterial());
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return RobustumToolManager.INSTANCE.getMiningSpeed(RobustumTool.HOE, state, getMaterial());
    }

}