package dev.robustum.core.extension

inline fun <T, reified U> T.castTo(): U? = this as? U
