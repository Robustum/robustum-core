package dev.robustum.core.tool

data class RobustumTool(val name: String) {
    companion object {
        val AXE = RobustumTool("axe")
        val HOE = RobustumTool("hoe")
        val PICKAXE = RobustumTool("pickaxe")
        val SHOVEL = RobustumTool("shovel")
        val SWORD = RobustumTool("sword")
    }
}
