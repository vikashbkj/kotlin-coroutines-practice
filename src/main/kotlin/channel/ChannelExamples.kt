package channel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*


fun main() {
//    simpleChannelExample()
//    produceChannelExample()
//    onBufferOverflowExample()
    fanOutExample()
}

fun simpleChannelExample() {
    runBlocking {
        coroutineScope {
            val channel = Channel<Int>()
            launch {
                repeat(5) {index ->
                    delay(1000)
                    println("Producing Next :: index  :: $index")
                    channel.send(index * 2)
                }
            }

            launch {
//                for (element in channel) { // Channel is not closed automatically
//                    println("Received :: element $element")
//                }
                channel.consumeEach { consumedElement -> // Channel is not closed automatically
                    println("Received :: consumedElement $consumedElement")
                }
            }
        }
    }
}

// produce returns ReceiveChannel instance
// The reason to use produce is that it closes the channel whenever the coroutine ends in any way(cancelled, completed, stopped)
fun produceChannelExample() {
    runBlocking {
        coroutineScope {
            val channel = produce {
                repeat(5) {item ->
                    delay(1000)
                    println("Producing Next :: item  :: $item")
                    send(item * 2)
                }
            }

            channel.consumeEach { consumedElement ->
                println("Consumed :: consumedElement $consumedElement")
            }

        }
    }
}

fun onBufferOverflowExample() {
    runBlocking {
        coroutineScope {
            val channel = Channel<Int>(capacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)

            launch {
                repeat(5) {item ->
                    channel.send(item * 2)
                    delay(1000)
                    println("Producing Next :: item  :: $item")
                }

                channel.cancel()
            }

            delay(1000)
            for (element in channel) {
                println(element)
                delay(1000)
            }
        }
    }
}

// Fan-out: multiple coroutines can receive from a single channel
// Do not use consumeEach to receive from multiple coroutines instead use for-loop
fun fanOutExample() {
    runBlocking {
        coroutineScope {
            launch {
                val channel = produceNumbers()
                repeat(3) { id ->
                    delay(10)
                    launchProcessor(id, channel)
                }
            }
        }
    }
}

fun CoroutineScope.produceNumbers(): ReceiveChannel<Int> {
     return produce {
        repeat(10) {
            delay(100)
            send(it)
        }
    }
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) {
    launch {
        for (msg in channel) {
            println("id: $id received msg :: $msg")
        }
    }
}
