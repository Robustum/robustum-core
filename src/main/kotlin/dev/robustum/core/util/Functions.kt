package dev.robustum.core.util

/**
 * 同じクラスの値を返す処理を表すエイリアスです。
 *
 * 参照 : [Java - UnaryOperator][java.util.function.UnaryOperator]
 */
typealias Identity<T> = (T) -> T

/**
 * 恒等操作を行うブロックを返します。
 */
fun <T> identity(): Identity<T> = { it }
