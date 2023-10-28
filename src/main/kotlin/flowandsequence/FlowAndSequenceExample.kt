package flowandsequence

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


//fun main() {
////    returnMoreThanOneElementUsingCollection()
////    returnMoreThanOneElementUsingSequence()
////    blockingCallSequenceExample()
////    runBlocking {
////        flowExample()
////    }
//
//    runBlocking {
//        flowCollectExample()
////        sequenceForEachExample()
//    }
//}

// When there is need to return more than one element from a function we can use collection List or Set.
// But when we use List / Set we need to wait until all elements are calculated then the collection is returned at once.
fun returnMoreThanOneElementUsingCollection() {
    val list = getList()
    println("Function stated")

    list.forEach {
        println(it)
    }
}

fun getList(): List<String> {
    return List(3) {
        Thread.sleep(1000)
        "User $it"
    }
}


// When we need to return elements one by one we can use sequence.
// Unlike collection, it provided elements one-by-one on demand.
// Sequence does not support suspension, hence any suspending function can not be called from inside sequence.
// We can use sequence for complex and large amount of data(CPU intensive operation) as well as for blocking calls(reading file).
fun returnMoreThanOneElementUsingSequence() {
    val sequence = getSequence()
    println("Function stated....")

    sequence.forEach {
        println(it)
    }
}

// Sequence does not support suspension instead it supports blocking call(thread is blocked returned until it processes all items in sequence)
// Sequence forEach is a blocking operation.
fun blockingCallSequenceExample() {
    runBlocking {
        withContext(newSingleThreadContext("custom")) {
            launch {
                repeat(3) {
                    delay(10)
                    println("Processing on coroutine thread :: ${Thread.currentThread().name}")
                }
            }
            val sequence = getSequence()
            sequence.forEach {
                println("item :: $it thread :: ${Thread.currentThread().name}")
            }
        }
    }
}

// Flow is also used to return more than one element from a function one-by-one on demand.
// Flow supports suspension, hence we can use suspending functions inside flow.
// Flow supports structured concurrency and exception handling.
suspend fun flowExample() {
    withContext(newSingleThreadContext("custom")) {
        launch {
            repeat(3) {
                delay(1000)
                println("Processing on coroutine thread :: ${Thread.currentThread().name}")
            }
        }

        val flow = getFlow()
        flow.collect {
            println("item :: $it thread :: ${Thread.currentThread().name}")
        }
    }
}

// Flow's collect is not a blocking call but it is suspending call.
suspend fun flowCollectExample() {
    val flow = getFlow()
    flow.collect {
        println("item :: $it")

        printStringExample("A")
        printStringExample("B")
        printStringExample("C")
    }
    println("After flow")
}

// Sequence forEach is blocking call.
// Here it blocks the current thread until all elements of sequence are received.
fun sequenceForEachExample() {
    val sequence = getSequence()
    sequence.forEach {

        println("item :: $it") // Wait here until all items from sequence are received

        printStringExample("A")
        printStringExample("B")
        printStringExample("C")
    }
    println("After sequence")
}

fun printStringExample(str: String) {
    println("$str")
}

private fun getFlow(): Flow<String> {
    return flow {
        repeat(3) {
            delay(2000)
            emit("User $it")
        }
    }
}

private fun getSequence(): Sequence<String> {
    return sequence {
        repeat(3) {
            Thread.sleep(2000)
            yield("User $it")
        }
    }
}


//fun main() {
//    println("Tag main start")
//
//    sequenceExample().forEach { value ->
//        println("Tag sequence value: $value")
//    }
//
//    println("Tag main end")
//}
//
//private fun sequenceExample(): Sequence<Int>{
//    return sequence {
//        for (i in 1..3) {
//            Thread.sleep(3000)
//            yield(i)
//        }
//    }
//}


fun main() {
    println("Tag main start")

    runBlocking {
        launch {
            flowExample2().collect { value ->
                println("Tag flow value: $value")
            }
        }
    }

    println("Tag main end")
}
private fun flowExample2(): Flow<Int> {
    return flow {
        for (i in 1..3) {
            delay(1000)
            emit(i)
        }
    }
}
