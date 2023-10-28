package flowandsequence

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.Exception


suspend fun main() {
//  onEachExample()
//    onStartExample()
//    onCompletionExample()
    onEmptyExample()
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
