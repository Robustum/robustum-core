package dev.robustum.core.tool

import dev.robustum.core.extension.effectiveBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.item.Items
import net.minecraft.item.ToolMaterial
import java.util.*

object RobustumToolManager {
    // Mining Level
    private val blocks: MutableMap<RobustumTool, MutableMap<Block, Int>> = EnumMap(RobustumTool::class.java)
    private val materials: MutableMap<RobustumTool, MutableMap<Material, Int>> = EnumMap(RobustumTool::class.java)

    fun canMine(tool: RobustumTool, state: BlockState, toolMaterial: ToolMaterial): Boolean =
        getMiningLevel(tool, state) <= toolMaterial.miningLevel

    fun getMiningLevel(tool: RobustumTool, state: BlockState): Int =
        blocks[tool]?.get(state.block) ?: materials[tool]?.get(state.material) ?: 0

    fun setMiningLevel(tool: RobustumTool, block: Block, level: Int = 0) {
        blocks.computeIfAbsent(tool) { hashMapOf() }[block] = level
    }

    fun setMiningLevel(tool: RobustumTool, material: Material, level: Int = 0) {
        materials.computeIfAbsent(tool) { hashMapOf() }[material] = level
    }

    // Mining Speed
    fun getMiningSpeed(tool: RobustumTool, state: BlockState, toolMaterial: ToolMaterial): Float =
        if (canMine(tool, state, toolMaterial)) toolMaterial.miningSpeedMultiplier else 1.0f

    init {
        // Axe
        Items.WOODEN_AXE.effectiveBlocks.forEach { block: Block ->
            setMiningLevel(RobustumTool.AXE, block)
        }
        setMiningLevel(RobustumTool.AXE, Material.BAMBOO)
        setMiningLevel(RobustumTool.AXE, Material.GOURD)
        setMiningLevel(RobustumTool.AXE, Material.NETHER_WOOD)
        setMiningLevel(RobustumTool.AXE, Material.PLANT)
        setMiningLevel(RobustumTool.AXE, Material.REPLACEABLE_PLANT)
        setMiningLevel(RobustumTool.AXE, Material.WOOD)
        // Hoe
        Items.WOODEN_HOE.effectiveBlocks.forEach { block: Block ->
            setMiningLevel(RobustumTool.HOE, block)
        }
        // Pickaxe
        setMiningLevel(RobustumTool.PICKAXE, Blocks.ANCIENT_DEBRIS, 3)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.CRYING_OBSIDIAN, 3)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.DIAMOND_BLOCK, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.DIAMOND_ORE, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.EMERALD_BLOCK, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.EMERALD_ORE, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.GOLD_BLOCK, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.GOLD_ORE, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.NETHERITE_BLOCK, 3)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.OBSIDIAN, 3)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.REDSTONE_ORE, 2)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.RESPAWN_ANCHOR, 3)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.IRON_BLOCK, 1)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.IRON_ORE, 1)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.LAPIS_BLOCK, 1)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.LAPIS_ORE, 1)
        setMiningLevel(RobustumTool.PICKAXE, Material.STONE)
        setMiningLevel(RobustumTool.PICKAXE, Material.METAL)
        setMiningLevel(RobustumTool.PICKAXE, Material.REPAIR_STATION)
        setMiningLevel(RobustumTool.PICKAXE, Blocks.NETHER_GOLD_ORE)
        // Shovel
        Items.WOODEN_SHOVEL.effectiveBlocks.forEach { block: Block ->
            setMiningLevel(RobustumTool.SHOVEL, block)
        }
        setMiningLevel(RobustumTool.SHOVEL, Blocks.SNOW)
        setMiningLevel(RobustumTool.SHOVEL, Blocks.SNOW_BLOCK)
        // Sword
        setMiningLevel(RobustumTool.SWORD, Blocks.COBWEB)
        setMiningLevel(RobustumTool.SWORD, Material.GOURD)
        setMiningLevel(RobustumTool.SWORD, Material.LEAVES)
        setMiningLevel(RobustumTool.SWORD, Material.MOSS_BLOCK)
        setMiningLevel(RobustumTool.SWORD, Material.PLANT)
        setMiningLevel(RobustumTool.SWORD, Material.REPLACEABLE_PLANT)
    }
}
