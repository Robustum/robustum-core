package dev.robustum.core.serialization

import com.mojang.serialization.Codec
import dev.robustum.core.util.Option
import dev.robustum.core.util.toOption
import java.util.Optional

/**
 * NBTやJSONに値を書き込むインターフェースです。
 */
interface ValueOutput {
    /**
     * 指定した[key]に値を書き込みます。
     * @param T 値のクラス
     * @param key 保存先のキー
     * @param codec [T]のコーデック
     * @param value 書き込む値
     */
    fun <T : Any> write(key: String, codec: Codec<T>, value: T?)

    fun <T : Any> writeOptional(key: String, codec: Codec<Optional<T>>, value: T?) {
        write(key, codec, Optional.ofNullable(value))
    }

    fun <T : Any> writeOption(key: String, codec: Codec<Option<T>>, value: T?) {
        write(key, codec, value.toOption())
    }

    fun isEmpty(): Boolean

    // Compound

    /**
     * 指定した[key]に[ValueOutput]を作ります。
     * @param key 保存先のキー
     * @return [key]に紐づけられた[ValueOutput]
     */
    fun child(key: String): ValueOutput

    // List

    /**
     * 指定した[key]に[ValueOutputList]を作ります。
     * @param key 保存先のキー
     * @return [key]に紐づけられた[ValueOutputList]
     */
    fun childrenList(key: String): ValueOutputList

    /**
     * 指定した[key]に[TypedOutputList]を作ります。
     * @param T [TypedOutputList]の要素のクラス
     * @param key 保存先のキー
     * @param codec [T]のコーデック
     * @return [key]に紐づけられた[TypedOutputList]
     */
    fun <T : Any> list(key: String, codec: Codec<T>): TypedOutputList<T>

    // Primitives

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putBoolean(key: String, value: Boolean)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putByte(key: String, value: Byte)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putShort(key: String, value: Short)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putInt(key: String, value: Int)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putLong(key: String, value: Long)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putFloat(key: String, value: Float)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putDouble(key: String, value: Double)

    /**
     * 指定した[key]に[value]を書き込みます。
     * @param key 保存先のキー
     * @param value 書き込む値
     */
    fun putString(key: String, value: String)

    /**
     * 要素の一覧を保持するインターフェース
     * @param T 要素のクラス
     */
    interface TypedOutputList<T : Any> {
        /**
         * この一覧が空か判定します。
         */
        val isEmpty: Boolean

        /**
         * 指定した[element]を追加します。
         */
        fun add(element: T)
    }

    /**
     * 要素の一覧を保持するインターフェース
     */
    interface ValueOutputList {
        /**
         * この一覧が空か判定します。
         */
        val isEmpty: Boolean

        /**
         * 新しく[ValueOutput]を追加し，その値を返します。
         */
        fun addChild(): ValueOutput

        /**
         * 最後の[ValueOutput]の要素を削除します。
         */
        fun discardLast()
    }
}
