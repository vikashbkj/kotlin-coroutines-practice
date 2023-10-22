import kotlinx.coroutines.*
import kotlin.random.Random

// Job reference storing i.e. parent - child relation of jobs
//fun main(): Unit = runBlocking {
//    val name = CoroutineName("Some name")
//    val ctxJob = coroutineContext[Job]
//    println("ctxJob :: $ctxJob")
//
//    val factoryJob = Job()
//    println("factoryJob :: $factoryJob")
//
//   val coroutineJob1 = launch(name + factoryJob) {
//        val childName = coroutineContext[CoroutineName]
//        println(childName == name) // true
//
//        val childJob = coroutineContext[Job] //Job created by Coroutine builder
//        println("childJob :: $childJob")
//
//        println(childJob == factoryJob) // false
//        println(childJob == factoryJob.children.first()) // true
//
//        println("job.children.first() :: ${factoryJob.children.first()}")
//    }
//
//    println("coroutineJob1 :: $coroutineJob1")
//}


// lost structured concurrency because coroutine builder launch() argument Job replaces the parent job
//fun main(): Unit = runBlocking {
//    launch(Job()) { // the new job replaces one from parent
//        delay(1000)
//        println("Will not be printed")
//    }
//}

fun main() {
    // join()
//    joinFunctionExample()

    // Job()
//    programNotEndingProblem()
    jobFactoryFunctionAsParameter()
//    jobFactoryFunctionCheckChildJobAndCoroutineName()
//    solutionForProgramNotEndingProblem()

    //Cancellation
//    handleCancellationUsingJobFactoryFunction()
//    cancelJobExample()
//    jobParentCancellationExample()
//    jobChildrenCancellationExample()
//    canNotUseCancelledJobExample()
//    finallyBlockWithCancel()
}

// join() method
suspend fun joinFunctionExample() {
    runBlocking {
        val job1 = launch {
            delay(1000)
            println("Test1")
        }
        val job2 = launch {
            delay(2000)
            println("Test2")
        }
        job1.join()
        println("All tests are done")
    }
}

// Job() factory method (Job is an interface)
// Job created using Jon() factory function is not associated any CoroutineContext
// So even if all coroutines created using the job are completed the job still remains in active state and the program never ends
fun programNotEndingProblem() = runBlocking {
    coroutineScope {
        val name = CoroutineName("Some Name")
        val job = Job()
        launch(name + job) { // the new job replaces one from parent
            delay(1000)
            println("Text 1")
            val ctxName = coroutineContext[CoroutineName]
            println("ctxName :: $ctxName")
        }
        launch(job) { // the new job replaces one from parent
            delay(2000)
            println("Text 2")
        }

        job.join() // Here we will await forever
        println("Will not be printed")
    }
}

// When a job is created using coroutine builder then parent-child relationship of job is established hence structured concurrency is supported but
// When a job is created using Job)_ factory function then parent-child relationship is not established and structured concurrency is not supported
fun jobFactoryFunctionAsParameter() = runBlocking {
    val name = CoroutineName("Some_Name")
    launch(name) {
        println("Parent Job :: ${coroutineContext.job}")

        val job = launch(Job()) {
            delay(1000)
            println("Inside Job Factory Job :: ${coroutineContext.job}")
            println("coroutineContext.job.children.count() :: ${coroutineContext.job.children.count()}")

            println("Inside Job factory Coroutine :: ${coroutineContext[CoroutineName]}")
        }

        println("job : $job")
        println("job children count : ${job.children.count()}")
        println()
        job.join()
    }
}

// Job() factory method (Job is an interface)
suspend fun jobFactoryFunctionCheckChildJobAndCoroutineName() {
    coroutineScope {
        val ctxJob = coroutineContext.job
        val ctxName = coroutineContext[CoroutineName]
        println("ctxJob :: $ctxJob")
        println("ctxName :: $ctxName")

        val name = CoroutineName("Some Name")
        val job = Job()
        println("job factory function :: $job")

        launch(name + job) { // the new job replaces one from parent
            delay(1000)
            val name1 = coroutineContext[CoroutineName]
            val job1 = coroutineContext[Job]

            println(job1 == job.children.first())
            println("Text 1")
            println("name1 :: $name1")
            println("job1 :: $job1")
        }
        launch(job) { // the new job replaces one from parent
            delay(2000)

            val name2 = coroutineContext[CoroutineName]
            val job2 = coroutineContext[Job]

            println(job2 == job.children.first())
            println("Text 2")
            println("name2 :: $name2")
            println("job2 :: $job2")
        }

        job.join() // Here we will await forever
        println("Will not be printed")
    }
}

