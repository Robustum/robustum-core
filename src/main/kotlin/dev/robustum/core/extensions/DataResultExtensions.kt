package dev.robustum.core.extensions

import com.mojang.serialization.DataResult
import dev.robustum.core.util.Option
import dev.robustum.core.util.kotlin

//    DataResult    //

/**
 * 存在する値がある場合はtrueを返し、それ以外の場合はfalseを返します。
 */
val <R> DataResult<R>.isSucceeded: Boolean get() = result().isPresent

/**
 * 存在する値がない場合はtrueを返し、それ以外の場合はfalseを返します。
 */
val <R> DataResult<R>.isErrored: Boolean get() = error().isPresent

/**
 * [DataResult]が値を保持している場合は指定された[action]をその値で呼び出し、それ以外の場合は何も行いません。
 * @param action 値が存在する場合に実行されるブロック
 */
fun <R> DataResult<R>.onSucceeded(action: (R) -> Unit): DataResult<R> = apply { result().ifPresent(action) }

/**
 * [DataResult]が値を保持していない場合は指定された[action]をその値で呼び出し、それ以外の場合は何も行いません。
 * @param action 値が存在しない場合に実行されるブロック
 */
fun <R> DataResult<R>.onErrored(action: (String) -> Unit): DataResult<R> =
    apply { error().map(DataResult.PartialResult<R>::message).ifPresent(action) }

/**
 * 指定された[predicate]で検証した[DataResult]を返します。
 * @param R 値のクラス
 * @param predicate 値を[Boolean]で評価する。
 * @param errorMessage [predicate]がfalseの場合のエラー文
 * @return [predicate]で評価された[DataResult]
 */
fun <R> DataResult<R>.filter(predicate: (R) -> Boolean, errorMessage: String): DataResult<R> = flatMap { result: R ->
    when (predicate(result)) {
        true -> DataResult.success(result)
        false -> DataResult.error(errorMessage)
    }
}

/**
 * 指定された[predicate]で検証した[DataResult]を返します。
 * @param R 値のクラス
 * @param predicate 値を[Boolean]で評価する。
 * @param errorMessage [predicate]がtrueの場合のエラー文
 * @return [predicate]で評価された[DataResult]
 */
fun <R> DataResult<R>.filterNot(predicate: (R) -> Boolean, errorMessage: String): DataResult<R> = flatMap { result: R ->
    when (predicate(result)) {
        true -> DataResult.error(errorMessage)
        false -> DataResult.success(result)
    }
}

/**
 * 指定された[DataResult]の値を[Option]に包んで返します。
 */
fun <R : Any> DataResult<R>.getOption(): Option<R> = get().left().kotlin

/**
 * 指定された[DataResult]の値をnullableな形で返します。
 */
fun <R : Any> DataResult<R>.getOrNull(): R? = getOption().getOrNull()
