package dev.robustum.core.extension

import net.minecraft.util.Identifier
import java.util.function.Function

fun Identifier.prefix(prefix: String) = Identifier(this.namespace, prefix + this.path)

fun Identifier.suffix(suffix: String) = Identifier(this.namespace, this.path + suffix)

fun Identifier.modify(function: Function<String, String>) = Identifier(this.namespace, function.apply(this.path))

fun Identifier.removePrefix(prefix: String) = Identifier(this.namespace, this.path.removePrefix(prefix))

fun Identifier.removeSuffix(suffix: String) = Identifier(this.namespace, this.path.removeSuffix(suffix))
