package dev.robustum.core.extension

import com.google.common.collect.Table

//    Table    //

fun <R : Any, C : Any, V : Any> Table<R, C, V>.toList(): List<Triple<R, C, V>> {
    if (size() == 0) return emptyList()
    val iterator: Iterator<Table.Cell<R, C, V>> = cellSet().iterator()
    if (!iterator.hasNext()) return emptyList()
    val first: Table.Cell<R, C, V> = iterator.next()
    if (!iterator.hasNext()) return listOfNotNull(first.toTriple())
    val result = ArrayList<Triple<R, C, V>>(size())
    first.toTriple()?.let(result::add)
    do {
        iterator.next().toTriple()?.let(result::add)
    } while (iterator.hasNext())
    return result
}

inline fun <R, C, V, T> Table<out R, out C, V>.mapNotNull(transform: (Table.Cell<out R, out C, V>) -> T): List<T> {
    return mapNotNullTo(ArrayList(size()), transform)
}

inline fun <R, C, V, T> Table<out R, out C, V>.map(transform: (Table.Cell<out R, out C, V>) -> T): List<T> {
    return mapTo(ArrayList(size()), transform)
}

inline fun <R, C, V, T, U : MutableCollection<in T>> Table<out R, out C, V>.mapNotNullTo(
    destination: U,
    transform: (Table.Cell<out R, out C, V>) -> T,
): U {
    forEach { cell -> transform(cell)?.let { destination.add(it) } }
    return destination
}

inline fun <R, C, V, T, U : MutableCollection<in T>> Table<out R, out C, V>.mapTo(
    destination: U,
    transform: (Table.Cell<out R, out C, V>) -> T,
): U {
    for (cell in cellSet()) destination.add(transform(cell))
    return destination
}

inline fun <R, C, V> Table<R, C, V>.forEach(action: (Table.Cell<R, C, V>) -> Unit) {
    for (cell: Table.Cell<R, C, V> in this.cellSet()) action(cell)
}

inline fun <R : Any, C : Any, V : Any> Table<R, C, V>.computeIfAbsent(row: R, column: C, mapping: (R, C) -> V): V {
    var result: V? = this.get(row, column)
    if (result == null) {
        this.put(row, column, mapping(row, column))
        result = this.get(row, column)!!
    }
    return result
}

fun <R : Any, C : Any, V : Any> Table.Cell<R, C, V>.toTriple(): Triple<R, C, V>? {
    val row: R? = this.rowKey
    val column: C? = this.columnKey
    val value: V? = this.value
    return if (row == null || column == null || value == null) null else Triple(row, column, value)
}
