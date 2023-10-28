package flowandsequence

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking


fun main() {
    runBlocking {
//        hotChannelExample()
        coldFlowExample()
    }
}

// Channel is hot source and stores the elements.
// Each item in channel can be consumed only once i.e. they can not be consumed by other receiver once all the items are consumed already.
// So other receivers receive empty channel only.
suspend fun hotChannelExample() {
    coroutineScope {
        val channel = makeChannel()

        delay(2000)
        println("Calling channel...")
        for (value in channel) {
            println(value)
//            break
        }

        println("Consuming again...")
        for (value in channel) {
            println(value)
        }
    }
}

fun CoroutineScope.makeChannel(): ReceiveChannel<Int> {
    return produce {
        repeat(3) {
            delay(1000)
            send(it)
        }
    }
}


// Flow is cold source and it does not store element s rather it generates element on-demand.
// Since items are generated on-demand so flow can be consumed as many times as you wish.
suspend fun coldFlowExample() {
    coroutineScope {
        val flow = makeFlow()

        delay(2000)
        println("Calling flow...")
        flow.collect { value ->
            println(value)
        }

        println("Consuming again...")
        flow.collect { value ->
            println(value)
        }
    }
}

private fun makeFlow(): Flow<Int> {
    val flow = flow<Int> {
        repeat(3) {
            delay(1000)
            emit(it)
        }
    }
    return flow
}
