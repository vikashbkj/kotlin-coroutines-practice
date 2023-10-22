import kotlinx.coroutines.*

fun main() = runBlocking{
//    tryCatchWithCoroutinesScope()
    tryCatchWithSupervisorScope()
}

// In both coroutineScope and supervisorScope If try-catch is declared inside the coroutine where exception is thrown
// then it is handled there and catch block is executed and the exception is not propagated to the parent coroutine

// If parent scope is coroutineScope and Try-Catch is declared outside exception throwing launch builder(coroutine)
// then catch block is not executed because before coming to catch block the launch builder propagates the exception to its parent
// and all coroutines are cancelled due to structured concurrency is intact in coroutineScope
suspend fun tryCatchWithCoroutinesScope() {
    coroutineScope {
        launch {
            try {
                launch {
                    delay(1000)
                    println("(A) Text1")
                    throw Exception("Custom Exception")

//                try {
//                    throw Exception("Custom Exception")
//                }catch (e: Exception) {
//                    println("(A) exception caught :: $e")
//                }
                }
            } catch (e: Exception) {
                println("(A) exception caught :: $e")
            }

            launch {
                delay(2000)
                println("(A) Text2")
            }
        }

        launch {
            delay(2000)
            println("(B) Text1")
        }
    }
}

// If parent scope is supervisorScope and Try-Catch is declared outside exception throwing launch builder(coroutine)
// then catch block is executed and other sibling and parent coroutines are not cancelled
suspend fun tryCatchWithSupervisorScope() {
    supervisorScope {
        launch {
            try {
                launch {
                    delay(1000)
                    println("(A) Text1")
//                    throw Exception("Custom Exception")

//                    try {
//                        throw Exception("Custom Exception")
//                    }catch (e: Exception) {
//                        println("(A) exception caught :: $e")
//                    }
                }
            } catch (e: Exception) {
                println("(A) exception caught :: $e")
            }

            launch {
                delay(2000)
                println("(A) Text2")
            }
        }

        launch {
            delay(2000)
            println("(B) Text1")
        }
    }
}
