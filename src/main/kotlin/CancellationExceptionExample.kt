import kotlinx.coroutines.*

fun main() = runBlocking {
    cancellationExceptionExample()
}

class MyCancellationException(name: String): CancellationException(name)


// CancellationException is not propagated to parent coroutines even if they are thrown inside coroutineScope.
// If a child throw a CancellationException only that coroutine and its children are cancelled but other sibling and parent coroutines are not affected
suspend fun cancellationExceptionExample() {
    coroutineScope {
        launch {
            launch {
                delay(2000)
                println("Text 1")
            }
            throw CancellationException("my custom exception")
        }
        launch {
            delay(2000)
            println("Text 2")
        }
    }
}
