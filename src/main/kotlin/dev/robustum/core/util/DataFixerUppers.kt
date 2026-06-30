package dev.robustum.core.util

import com.mojang.serialization.DataResult

//    DFUEither <-> Either    //

/**
 * DataFixerUpper由来の[Either][com.mojang.datafixers.util.Either]のエイリアスです。
 */
typealias DFUEither<A, B> = com.mojang.datafixers.util.Either<A, B>

/**
 * [DFUEither]を[Either]に変換します。
 */
val <A, B> DFUEither<A, B>.kotlin: Either<A, B> get() = this.map({ Either.Left(it) }, { Either.Right(it) })

/**
 * [Either]を[DFUEither]に変換します。
 */
val <A, B> Either<A, B>.java: DFUEither<A, B> get() = this.fold({ DFUEither.left(it) }, { DFUEither.right(it) })

//    DFUPair <-> Pair    //

/**
 * DataFixerUpper由来の[Either][com.mojang.datafixers.util.Pair]のエイリアスです。
 */
typealias DFUPair<A, B> = com.mojang.datafixers.util.Pair<A, B>

/**
 * [DFUPair]を[Pair]に変換します。
 */
val <A, B> DFUPair<A, B>.kotlin: Pair<A, B> get() = this.first to this.second

/**
 * [Pair]を[DFUPair]に変換します。
 */
val <A, B> Pair<A, B>.java: DFUPair<A, B> get() = DFUPair.of(this.first, this.second)

//    DataResult <-> TextResult    //

fun <R> DataResult<R>.toTextResult(): TextResult<R> = this
    .get()
    .kotlin
    .swap()
    .mapLeft { ErrorText(it.message()) }

fun <R> TextResult<R>.toDataResult(): DataResult<R> = this.fold({ DataResult.error(it.value) }, DataResult<R>::success)
