@file:OptIn(ExperimentalContracts::class)

package dev.robustum.core.extensions

import net.minecraft.util.Identifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 指定された[prefix]でパスを前置した[Identifier]を返します。
 * @param prefix パスの前置詞
 * @return [prefix]でパスを前置した新しい[Identifier]
 */
fun Identifier.prefix(prefix: String): Identifier = Identifier(this.namespace, prefix + this.path)

/**
 * 指定された[suffix]でパスを後置した[Identifier]を返します。
 * @param suffix パスの後置詞
 * @return [suffix]でパスを高知した新しい[Identifier]
 */
fun Identifier.suffix(suffix: String): Identifier = Identifier(this.namespace, this.path + suffix)

/**
 * 指定された[transform]でパスを変換した[Identifier]を返します。
 * @param transform [Identifier]のパスを変換するブロック
 * @return [transform]でパスを置換した新しい[Identifier]
 */
inline fun Identifier.modify(transform: (String) -> String): Identifier {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return Identifier(this.namespace, transform(this.path))
}

/**
 * 指定された[prefix]でパスの前置詞を削除した[Identifier]を返します。
 * @param prefix 削除するパスの前置詞
 * @return [prefix]でパスの前置詞を削除した新しい[Identifier]
 */
fun Identifier.removePrefix(prefix: String): Identifier = Identifier(this.namespace, this.path.removePrefix(prefix))

/**
 * 指定された[suffix]でパスの後置詞を削除した[Identifier]を返します。
 * @param suffix 削除するパスの後置詞
 * @return [suffix]でパスの後置詞を削除した新しい[Identifier]
 */
fun Identifier.removeSuffix(suffix: String): Identifier = Identifier(this.namespace, this.path.removeSuffix(suffix))
