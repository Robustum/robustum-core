package dev.robustum.core.tag

interface TagFormatter {
    fun canFormat(path: String): Boolean

    fun format(path: String): String

    companion object {
        @JvmStatic
        fun conventional(name: String): TagFormatter = object : TagFormatter {
            override fun canFormat(path: String): Boolean = path.endsWith("_$name")

            override fun format(path: String): String = "$name/" + path.removeSuffix("_$name")
        }
    }
}
