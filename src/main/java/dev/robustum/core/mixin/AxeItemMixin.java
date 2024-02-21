package dev.robustum.core.mixin;

import dev.robustum.core.tool.RobustumTool;
import dev.robustum.core.tool.RobustumToolManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin extends MiningToolItem {

    protected AxeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return RobustumToolManager.INSTANCE.canMine(RobustumTool.AXE, state, getMaterial());
    }

    /**
     * @author Hiiragi283
     * @reason Replace with RobustumToolManager
     */
    @Overwrite
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return RobustumToolManager.INSTANCE.getMiningSpeed(RobustumTool.AXE, state, getMaterial());
    }

}