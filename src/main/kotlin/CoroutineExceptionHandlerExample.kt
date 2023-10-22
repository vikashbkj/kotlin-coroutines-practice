import kotlinx.coroutines.*

fun main() = runBlocking {
    coroutineExceptionHandlerExample()
}

// CoroutineExceptionHandler does not stop exception propagation but it defines what should be done in case there is an exception
suspend fun coroutineExceptionHandlerExample() {
    val handler = CoroutineExceptionHandler { ctx, throwable ->
        println("Inside coroutine exception handler coroutineContext :: $ctx")
        println("Inside coroutine exception handler throwable :: $throwable")
    }
    val scope = CoroutineScope(handler)
//    val scope = CoroutineScope(SupervisorJob() + handler)

    scope.launch {
        delay(1000)
        throw Error("My Error.......")
    }
    scope.launch {
        delay(2000)
        println("Text 2")
    }

    delay(3000)
}