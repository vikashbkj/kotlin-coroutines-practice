//package flow
//
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.runBlocking
//
//
//fun main() = runBlocking {
//    flatMapConcatExample()
////    mergeExample()
//
////    flowOperations()
//}
//
//suspend fun flowOperations() {
//    val flow = flowOf(1, 2, 3, 4).onEach { delay(1000) }
//    val flow2 = flowOf("a", "b").onEach { delay(1500) }
//
//    // combines the latest elements from both flows and emits the result everytime a new value is received from either flow
//    // it closes only when all flows are closed
//    // can emit duplicate pair
////    flow.combine(flow2) { i, s -> i.toString() + s }.collect {
////        println(it) // O/P 1a 2b 3b 4b
////    }
//
////    emits each element received in the same sequence it receives,
////    and it does not preserve the order of value to be emitted
//    merge(flow, flow2).collect{
//        println(it)
//    }
//
//    //waits for one element from each flow and when one-one value from both flows are received it emits the final value
//    // when one of the flow closes it closes
//    // does not emit duplicate pair
////    flow2.zip(flow) { i, s -> i.toString() + s }.collect {
////        println(it) // O/P
////    }
//}
//
////The flatMapConcat function processes the produced flows one after
////another. So, the second flow can start when the first one is done.
//suspend fun flatMapConcatExample() {
//    coroutineScope {
////        val flow1 = flow<Int> {
////            repeat(3) {
////                delay(1000)
////                emit(it)
////            }
////        }
////        val flow2 = flowOf("A", "B", "C")
////
////        val flow = flow2.flatMapConcat {flow1}
////        flow.collect {
////            println("collect :: $it")
////        }
//
//        val flow2 = flow<String> {
//            delay(500)
//            emit("A")
//
//            delay(500)
//            emit("B")
//
//            delay(500)
//            emit("C")
//        }
//        val flowResult = flow2.flatMapLatest { flowFrom(it) }
//            .collect {
//                println("collect :: $it")
//            }
//    }
//}
//
//suspend fun mergeExample() {
//    val ints = flowOf(1, 2, 3,)
//        .onEach { delay(1000) }
//
//    val doubles = flowOf(0.1, 0.2, 0.3)
//        .onEach { delay(2000) }
//
////    val result = ints.zip(doubles) { num1, num2 ->
////         "$num1 _ $num2"
////    }
//    val result = ints.combine(doubles) { num1, num2 ->
//        "$num1 _ $num2"
//    }
//    result.collect {
//        println("collect :: $it")
//    }
//}
//
//private fun flowFrom(str: String): Flow<String> {
//    val flow1 = flow<String> {
//        repeat(3) {
//            delay(1000)
//            emit(" $it _ $str")
//        }
//    }
//    return flow1
//}