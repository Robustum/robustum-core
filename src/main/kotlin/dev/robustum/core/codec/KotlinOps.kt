package dev.robustum.core.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.robustum.core.extensions.DataPair
import it.unimi.dsi.fastutil.bytes.ByteList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.longs.LongList
import java.nio.ByteBuffer
import java.util.stream.Collectors
import java.util.stream.Stream

object KotlinOps : DynamicOps<Any> {
    override fun empty(): Any = Unit

    override fun <U : Any> convertTo(outOps: DynamicOps<U>, input: Any): U {
        if (input is Map<*, *>) {
            return convertMap(outOps, input)
        }
        if (input is ByteList) {
            return outOps.createByteList(ByteBuffer.wrap(input.toByteArray()))
        }
        if (input is IntList) {
            return outOps.createIntList(input.stream().mapToInt { it })
        }
        if (input is LongList) {
            return outOps.createLongList(input.stream().mapToLong { it })
        }
        if (input is List<*>) {
            return convertList(outOps, input)
        }
        if (input is String) {
            return outOps.createString(input)
        }
        if (input is Boolean) {
            return outOps.createBoolean(input)
        }
        if (input is Byte) {
            return outOps.createByte(input)
        }
        if (input is Short) {
            return outOps.createShort(input)
        }
        if (input is Int) {
            return outOps.createInt(input)
        }
        if (input is Long) {
            return outOps.createLong(input)
        }
        if (input is Float) {
            return outOps.createFloat(input)
        }
        if (input is Double) {
            return outOps.createDouble(input)
        }
        if (input is Number) {
            return outOps.createNumeric(input)
        }
        throw IllegalStateException("Unsupported class: $input")
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

    override fun getMapValues(input: Any): DataResult<Stream<Pair<in Any, in Any>>> {
        if (input is Map<*, *>) {
            return DataResult.success(
                input
                    .mapNotNull { (k, v) ->
                        if (k == null) return@mapNotNull null
                        if (v == null) return@mapNotNull null
                        DataPair.of(k, v)
                    }.stream(),
            )
        }
        return DataResult.error("Not a map: $input")
    }

    override fun createMap(map: Stream<Pair<in Any, in Any>>): Any = map.collect(Collectors.toMap({ it.first }, { it.second }))

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
