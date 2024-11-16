package dev.robustum.core.extensions

import net.minecraft.util.Identifier

fun Identifier.prefix(prefix: String): Identifier = Identifier(this.namespace, prefix + this.path)

fun Identifier.suffix(suffix: String): Identifier = Identifier(this.namespace, this.path + suffix)

inline fun Identifier.modify(transform: (String) -> String): Identifier = Identifier(this.namespace, transform(this.path))

fun Identifier.removePrefix(prefix: String): Identifier = Identifier(this.namespace, this.path.removePrefix(prefix))

fun Identifier.removeSuffix(suffix: String): Identifier = Identifier(this.namespace, this.path.removeSuffix(suffix))
