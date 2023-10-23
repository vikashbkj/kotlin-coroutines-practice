import kotlinx.coroutines.*
import kotlin.random.Random


fun main() {
  withContextWithSupervisorJobExample()
}

// Do not pass SupervisorJob to withContext coroutine scope function it makes no change in behavior of withContext because
// Even if you pass SupervisorJob it still uses regular job instance and the SupervisorJob becomes parent of regular job represented by withContext
// So when a child is throws an exception then other children are also cancelled.
fun withContextWithSupervisorJobExample() {
    runBlocking {
        println("Before")

        withContext(SupervisorJob()) {
            launch {
                delay(1000)
                throw Error()
            }
            launch {
                delay(2000)
                println("Done !")
            }
        }
        println("After")
    }
}
