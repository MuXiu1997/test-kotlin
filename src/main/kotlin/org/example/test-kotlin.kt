package org.example

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <reified T : Any> T?.ifNotNull(block: (T) -> Unit) {
    if (this != null) block(this)
}

inline fun <reified T : CharSequence> T?.ifNotBlank(block: (T) -> Unit) {
    if (!this.isNullOrBlank()) block(this)
}

inline fun <reified T > MutableList<T>.addIfNotNull(item: T?) {
    item.ifNotNull {
        this.add(it)
    }
}

fun <T, R> Iterable<T>.asyncMap(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    transform: suspend (T) -> R,
): List<R> = runBlocking {
    map { async(context, start) { transform(it) } }.awaitAll()
}

fun main() {
    "test".ifNotNull { println(it) }
    "test".ifNotBlank { println(it) }

    null.ifNotNull { println(it) }
    "".ifNotBlank { println(it) }

    mutableListOf<String>().addIfNotNull(null).also { println(it) }
    mutableListOf<String>().addIfNotNull("test").also { println(it) }

    listOf(1,2,3).asyncMap {
        delay(1000)
        it * it
    }.also {
        println(it)
    }
}
