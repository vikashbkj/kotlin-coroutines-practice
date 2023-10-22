import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.*

/* ############################################# */

//suspend fun main() {
//    println("Before ************** \n\n")
//
//    delay(1000)
//
//    println("After ************** \n\n")
//}

//suspend fun delay(milliSeconds: Long) {
//    val executors = Executors.newSingleThreadScheduledExecutor {
//        Thread(it, "scheduler").apply { isDaemon = true }
//    }
//    suspendCoroutine<Unit> { continuation ->
//        println("Suspended :: ${Thread.currentThread().name}")
//        println("${continuation.context}")
//
//        executors.schedule(
//            {
//                println("Schedule :: ${Thread.currentThread().name}")
//                continuation.resume(Unit)
//            },
//            1000L,
//            TimeUnit.MILLISECONDS
//        )
//    }
//}


/* ############################################# */

data class User(val name: String = "Test")

fun requestUserData() = User()

class MyException : Throwable("Just an exception")

//suspend fun main() {
//    println("Before ************** \n\n")
//
//    val user = requestUser()
//
//    println("user:: $user")
//
//    println("After ************** \n\n")
//}

suspend fun requestUser(): User {
    return suspendCancellableCoroutine { cancellableContinuation ->
//        cancellableContinuation.resumeWith(Result.success(requestUserData()))
        try {
            cancellableContinuation.resumeWithException(MyException())
        }catch (e: Throwable) {
            println("Error:: ${e.stackTrace}")
        }
    }
}

/* ############################################# */

// launch builder

//fun main() {
//    GlobalScope.launch {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    GlobalScope.launch {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    GlobalScope.launch {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    println("Hello,")
//    Thread.sleep(2000L)
//}

/* ############################################# */

// runBlocking builder

//fun main() {
//
//    runBlocking {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    runBlocking {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    runBlocking {
//        delay(Duration.ofMillis(1000L))
//        println("World!")
//    }
//    println("Hello,")
//}

//fun main() {
//    thread(name = "My_Thread") {
//        runBlocking {
//            delay(Duration.ofMillis(1000L))
//            println("World! :: ${Thread.currentThread().name}")
//
//        }
//        runBlocking {
//            delay(Duration.ofMillis(1000L))
//            println("World! :: ${Thread.currentThread().name}")
//        }
//        runBlocking {
//            delay(Duration.ofMillis(1000L))
//            println("World! :: ${Thread.currentThread().name}")
//        }
//    }
//    println("Hello, :: ${Thread.currentThread().name}")
//}

/* ############################################# */

// CoroutineContext

//fun main() {
////    val name: CoroutineName = CoroutineName("A Name")
////    val element: CoroutineContext.Element = name
////    val context: CoroutineContext = element
////
////    val job: Job = Job()
////    val jobElement: CoroutineContext.Element = job
////    val jobContext: CoroutineContext = jobElement
//
//    val ctx1: CoroutineContext = CoroutineName("My_Coroutine")
//    val coroutineName1: CoroutineName? = ctx1[CoroutineName] // ctx[CoroutineName.Key] // Companion object
//    val job1: Job? = ctx1[Job.Key] // ctx1[Job.Key]
//
//    println("coroutineName1 :: $coroutineName1")
//    println("coroutineName1 :: ${coroutineName1?.name}")
//    println("job1 :: $job1")
//    println("job1?.isActive :: ${job1?.isActive}")
//
//    println("\n")
//
//    val ctx2: CoroutineContext = Job()
//    val coroutineName2: CoroutineName? = ctx2[CoroutineName] // ctx2[CoroutineName.Key] // Companion object
//    val job2: Job? = ctx2[Job] // ctx[Job.Key]
//
//    println("coroutineName2 :: $coroutineName2")
//    println("coroutineName2 :: ${coroutineName2?.name}")
//    println("job2 :: $job2")
//    println("job2?.isActive :: ${job2?.isActive}")
//
//
//    // Adding Contexts
//    println("\n\n Adding elements to coroutine context \n")
//
//    val ctx3 = ctx1 + ctx2
//
//    val coroutineName3: CoroutineName? = ctx3[CoroutineName]
//    val job3: Job? = ctx3[Job]
//
//    println("coroutineName3 :: $coroutineName3")
//    println("coroutineName3 :: ${coroutineName3?.name}")
//    println("job3 :: $job3")
//    println("job3?.isActive :: ${job3?.isActive}")
//
//
//    //Empty coroutine context
//    println("\n\n Empty coroutine context \n")
//
//    val empty: CoroutineContext = EmptyCoroutineContext
//
//    val coroutineName4: CoroutineName? = empty[CoroutineName]
//    val job4: Job? = empty[Job]
//
//    println("coroutineName4 :: $coroutineName4")
//    println("coroutineName4 :: ${coroutineName4?.name}")
//    println("job4 :: $job4")
//    println("job4?.isActive :: ${job4?.isActive}")
//
//
//    //Subtracting elements
//    println("\n\n Subtracting elements from coroutine context \n")
//
//    val subCTX = CoroutineName("Name1") + Job()
//    println("SUB Coroutine Name Before Minus :: ${subCTX[CoroutineName]}")
//
//    val minusCTX: CoroutineContext = subCTX.minusKey(CoroutineName)
//    val coroutineName5: CoroutineName? = minusCTX[CoroutineName]
//    val job5: Job? = minusCTX[Job]
//
//    println("coroutineName5 :: $coroutineName5")
//    println("coroutineName5 :: ${coroutineName5?.name}")
//    println("job5 :: $job5")
//    println("job5?.isActive :: ${job5?.isActive}")
//
//    println("\n")
//}


/* ############################################# */

// Job

suspend fun main() {
    coroutineScope {
/*        val jobDefault = launch {
            println("Inside default active job state")
            delay(Duration.ofMillis(1000L))
        }

        println(jobDefault)
        jobDefault.start()
        println(jobDefault)
        jobDefault.join()
        println(jobDefault)

        println("\n\n")

        val lazyJob = launch(start = CoroutineStart.LAZY) {
            println("Inside lazy job state")
            delay(Duration.ofMillis(1000L))
        }

        println(lazyJob)
        lazyJob.start()
        println(lazyJob)
        lazyJob.join()
        println(lazyJob)

        println("\n\n")*/


/*//        val job = launch(Job()) {
        val job = launch() {
            delay(Duration.ofMillis(1000L))
        }
        val parentJob: Job = coroutineContext.job // coroutineContext[Job]

        println(job == parentJob)

        val parentChildrenSequence = parentJob.children
        if (parentChildrenSequence.count() > 0)
            println(job == parentChildrenSequence.first())

        println("job :: $job")
        println("job.job :: ${job.job}")
        println("parentJob :: $parentJob")
        println("parentJob.job :: ${parentJob.job}")
        println("parentChildrenSequence.first :: ${parentChildrenSequence.first()}")*/


        //Join() method wait until a job reaches its final state i.e either completed or cancelled
        val job1 = launch {
            delay(1000)
            println("Test1")
        }
        val job2 = launch {
            delay(2000)
            println("Test2")
        }
        job1.join()
        job2.join()
        println("All tests are done")

    }
}
