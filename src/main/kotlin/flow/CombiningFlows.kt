package flow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    flowMergeDemo()
//    flowZipDemo()
//    flowCombineDemo()
}

suspend fun flowCombineDemo() {
    coroutineScope {
        val flow1 = flowOf(1, 2, 3, 4, 5).onEach { delay(300) }
        val flow2 = flowOf(1.1, 2.2, 3.3).onEach { delay(1000) }

        println("COMBINE")

//        combine(flow1, flow2) { item1, item2 -> Pair(item1, item2)}.collect {
//            println("(${it.first}, ${it.second})")
//        }
        combine(flow1, flow2) { item1, item2 -> "($item1, $item2)" }.collect {
            println(it)
        }
    }
}

suspend fun flowZipDemo() {
    coroutineScope {
        val flow1 = flowOf(1, 2, 3, 4, 5).onEach { delay(300) }
        val flow2 = flowOf(1.1, 2.2, 3.3).onEach { delay(1000) }

        println("ZIP")
        flow1.zip(flow2) { item1, item2 -> "($item1, $item2)" }.collect {
            println(it)
        }
    }
}

suspend fun flowMergeDemo() {
    coroutineScope {
//        val flow1 = flowOf(1, 2, 3).onEach { delay(2000) }
        val flow1 = flowOf(1, 2, 3, 4, 5).onEach { delay(200) }
        val flow2 = flowOf(1.1, 2.2, 3.3)

        println("MERGE")
        merge(flow1, flow2).collect {
            println(it)
        }
    }
}