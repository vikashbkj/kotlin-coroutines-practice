import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random


fun main() {
//    defaultDispatcherExample()
    ioDispatcherExample()
}

fun defaultDispatcherExample() {
    runBlocking {
        coroutineScope {
            repeat(1000) {
                launch(Dispatchers.Default) {
                    List(1000) { Random.nextLong() }.maxOrNull() // To make it busy
                    val threadName = Thread.currentThread().name
                    println("Running on thread: $threadName")
                }
            }
        }
    }
}

fun ioDispatcherExample() {
    runBlocking {
        coroutineScope {
            repeat(1000) {
                launch(Dispatchers.IO) {
                    Thread.sleep(200)
                    val threadName = Thread.currentThread().name
                    println("Running on thread: $threadName")
                }
            }
        }
    }
}