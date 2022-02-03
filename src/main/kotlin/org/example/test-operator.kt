package org.example

import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Date


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

fun fromDate(date: Date): YearMonth {
    return YearMonth.parse(DateFormatUtils.format(date, "yyyy-MM"))
}

fun fromLocalDate(localDate: LocalDate): YearMonth {
    return YearMonth.of(localDate.year, localDate.month)
}

fun fromLocalDateTime(localDateTime: LocalDateTime): YearMonth {
    return YearMonth.of(localDateTime.year, localDateTime.month)
}

fun YearMonth.toYear(): Year {
    return Year.of(this.year)
}

fun YearMonth.toDate(dayOfMonth: Int = 1): Date {
    return DateUtils.parseDate(this.toLocalDate(dayOfMonth).format(DateTimeFormatter.ISO_LOCAL_DATE), "yyyy-MM-dd")
}

fun YearMonth.toLocalDate(dayOfMonth: Int = 1): LocalDate {
    return LocalDate.of(this.year, this.month, dayOfMonth)
}

fun YearMonth.toLocalDateTime(dayOfMonth: Int = 1, hour: Int = 0, minute: Int = 0, second: Int = 0): LocalDateTime {
    return LocalDateTime.of(this.year, this.month, dayOfMonth, hour, minute, second)
}

fun YearMonth.prevMonth(): YearMonth {
    return this.minusMonths(1)
}

fun YearMonth.nextMonth(): YearMonth {
    return this.plusMonths(1)
}

fun YearMonth.prevYear(): YearMonth {
    return this.minusYears(1)
}

fun YearMonth.nextYear(): YearMonth {
    return this.plusYears(1)
}

fun YearMonth.withYear(year: Year): YearMonth {
    return YearMonth.of(year.value, this.monthValue)
}

fun YearMonth.withMonth(month: Month): YearMonth {
    return YearMonth.of(this.year, month)
}

class YearMonthRange(override val start: YearMonth, override val endInclusive: YearMonth) : Iterable<YearMonth>,
    ClosedRange<YearMonth> {
    override fun iterator(): Iterator<YearMonth> {
        return object : Iterator<YearMonth> {
            var current = start
            override fun hasNext(): Boolean {
                return current <= endInclusive
            }

            override fun next(): YearMonth {
                return current.apply { current = nextMonth() }
            }
        }
    }
}

operator fun YearMonth.rangeTo(other: YearMonth): YearMonthRange {
    return YearMonthRange(this, other)
}

fun main() {
    println((1 safeAdd 2))
    println(listOf(1,2,3).safeSum())

    (fromLocalDate(LocalDate.ofYearDay(2021,1))..YearMonth.of(2021,12)).forEach {
        println(it)
    }
}