suspend fun solutionForProgramNotEndingProblem() {
    runBlocking {
        val job = Job()

        launch(job) {
            delay(1000)
            println("Text 1")
        }

        launch(job) {
            delay(2000)
            println("Text 2")
        }

//        job.join() // problem
        if (job.children.count() > 0) {
            for (child in job.children) {
                child.join()
            }
        }
        println("All child jobs representing coroutines completed")
    }
}

// Handles cancellation of the Job created by Job() factory function
fun handleCancellationUsingJobFactoryFunction() {
    runBlocking {
        val parentJob = Job()
        val job = Job(parentJob)

        launch(job) {
            delay(1000)
            println("Text 1")
        }

        launch(job) {
            delay(2000)
            println("Text 2")
        }

        delay(1100)
        parentJob.cancel()

        // This code block is not executed because job is cancelled when parentJob passed as parameter is cancelled
        job.children.forEach { child ->
            child.join()
        }
    }
}

//Basic cancellation

// When a job is cancelled the then coroutine execution represented by the job ends at first suspending function call in the coroutine
fun cancelJobExample() = runBlocking {
    coroutineScope {
        val job = launch {
            repeat(1_000) { i ->
                delay(200)
                println("Printing $i")
            }
        }
        delay(1100)
        job.cancel()
        job.join()
        println("Cancelled successfully")
    }
}

// When a parent job is cancelled its children are also cancelled
// When a parent job is canceled and child job tries to execute then a CancellationException is thrown
fun jobParentCancellationExample() {
    runBlocking {
        val parentJob = coroutineContext.job

        launch { // Child 1
            try {
                repeat(1000) { i ->
                    delay(200)
                    println("Printing $i")
                }
            } catch (e: CancellationException) {
                println("(1) Exception caught:: $e")

                println("(1) ctxJob.children.count() :: ${parentJob.children.count()}")
                parentJob.children.forEach { childJob ->
                    println("childJob :: $childJob")
                    println("childJob.isActive :: ${childJob.isActive}")
                    println("childJob.isCancelled :: ${childJob.isCancelled}")
                    println("childJob.isCompleted :: ${childJob.isCompleted}")

                    println("")
                }
                throw e
            }
        }
        launch { // Child 2
            try {
                repeat(1000) { i ->
                    delay(200)
                    println("Calling $i")
                }
            } catch (e: CancellationException) {
                println("(2) Exception caught:: $e")

                println("(2) ctxJob.children.count() :: ${parentJob.children.count()}")
                parentJob.children.forEach { childJob ->
                    println("childJob :: $childJob")
                    println("childJob.isActive :: ${childJob.isActive}")
                    println("childJob.isCancelled :: ${childJob.isCancelled}")
                    println("childJob.isCompleted :: ${childJob.isCompleted}")

                    println("")
                }
                throw e
            }
        }

        delay(700)
        parentJob.cancel()
        parentJob.join()

        println("Cancelled Successfully!") // this line is not printed because the representing this coroutine was cancelled
    }
}

// When child job is canceled then parent is not affected
// When child job is canceled then no CancellationException is thrown to the parent
fun jobChildrenCancellationExample() = runBlocking {
    val parentJob = coroutineContext.job

    val job1 = launch {
        repeat(1_000) { i ->
            delay(200)
            println("Printing $i")
        }
    }
    val job2 = launch {
        repeat(1_000) { i ->
            delay(200)
            println("Calling $i")
        }
    }

    delay(1100)
    job1.cancel()
    job1.join()

    job2.cancel()
    job2.join()

    println("parentJob.isCancelled :: ${parentJob.isCancelled}")
    println("job1.isCancelled :: ${job1.isCancelled}")
    println("job2.isCancelled :: ${job2.isCancelled}")
    println("Cancelled Successfully!")
}

// Once a job is cancelled its state changes to final state(i.e. from Cancelling to Cancelled) and the job can not be used further
fun canNotUseCancelledJobExample() {
    runBlocking {

        val job = launch {
            repeat(1_000) { i ->
                delay(200)
                println("Printing $i")
            }
        }

        delay(1100)
        job.cancel()
        job.join()

        launch(job) {
            println("Cancelled Job")
        }
        println("job.isCancelled :: ${job.isCancelled}")
        println("Cancelled Successfully!")
    }
}

fun finallyBlockWithCancel() {
    runBlocking {
        val job = Job()
        launch(job) {
            try {
                delay(2000)
                println("Done")
            } finally {
                print("Will always be printed")
            }
        }
        delay(1000)
        job.cancelAndJoin()
    }
}

