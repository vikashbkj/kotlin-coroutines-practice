package flowandsequence

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.Exception


suspend fun main() {
//    onEachExample()
//    onStartExample()
//    onCompletionExample()
//    onEmptyExample()
//    catchExampleUpstream()
//    catchExampleDownstream()
//    moveOperationsFromCollectToOnEachAndBeforeCatch()
//    passingContextToFlowFunctionsLambdaExample()
//    flowOnExample()
//    launchInExample()
//    takeExample()
//    dropExample()
//    mergeExample()
//    zipExample()
    combineExample()
}

private suspend fun onEachExample() {
    flowOf(1, 2, 3, 4, 5)
        .onEach { println("onEach $it") }
        .collect { println("collect :: $it") }
}

private suspend fun onStartExample() {
    flowOf(1, 2, 3, 4, 5)
        .onEach { delay(1000) }
        .onStart { println("onStart :: Before") }
        .collect { println("collect :: $it") }
}

private suspend fun onCompletionExample() {
    flowOf(1, 2, 3, 4)
        .onEach {
            delay(1000)
            println("onEach :: $it")
//            throw Exception("My Exception")
        }
        .onCompletion { println("Completed :: $it") }
        .collect { println("collect :: $it") }
}

private suspend fun onEmptyExample() {
    val default = listOf(100)
    flow<List<Int>> {
        delay(1000)
    }.onEmpty {
        println(" :: onEmpty :: ")
        emit(default)
    }.onCompletion {
        println("onCompletion :: $it")
    }.collect {
        println(" :: collect :: $it")
    }
}

private suspend fun catchExample() {
    flow {
       emit(doSomeWork())
        emit(2)
    }.catch {
        println(":: catch :: $it")
    }.collect {
        println("collect :: $it")
    }
}

private fun doSomeWork(): Int  {
    throw Exception("My Exception")
    return 2
}

// catch can only be used to receive exceptions that occur upstream.
// That means if a exception is throw after catch function then it can not receive that exception.
private suspend fun catchExampleUpstream() {
    flow<String> {
//        emit("Message 1")
        throw Exception("My Exception")
    }.catch {
        println("catch :: Error!")
    }.onEach {
        println("onEach :: $it")
    }.collect { println("collect :: $it") }
}

private suspend fun catchExampleDownstream() {
    flowOf("Message 1")
        .catch { println("catch :: Error!") }
        .onEach {
            println("onEach :: $it")
            throw Exception("My Exception")
        }
        .collect { println("collect :: $it") }
}

// We should keep all operations in onEach and put it before catch function and do not keep any operation in collect
// so that every exception is caught in catch function.
private suspend fun moveOperationsFromCollectToOnEachAndBeforeCatch() {
    val flow = flow {
        emit("Message 1")
        emit("Message 2")
    }

    flow.onStart {
        println("onStart :: Before ")
    }.onEach {
        throw Exception("My Exception")
    }.catch { exception->
        println("catch :: $exception")
    }.collect()
}

private suspend fun passingContextToFlowFunctionsLambdaExample() {
    val users = getUsersFlow()
    withContext(CoroutineName("Name 1")) {
        users.collect {
            println(it)
        }
    }

    withContext(CoroutineName("Name 2")) {
        users.collect {
            println(it)
        }
    }
}

private fun getUsersFlow() = flow {
    repeat(2) {
        val ctx = currentCoroutineContext()
        val name = ctx[CoroutineName]?.name
        emit("User :: $it :: name :: $name")
    }
}

// flowOn function can be used to modify the coroutine context that is taken from collect function.
// Also, flowOn works only in upstream direction. That means flowOn context modification will only be applied
// to function that are above the flowOn function and flowOn function has no effect on flow functions that are
// called below flowOn.
suspend fun flowOnExample() {
    val messageFlow = messageFlow()
    withContext(CoroutineName("Name 1")) {
        messageFlow.flowOn(CoroutineName("Name 2"))
            .onEach { println("onEach :: $it") }
            .flowOn(CoroutineName("Name 3"))
            .collect()
    }
}

private fun messageFlow() = flow<String> {
    present("flow builder", "my message")
    emit("Message")
}

private suspend fun present(place: String, message: String) {
    val ctx = currentCoroutineContext()
    val name = ctx[CoroutineName]?.name
    println("name :: $name :: place :: $place :: message :: $message")
}

// process the flow in new coroutine in the given scope
private suspend fun launchInExample() {
    coroutineScope {
        println("Before flow starts")
        println()

        val flowJob = flowOf("user 1", "user 2")
            .onStart {
                println("onStart :: ")
            }.onEach {
                println("onEach :: $it")
            }.launchIn(this)

        println()
        println("After flow ends")
        println()

//        flowJob.cancel()
    }
}

//picks only given amount of elements from flow
private suspend fun takeExample() {
    val charR: CharRange = ('A'..'Z')
    charR.asFlow()
        .take(5)
        .onEach { println("onEach :: $it") }
        .collect()
}

// drops given amount of elements from flow
private suspend fun dropExample() {
    val intRange: IntRange = (1..100)
    intRange.asFlow()
        .drop(50)
        .onEach { println("onEach :: $it") }
        .collect()
}

// When we use merge the elements from one flow do not wait for another flow.
// If elements from one flow are delayed then merge do not stop element from other flow.
private suspend fun mergeExample() {
    val ints: Flow<Int> = flowOf(1, 2, 3)
        .onEach {
            delay(1000)
        }
    val doubles: Flow<Double> = flowOf(0.1, 0.2, 0.3)
    val together: Flow<Number> = merge(ints, doubles)
    together.collect { println(it) }
}

// zip collects elements from both the flows one by one and if one is faster then it waits for the slower.
// When it gets one element from each source then it produces new pair based on the action we define in lambda.
// If one of the pair closes then zip is also closed
private suspend fun zipExample() {
//    val flow1 = flowOf("A", "B") // If one of the pair closes then zip is also closed
    val flow1 = flowOf("A", "B", "C")
        .onEach {
            delay(100)
        }

    val flow2 = flowOf(1, 2, 3, 4)
        .onEach {
            delay(2000)
        }

    flow1.zip(flow2) { f1, f2 -> "${f1}_${f2}" }
        .collect { println(it) }
}

private suspend fun combineExample() {
    val flow1 = flowOf("A", "B", "C")
        .onEach {
            delay(400)
        }

    val flow2 = flowOf(1, 2, 3)
        .onEach {
            delay(1000)
        }

    flow1.combine(flow2) { f1, f2 -> "${f1}_${f2}" }
        .collect { println(it) }
}