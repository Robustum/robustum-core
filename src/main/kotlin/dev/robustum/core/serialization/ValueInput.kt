package dev.robustum.core.serialization

import com.mojang.serialization.Codec
import dev.robustum.core.util.Option
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * NBTやJSONから値を読み取るインターフェースです。
 */
interface ValueInput {
    /**
     * 指定した[key]に対応する値を返します。
     * @param T 戻り値のクラス
     * @param key 保存先のキー
     * @param codec [T]のコーデック
     * @return 指定した[key]に値がない，または[codec]での変換に失敗した場合は`null`
     */
    fun <T : Any> read(key: String, codec: Codec<T>): T?

    fun <T : Any> readOptional(key: String, codec: Codec<Optional<T>>): T? = read(key, codec)?.getOrNull()

    fun <T : Any> readOption(key: String, codec: Codec<Option<T>>): T? = read(key, codec)?.getOrNull()

    // Compound

    /**
     * 指定した[key]から[ValueInput]を返します。
     * @param key 保存先のキー
     * @return 指定した[key]に値がない場合は`null`
     */
    fun child(key: String): ValueInput?

    /**
     * 指定した[key]から[ValueInput]を返します。
     * @param key 保存先のキー
     */
    fun childOrEmpty(key: String): ValueInput

    // List

    /**
     * 指定した[key]から[ValueInput]の[Iterable]を返します。
     * @param key 保存先のキー
     * @return 指定した[key]に値がない場合は`null`
     */
    fun childrenList(key: String): Iterable<ValueInput>?

    /**
     * 指定した[key]から[ValueInput]の[Iterable]を返します。
     * @param key 保存先のキー
     */
    fun childrenListOrEmpty(key: String): Iterable<ValueInput>

    /**
     * 指定した[key]から[Iterable]を返します。
     * @param T [Iterable]の要素のクラス
     * @param key 保存先のキー
     * @param codec [T]のコーデック
     * @return 指定した[key]に値がない，[codec]での変換に失敗した場合は`null`
     */
    fun <T : Any> list(key: String, codec: Codec<T>): Iterable<T>?

    /**
     * 指定した[key]から[Iterable]を返します。
     * @param T [Iterable]の要素のクラス
     * @param key 保存先のキー
     * @param codec [T]のコーデック
     */
    fun <T : Any> listOrEmpty(key: String, codec: Codec<T>): Iterable<T>

    // Primitives

    /**
     * 指定した[key]から[Boolean]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * 指定した[key]から[Byte]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getByte(key: String, defaultValue: Byte): Byte

    /**
     * 指定した[key]から[Short]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getShort(key: String, defaultValue: Short): Short

    /**
     * 指定した[key]から[Int]を返します。
     * @param key 保存先のキー
     * @return 指定した[key]に値がない場合は`null`
     */
    fun getInt(key: String): Int?

    /**
     * 指定した[key]から[Int]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getInt(key: String, defaultValue: Int): Int

    /**
     * 指定した[key]から[Long]を返します。
     * @param key 保存先のキー
     * @return 指定した[key]に値がない場合は`null`
     */
    fun getLong(key: String): Long?

    /**
     * 指定した[key]から[Long]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getLong(key: String, defaultValue: Long): Long

    /**
     * 指定した[key]から[Float]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getFloat(key: String, defaultValue: Float): Float

    /**
     * 指定した[key]から[Double]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getDouble(key: String, defaultValue: Double): Double

    /**
     * 指定した[key]から[String]を返します。
     * @param key 保存先のキー
     * @return 指定した[key]に値がない場合は`null`
     */
    fun getString(key: String): String?

    /**
     * 指定した[key]から[String]を返します。
     * @param key 保存先のキー
     * @param defaultValue 戻り値のデフォルト値
     * @return 指定した[key]に値がない場合は[defaultValue]
     */
    fun getString(key: String, defaultValue: String): String
}
