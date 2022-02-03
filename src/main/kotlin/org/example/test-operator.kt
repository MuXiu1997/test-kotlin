package org.example

import java.math.BigDecimal
import java.math.RoundingMode


fun toBigDecimal(any: Any?): BigDecimal {
    return try {
        BigDecimal(any?.toString() ?: "0")
    } catch (_: Exception) {
        BigDecimal.ZERO
    }
}

infix fun Any?.safeAdd(other: Any?): BigDecimal {
    return toBigDecimal(this) + toBigDecimal(other)
}

infix fun Any?.safeSub(other: Any?): BigDecimal {
    return toBigDecimal(this) - toBigDecimal(other)
}

infix fun Any?.safeMul(other: Any?): BigDecimal {
    return toBigDecimal(this) * toBigDecimal(other)
}

infix fun Any?.safeDiv(other: Any?): BigDecimal {
    return safeDiv(this, other)
}

fun safeDiv(a: Any?, b: Any?, scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): BigDecimal {
    return try {
        toBigDecimal(a).divide(toBigDecimal(b), scale, roundingMode)
    } catch (ignored: Exception) {
        BigDecimal.ZERO.setScale(scale)
    }
}

fun safeSum(vararg objects: Any?): BigDecimal {
    return objects.fold(BigDecimal.ZERO) { a, b -> a safeAdd b }
}

fun <T> Iterable<T>?.safeSum(): BigDecimal {
    return this?.fold(BigDecimal.ZERO) { a, b -> a safeAdd b } ?: BigDecimal.ZERO
}

fun <T> Sequence<T>?.safeSum(): BigDecimal {
    return this?.fold(BigDecimal.ZERO) { a, b -> a safeAdd b } ?: BigDecimal.ZERO
}


fun <T> Iterable<T>?.safeSumOf(selector: (T) -> Any?): BigDecimal {
    return this?.map(selector)?.fold(BigDecimal.ZERO) { a, b -> a safeAdd b } ?: BigDecimal.ZERO
}

fun <T> Sequence<T>?.safeSumOf(selector: (T) -> Any?): BigDecimal {
    return this?.map(selector)?.fold(BigDecimal.ZERO) { a, b -> a safeAdd b } ?: BigDecimal.ZERO
}

fun main() {
    println((1 safeAdd 2))
    println(listOf(1,2,3).safeSum())
}
