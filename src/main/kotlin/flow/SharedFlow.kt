package flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect


fun main() = runBlocking {
//    sharedFlowExample1()
    sharedFlowExample2()
}

/**
 * SharedFlow is like a broadcasting channel where multiple subscribers can subscribe / listen to it.
 * However, it does not stop itself the subscriber must unsubscribe instead of closing the broadcasting channel(SharedFlow)
 * SharedFlow never ends even if you call collect or launchIn functions on shared flow instance.
 * There is an only way to stop shared flow listening is to cancel the subscriber / coroutine scope.
 * If replay value is set to 1 then it is similar to StateFLow because
 * it will now store the last state value and when a new subscribe joins it will receive the last emitted state
 */
private suspend fun sharedFlowExample1() {
    val sharedFlow = MutableSharedFlow<String>(replay = 0)

    coroutineScope {
        launch {
            sharedFlow.collect {
                println("#1 received $it")
            }
        }

        launch {
            sharedFlow.collect {
                println("#2 received $it")
            }
        }

        delay(1000)

        sharedFlow.emit("Message 1")
        sharedFlow.emit("Message 2")
//        cancel()
    }
}

/**
 * If you set replay parameter in MutableSharedFlow it maintains a cache of last number of replay values
 * You can also reset the cache by using resetReplayCache() function.
 */
private suspend fun sharedFlowExample2() {
    coroutineScope {
        val sharedFlow = MutableSharedFlow<String>(replay = 2)

        sharedFlow.emit("Message 1")
        sharedFlow.emit("Message 2")
        sharedFlow.emit("Message 3")

        println("cache :: ${sharedFlow.replayCache}")

        launch {
            sharedFlow.collect {
                println("#1 received $it")
            }
        }

        launch {
            sharedFlow.collect {
                println("#2 received $it")
            }
        }

        delay(1000)
        sharedFlow.resetReplayCache()
        println("cache :: ${sharedFlow.replayCache}")
    }
}
