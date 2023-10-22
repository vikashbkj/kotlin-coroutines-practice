import kotlinx.coroutines.*

fun main() {
//    handleException()
//    handleExceptionUsingJob()
//    handleExceptionUsingSupervisorJob()

    runBlocking {
        tryCatchExceptionHandlingExample()
    }
}

fun handleException() {
    runBlocking {
        launch {
            launch {
                delay(1000)
                throw Error("Some error")
//                throw CancellationException()
            }
            launch {
                delay(2000)
                println("(1) Will not be printed")
            }
            launch {
                delay(500) // faster than the exception
                println("Will be printed")
            }
        }
        launch {
            delay(2000)
            println("(2) Will not be printed")
        }
    }
}

// CancellationException does not crash because CancellationException is handled by the framework but Exception crashes the application
fun handleExceptionUsingJob() {
    runBlocking {
        launch {
            delay(500)
            println("Parent coroutine of job1 and job2")
            val job1 = launch {
                launch {
                    delay(500)
                    println("Text 1-1")
                    throw CancellationException("Cancelled exceptionally")
//                    throw ArithmeticException("Some Exception")
//                    throw Error("Some Exception")
                }
                launch {
                    delay(1000)
                    println("Text 1-2")
                }
            }

            val job2 = launch {
                launch {
                    delay(1000)
                    println("Text 2-1")
                }
                launch {
                    delay(1000)
                    println("Text 2-2")
                }
            }

//            delay(600)
//            job1.cancel()
//            job1.join()
            println("Cancelled Successfully!")
        }
    }
}

// If SupervisorJob is used as parent and if any of its child throw an exception the SupervisorJob ignores the exception and none of the other children are affected
// If SupervisorJob is used as parent, and it throws an exception then all its children are cancelled
fun handleExceptionUsingSupervisorJob() {
    runBlocking {
        launch {
            val supervisorJob = launch(SupervisorJob()) {
                delay(1000)
//                throw Error("Some error")
//                throw CancellationException()

                launch {
                    delay(1500)
                    println("(1) SupervisorJob child")
                    throw Error("Some error")
                }
                launch {
                    delay(2000)
                    println("(2) SupervisorJob child")
                }
            }

            supervisorJob.join()

            launch {
                delay(2000)
                println("(1) Will not be printed")
            }
            launch {
                delay(500) // faster than the exception
                println("Will be printed")
            }
        }
        launch {
            delay(2000)
            println("(2) Will not be printed")
        }
    }
}

// supervisorScope creates a CoroutineScope with SupervisorJob. It inherits the parent CoroutineContext and overrides the Job with SupervisorJob.

suspend fun tryCatchExceptionHandlingExample() = coroutineScope {
    val name:  CoroutineName = CoroutineName("Some_Name")

    val parentJob = coroutineContext.job
    println("Parent Job :: $parentJob")

    launch(name) {
        println()
        println("(A) Job :: ${coroutineContext.job}")
        println()

        supervisorScope {
            println("supervisorScope coroutine name:  ${coroutineContext[CoroutineName]}")

            val childJob = coroutineContext.job
            println()
            println("supervisorScope Child Job :: $childJob")
            println()

            println("supervisorScope childJob Job Children Count :: ${childJob.children.count()}")

            launch {
                println()
                println("(A)(1) Job :: ${coroutineContext.job}")
                println()

                delay(500)
                println("(A) Text 1")
//                try {
                throw Exception("My Exception")
//                } catch (e: Exception) {
//                    println("Exception found :: $e")
//                }
            }
            launch {
                println()
                println("(A)(2) Job :: ${coroutineContext.job}")
                println()

                delay(2000)
                println("(A) Text 2")
            }
        }
    }
    launch {
        println()
        println("(B) Job :: ${coroutineContext.job}")
        println()

        delay(2000)
        println("(B) Text")
    }
    launch {
        println()
        println("(C) Job :: ${coroutineContext.job}")
        println()

        delay(500)
        println("(C) Text")
    }

    println()
    if (parentJob.children.count() > 0) {
        parentJob.children.forEach { cJob ->
            println("cJob ::  $cJob")
        }
    }
    println()
}