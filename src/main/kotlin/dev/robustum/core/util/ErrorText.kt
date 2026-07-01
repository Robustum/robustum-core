@file:OptIn(ExperimentalContracts::class)

package dev.robustum.core.util

import dev.robustum.core.text.HasText
import dev.robustum.core.text.toText
import net.minecraft.text.Text
import org.apache.logging.log4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 結果を[ErrorText]または値で表現する[Either]のエイリアスです。
 * @param T 値のクラス
 */
typealias TextResult<T> = Either<ErrorText, T>

/**
 * 新しい[TextResult]のインスタンスを作成します。
 * @param value エラーメッセージ
 */
fun TextResult(value: String): TextResult<Nothing> = ErrorText(value).left()

/**
 * [TextResult]に変換します。
 * @param T 値のクラス
 * @param message エラーメッセージを提供するブロック
 */
inline fun <T> T?.toTextResult(message: () -> String): TextResult<T> {
    contract {
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }
    return this?.right() ?: TextResult(message())
}

/**
 * エラーメッセージがある場合，それをログに出力します。
 * @param logger ログの出力先
 */
fun <T> TextResult<T>.printError(logger: Logger): TextResult<T> = this.onLeft { logger.error(it.value) }

/**
 * 保持している値を取得します。
 * @throws IllegalStateException 値がない場合
 */
fun <T> TextResult<T>.getOrThrow(): T = this.getOrElse { error(it.value) }

/**
 * エラーメッセージのラッパークラスです。
 */
@JvmInline
value class ErrorText(val value: String) : HasText {
    override fun getText(): Text = value.toText()
}
