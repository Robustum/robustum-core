package dev.robustum.core.codec

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.robustum.core.util.DFUPair
import it.unimi.dsi.fastutil.bytes.ByteList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.longs.LongList
import java.nio.ByteBuffer
import java.util.stream.Collectors
import java.util.stream.Stream

data object KotlinOps : DynamicOps<Any> {
    override fun empty(): Any = Unit

    override fun <U : Any> convertTo(outOps: DynamicOps<U>, input: Any): U = when (input) {
        is Map<*, *> -> convertMap(outOps, input)
        is ByteList -> outOps.createByteList(ByteBuffer.wrap(input.toByteArray()))
        is IntList -> outOps.createIntList(input.stream().mapToInt { it })
        is LongList -> outOps.createLongList(input.stream().mapToLong { it })
        is List<*> -> convertList(outOps, input)
        is String -> outOps.createString(input)
        is Boolean -> outOps.createBoolean(input)
        is Byte -> outOps.createByte(input)
        is Short -> outOps.createShort(input)
        is Int -> outOps.createInt(input)
        is Long -> outOps.createLong(input)
        is Float -> outOps.createFloat(input)
        is Double -> outOps.createDouble(input)
        is Number -> outOps.createNumeric(input)
        else -> throw IllegalStateException("Unsupported class: $input")
    }

    override fun getNumberValue(input: Any): DataResult<Number> = when (input) {
        is Number -> DataResult.success(input)
        else -> DataResult.error("Not a number: $input")
    }

    override fun createNumeric(i: Number): Any = i

    override fun getStringValue(input: Any): DataResult<String> = when (input) {
        is String -> DataResult.success(input)
        else -> DataResult.error("Not a string: $input")
    }

    override fun createString(value: String): Any = value

    override fun mergeToList(list: Any, value: Any): DataResult<in Any> {
        if (list == empty()) {
            return DataResult.success(listOf(value))
        }
        if (list is List<*>) {
            if (list.isEmpty()) {
                return DataResult.success(listOf(value))
            }
            return DataResult.success(
                buildList {
                    addAll(list)
                    add(value)
                },
            )
        }
        return DataResult.error("Not a list: $list")
    }

    override fun mergeToMap(map: Any, key: Any, value: Any): DataResult<in Any> {
        if (map == empty()) {
            return DataResult.success(mapOf(key to value))
        }
        if (map is Map<*, *>) {
            if (map.isEmpty()) {
                return DataResult.success(mapOf(key to value))
            }
            return DataResult.success(
                buildMap {
                    putAll(map)
                    put(key, value)
                },
            )
        }
        return DataResult.error("Not a map: $map")
    }

    override fun getMapValues(input: Any): DataResult<Stream<DFUPair<in Any, in Any>>> = when (input) {
        is Map<*, *> ->
            DataResult.success(
                input
                    .mapNotNull { (k, v) ->
                        if (k == null) return@mapNotNull null
                        if (v == null) return@mapNotNull null
                        DFUPair.of(k, v)
                    }.stream(),
            )
        else -> DataResult.error("Not a map: $input")
    }

    override fun createMap(map: Stream<DFUPair<in Any, in Any>>): Any = map.collect(Collectors.toMap({ it.first }, { it.second }))

    override fun getStream(input: Any): DataResult<Stream<in Any>> = when (input) {
        is List<*> -> DataResult.success(input.stream())
        else -> DataResult.error("Not a list: $input")
    }

    override fun createList(input: Stream<in Any>): Any = input.toList()

    override fun remove(input: Any, key: String): Any = when (input) {
        is Map<*, *> -> buildMap {
            putAll(input)
            remove(key)
        }

        else -> input
    }
}
