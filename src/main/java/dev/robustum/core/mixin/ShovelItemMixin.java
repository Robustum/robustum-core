package dev.robustum.core.mixin;

import dev.robustum.core.tool.RobustumTool;
import dev.robustum.core.tool.RobustumToolManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;

@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin extends MiningToolItem {

    protected ShovelItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    /**
     * @author Hiiragi283
     * @reason Replace with RobustumToolManager
     */
    @Overwrite
    public boolean isSuitableFor(BlockState state) {
        return RobustumToolManager.INSTANCE.canMine(RobustumTool.SHOVEL, state, getMaterial());
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return RobustumToolManager.INSTANCE.getMiningSpeed(RobustumTool.SHOVEL, state, getMaterial());
    }

}