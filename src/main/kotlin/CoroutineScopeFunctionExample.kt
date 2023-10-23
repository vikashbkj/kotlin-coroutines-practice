import kotlinx.coroutines.*

fun main() {
//    coroutineScopeSuspendingExample()
//    coroutineScopeCancellationExample()
    coroutineScopeExceptionRethrowExample()
}

// coroutineScope suspends the previous coroutine and starts a new coroutine, previous one is suspended until new coroutine is complete
fun coroutineScopeSuspendingExample() {
    runBlocking {
      launch {
          val a = coroutineScope {
              println("Inside coroutine scope 1")
              delay(1000)
              10
          }
          println("a is calculated")

          val b = coroutineScope {
              println("Inside coroutine scope 1")
              delay(2000)
              20
          }
          println("b is calculated")

          println("a :: $a")
          println("b :: $b")
      }
    }
}

// coroutineScope cancellation
fun coroutineScopeCancellationExample() {
    runBlocking {
        val job = launch(CoroutineName("Parent")) {
            longTask()
        }

        delay(1500)
        job.cancel()
    }
}

private suspend fun longTask() {
    coroutineScope {
        launch {
            delay(1000)
            val name = coroutineContext[CoroutineName]?.name
            println("[$name] Finished task 1")
        }

        launch {
            delay(2000)
            val name = coroutineContext[CoroutineName]?.name
            println("[$name] Finished task 2")
        }
    }
}


// When an exception is encountered in coroutineScope function then all children are cancelled and it rethrows the exception
data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)

class ApiException(val code: Int, message: String) : Throwable(message)

fun getFollowersNumber(): Int = throw ApiException(500, "Service unavailable")

suspend fun getUserName(): String {
    delay(500)
    return "marcinmoskala"
}

suspend fun getTweets(): List<Tweet> {
    return listOf(Tweet("Hello, world"))
}

suspend fun getUserDetails(): Details {
    val details = coroutineScope {
        val userName = async { getUserName() }
        val followers = async { getFollowersNumber() }

        Details(userName.await(), followers.await())
    }
    return details
}

fun coroutineScopeExceptionRethrowExample() {
    runBlocking {
        val details = try {
            getUserDetails()
        } catch (e: Exception) {
            null
        }

        val tweets = try {
            async { getTweets() }
        } catch (e: Exception) {
            null
        }

        println("Details :: $details")
        println("Tweets :: $tweets")
    }
}