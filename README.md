# kotlin-coroutines-practice
Learn kotlin coroutines by practice in IntelliJ Idea 


Coroutines
Terms
Continuation passing style
Coroutine suspension interception
Continuation
CancellableContinuation
suspendCoroutine
startCoroutine
Cancellation and Cancellation Propagation
Job
Job()
SupervisorJob()
coroutineScope
supervisorScope
CancellationException
CoroutineExceptionHandler
*****************************************

Both Room and Retrofit make suspending functions main-safe.

Both Room and Retrofit use a custom dispatcher and do not use Dispatchers.IO.

*************************************

Main as suspend function?
We can declare main function as suspend function
	suspend fun Main() {
	// some work
}

Declaring Main as suspend function does not help in Android or Backend applications.

Suspending Main function is also launched in coroutine builder by the compiler under the hood.


**Continuation ?**

public interface Continuation { 
	public val context: CoroutineContext 
	public fun resumeWith(result: Result) 
}

**What call stack is maintained and used in coroutines?**

-> Each continuation keeps the state where we suspended (as a label) the function’s local variables and parameters (as fields), and the reference to the continuation of the function that called this function.
One continuation references another, which references another, etc. As a result, our continuation is like a huge onion: it keeps everything that is generally kept on the call stack.

**How does Continuation passes the result / exception to your code after suspension is over?**

When a continuation is resumed, each continuation first calls its function; once this is done, that continuation resumes the continuation of the function that called the function. This continuation calls its function, and the process repeats until the top of the stack is reached.

-> The state needs to be stored in a continuation.  When we resume, we need to restore the state from the continuation and either use the result or throw an exception.

**What is the cost of using suspending functions instead of regular ones?**
Dividing a function into states is cheap as number comparison and execution jumping costs nearly nothing. Saving a state in a continuation is also cheap. We do not copy local variables: we make new variables point to the same points in memory. The only operation that costs something is creating a continuation class, but this is still not a big deal. If you are not worried about the performance of RxJava or callbacks, you should definitely not worry about the performance of suspending functions.

Note
-Suspending functions are like state machines, with a possible state at the beginning of the function and after each suspending function call. • 

Both the label identifying the state and the local data are kept in the continuation object.


In JVM, type arguments are erased during compilation; so, for instance, both Continuation or Continuation become just Continuation.

Callback and continuation passing style both are same thing. Where as continuation uses state to resume back to caller.


**Coroutine Builders?**
Coroutine builders work as bridge between normal functions and suspend functions
kotlinx.coroutines library provided three builders
runBlocking
launch
async

launch and async builder are extension function of CoroutineScope interface but runBlocking not an extension function of CoroutineScope. 
So runBlocking can only be used as a root coroutine(parent coroutine). That means runBlocking will be used in different cases than other coroutines.
Except runBlocking coroutine builders need to be started on CoroutineScope.

**coroutineScope function?**
coroutineScope is just a suspending function that creates a scope for its lambda expression. 
The function returns whatever is returned by the lambda expression.

e.g.  coroutineScope{      } 

public interface CoroutineScope { 
	public val coroutineContext: CoroutineContext 
}

Passing scope as an argument is not a good solution


What is CoroutineContext?
CoroutineContext is an interface that represents an element or a collection of elements. 

It is conceptually similar to a map or a set collection: it is an indexed set of Element instances like Job, CoroutineName, CouroutineDispatcher, etc. 

The unusual thing is that each Element is also a CoroutineContext.
So, every element in a collection is a collection in itself.

For Example
fun main() {
	val name: CoroutineName = CoroutineName("A name") 
	val element: CoroutineContext.Element = name 
	val context: CoroutineContext = element 
	
	val job: Job = Job() 
	val jobElement: CoroutineContext.Element = job 
	val jobContext: CoroutineContext = jobElement 
}


**What is Job?**
Every coroutine builder from the Kotlin Coroutines library creates its own job.

Job is the only coroutine context that is not inherited by a coroutine from a coroutine. Every coroutine creates its own Job, and the job from an argument or parent coroutine is used as a parent of this new job.

This parent-child relationship (Job reference storing) enables the implementation of cancellation and exception handling inside a coroutine’s scope.

fun main(): Unit = runBlocking {
	val name = CoroutineName("Some name") 
	val job = Job() 
	launch(name + job) { 
		val childName = coroutineContext[CoroutineName] 
		println(childName == name) // true 


		// Here child job is new job whose parent is job which is passed as argument 		// to the coroutine builder
		val childJob = coroutineContext[Job]  

		println(childJob == job) // false 
		println(childJob == job.children.first()) // true 
	} 
}

When a coroutine has its own (independent) job, it has nearly no relation to its parent,
this causes us to lose structured concurrency.


**States / lifecycle of Job?**

**Active State:** In active state a job is running and doing its work. When a job is created using coroutine mostly the job is created in Active State. When a coroutine is executing its body the job is in active state.

**New State:** Only when a coroutine / job is created lazily the job is created in New State. When a job is in new state we need to call job.start() to move the state from new to active state. For example

// launch started lazily is in New state 
val lazyJob = launch(start = CoroutineStart.LAZY) {
	 delay(1000) 
} 
println(lazyJob) // LazyStandaloneCoroutine{New}@ADD 
// we need to start it, to make it active l
azyJob.start() 
println(lazyJob) // LazyStandaloneCoroutine{Active}@ADD

**Completing State:** When a job has done its work its state is changed from active to completing state.

**Completed State:** When a job is in completing state it waits for children to be completed. Once all children complete their jobs the state move from Completing to Completed state.

**Cancelling state:** When a running job(Active or Completing) fails its state is changed to Cancelling state. In cancelling state we have last chance to do some cleanup work there. 

**Cancelled state:** Once cleanup work is completed in cancelling state the job state moves to Cancelled state.
￼

**Structured concurrency mechanisms will not work if a new Job context replaces the one from the parent.**

fun main(): Unit = runBlocking { 
	launch(Job()) {  // the new job replaces one from parent 
	delay(1000) 
	println("Will not be printed") 
	}
 }

**join() function?**
join() is a suspending function so when it is called on a job reference it suspends further execution until the coroutine represented by the job is completed and the job object reaches final state I.e. either Cancelled or Completed.

Any code written after job.join() will be executed only when the coroutine represented by the job is completed I.e. it will sit for the coroutine to be completed before resuming execution of next line after job.join() call.

Example
fun main(): Unit = runBlocking {
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

Output
// (1 sec)
Test1
All tests are done
// (1 sec)
Test2

**In how many ways a Job can be created?**
There are two ways to create Job
By using coroutine builders
By using Job() factory function.


**What is Job factory function I.e. Job()?**

Ans:- public fun Job(parent: Job? = null): CompletableJob

Job() is not constructor function because Job is an interface. Job() function returns CompletableJob sub interface.

When we use Job() factory function a job instance is created which is not associated with any coroutine context but the instance can be used as argument to coroutine builders. 

When we use job (instance by factory function) as argument to any coroutine it replaces the job from parent coroutine context. Now if you call join() on this job instance then the program will never end because the Job is still in active state because it is not associated with any coroutine context.

For example
suspend fun main(): Unit = coroutineScope {
    val name = CoroutineName("Some Name")
    val job = Job()
    launch(name + job) { // the new job replaces one from parent
        delay(1000)
        println("Text 1")
    }
    launch(job) { // the new job replaces one from parent
        delay(2000)
        println("Text 2")
    }

    job.join() // Here job will be suspended forever
    println("Will not be printed")
}

**Output**
// (1 sec) 
// Text 1
 // (1 sec) 
// Text 2 
// (runs forever)

**What is the solution to program not ending in case of Job factory function?**
The solution for the problem of program not ending in case of Job() factory function is to call join() on every child of the job instance created by Job() function even if job is replaced from parent coroutine context.

For Example
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



**Job interface**
public interface Job : CoroutineContext.Element {
	public companion object Key : CoroutineContext.Key<Job>
	public val parent: Job?
	public val isActive: Boolean
	public val isCompleted: Boolean
	public val children: Sequence<Job>
	public suspend fun join()
	public suspend fun join()
	—	—	—	—	—
	—	—	—	—	—

}

**CompletableJob interface**
public interface CompletableJob : Job {
    public abstract fun complete(): Boolean
    public abstract fun completeExceptionally(exception: Throwable): Boolean
}


**How can you use Job() function to handle cancellation?**
You can pass a parent job as argument to Job() function.
Once you pass a parent job as argument and if you cancel the parent job then the job created by Job() function is also cancelled.

For Example
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


When a job is created using coroutine builder then parent-child relationship of job is established hence structured concurrency is supported but

When a job is created using Job() factory function then parent-child relationship is not established and structured concurrency is not supported

Job is the only element of which is not inherited, each coroutine creates its own job.



**Cancellation**
According to Google IO 19 support for coroutines was added primarily because of cancellation support.

**Basic cancellation concept**
Job interface has a cancel() function which has filling effect 

(1) When a job is cancelled the then coroutine execution represented by the job ends at first suspending function call in the coroutine.

(2) When a parent job is cancelled its children are also cancelled.
	When a parent job is canceled and child job tries to execute then a 	
  	CancellationException is thrown.

(3) When child job is canceled then parent is not affected
	When child job is canceled then no CancellationException is thrown to the parent

(4) Once a job is cancelled its state changes to final state(i.e. from Cancelling to Cancelled) and the job can not be used further

When job.join() is called after job.cancel() then the suspends the execution till the coroutine / job is cancelled(final state).
So we should call join() after cancel() and Kotlin.coroutines library provides a function cancelAndJoin() which wait till the coroutine / job reaches its final state(cancelled)


**How does cancellation work?**

When a job is cancelled its state changes “Cancelling” state. 

Once a job is in “Cancelling” state it can not be started, if you try to suspend the job it throws CancellationException.

So a CancellationException is thrown at first suspension point(i.e. delay(1000)) in the coroutine.

We can catch the CancellationException and perform more operation before the coroutine actually ends by try-catch-finally block.

We can use finally block to free up resources.

Because cancellation happens at the suspension points, it will not happen if there is no suspension point.


job.invokeOnCompletion() is another option to clean up the resources in coroutines
job.ensureActive() this function throw CancellationException if the job is cancelled.

suspendCancellableCoroutine{ } this suspend function is used to suspend a coroutine.The compiler passes CancelableContinuation object in this function which has additional function invokeOnCancellation().

invokeOnCancellation()- we can use this function to define what should be done in case the coroutine is cancelled.

Retrofit uses suspendCancellable { } coroutine to free resources if coroutine is cancelled by the user.

**How does exception propagation work in coroutines?**
When a coroutine receives / throws an exception it cancels itself and all its children and then propagates the exception to parent. When parent receives the exception it cancels itself, all its children then propagates the exception to its parent. Then exception propagation happens tub it reaches the root coroutine.

**How to stop exception propagation in coroutines?**
There are two ways to stop exception propagation in coroutines by using
(1) SupervisorJob
(2) supervisorScope


**What is SupervisorJob?**
When SupervisorJob is used as parent and if any of its child is throws an exception then the SupervisorJob ignores the exception and other children are not affected by the exception of their sibling.

When SupervisorJob is parent and it throws an exception then all its children are also cancelled.

  
**What is the difference between SupervisorJob and supervisorScope?**
To use SupervisorJob you need to create its instance by factory function SupervisorJob() which is not associated with any context and if used a argument to a coroutine builder it replaces the job of the parent coroutine context.

Where as if you use supervisorScope it’s just a suspending function where parent child relationship of Job is maintained and hence it supports structured concurrency easily.

Practical

supervisorScope creates a CoroutineScope with SupervisorJob. It inherits CoroutineContext from parent but overrides contexts’s job with SupervisorJob.

Unlike coroutineScope function if any of its child coroutine throws an exception then supervisorScope as well as its other children are not affected and the exception is not propagated to the parent.

Job(), supervisorJob() and supervisorScope() all of three overrides the job from inherited parent coroutine context.

In both coroutineScope and supervisorScope If try-catch is declared inside the coroutine where exception is thrown then it is handled there and catch block is executed and the exception is not propagated to the parent coroutine.

If parent scope is coroutineScope and Try-Catch is declared outside exception throwing launch builder(coroutine) then catch block is not executed because before coming to catch block the launch builder propagates the exception to its parent
and all coroutines are cancelled due to structured concurrency is intact in coroutineScope.

If parent scope is supervisorScope and Try-Catch is declared outside exception throwing launch builder(coroutine) then catch block is executed and other sibling and parent coroutines are not cancelled


**NonCancellable job can not be cancelled.**
**Use case- rollback changes to database**


**How does CancellationException work?**
CancellationException is not propagated to parent coroutines even if they are thrown inside coroutineScope.

If a child throw a CancellationException only that coroutine and its children are cancelled but other sibling and parent coroutines are not affected.

For Example
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
Output
// 2 Secs
Text 2

CancellationException inherits IllegalStateException which extends RuntimeException.

CancellationException exception propagates up the call stack, but does not crash the application because it is handled separately by the framework.

CancellationException does not crash because CancellationException is handled by the framework but Exception crashes the application

**How does exception handler work?**
CoroutineExceptionHandler does not stop exception propagation but it defines what should be done in case there is an exception

**********************************




We can use launch inside launch to call apis in parallel then what is the specific use of async in coroutines?

**Fragment life cycle flow in details?**

**How can I observe fragment lifecycle in ViewModel?**

**If a lazy initialiser throws an exception and if you use the lazy initialised member variable again the what happens? Is lazy block executed again?**

**Why do we not use GlobalScope in general?**

Note:-
Every suspend function receives additional parameter of Continuation type. This object is used to control the process of resuming, it helps returning to the function caller

**What is difference between coroutine builders and coroutine scope function?**
**(1)**
Coroutine Builders
launch, async, produce

Coroutine scope functions
coroutineScope
supervisorScope
withContext
withTimeout

**(2)**
Coroutine builders are extension functions on CoroutineScope.
Coroutine scope function are suspending functions. That mean the execution of previous coroutine is suspended at the point suspending functions is called. These function are called in-place and complete either due to exceptions or all of their children complete. They inherit the CoroutineContext from the suspending functions’s continuation object.

**(3)**
In case of coroutine builders exception is propagated to the parent through the job object.
Coroutine functions throw exception just like regular function throw exceptions.


**Can we use withContext(SupervisorJob()) instead of supervisorScope?**
No, we can’t. When we use withContext(SupervisorJob()), then withContext is still using a regular Job, and the SupervisorJob() becomes its parent. As a result, when one child raises an exception, the other children will be cancelled as well. withContext will also throw an exception, so its SupervisorJob() is practically useless


**Dispatchers (represent thread pool in coroutines concept)**

Default dispatcher
It is designed to run CPU-intensive operations.
The number of threads in pool represented by default dispatcher is equal to the number of cores on the underlying machine but not less than two.

Dispatchers.Default is limited by the number of cores in your processor.

**Main dispatcher**
****Defaut Dispatcher-** used for upu-intensive operations.

**Dispatchers.IO**
Dispatchers.IO is designed to be used when we block threads with I/O operations, for instance, when we read/write files, use Android shared preferences, or call blocking functions.

**The limit of Dispatchers.IO is 64 (or the number of cores if there are more).**

**limitedParallelism(parallelism: Int)**
creates a new dispatcher with an independent pool of threads. What is more, this pool is not limited to 64 as we can decide to limit it to as many threads as we want.

If limittedParellelism is used onDispatchers.Default it adds additional threads to the thread pool I.e. it increasers the number of thread in default dispatcher.

If limittedParellelism is used on Dispatcher.IO then it creates a separate dispatcher however both IO dispatcher pool and newly created pool are shared by threads.

**Unconfined dispatcher**
When it is started, it runs on the thread on which it was started. If it is resumed, it runs on the thread that resumed it.


**What is channel?**
**Channel is mechanism for inter-coroutine communication.**

**Channel is an interface which extends two other interfaces SendChannel and ReceiveChannel.**
**SendChannel** is used to send elements and close the channel
**ReceiveChannel** is used to receive the elements 

interface SendChannel<out E> { 
	suspend fun send(element: E) 
	fun close(): Boolean 
	//... 
}

interface ReceiveChannel <out E> { 
	suspend fun receive(): E 
	fun cancel(cause: CancellationException? = null) 
	// ... 
}

interface Channel : SendChannel, ReceiveChannel

**What is produce function?**
produce returns ReceiveChannel instance
he reason to use produce is that it closes the channel whenever the coroutine ends in any way(cancelled, completed, stopped)

**Channel types**
Depending on the channel size we have four types of channel

**Unlimited-** channel with capacity Channel.UNLIMITED that has an unlimited capacity buffer, and send never suspends.

**Buffered-** channel with capacity Channel.BUFFERED(which is 64 by default).

**Rendezvous (default)-** channel with capacity 0 or Channel.RENDEZVOUS (which is equal to 0), meaning that an exchange can happen only if sender and receiver meet (so it is like a book exchange spot, instead of a bookshelf).

**Conflated-** channel with capacity Channel.CONFLATED which has a buffer of size 1, and each new element replaces the previous one.


**On buffer overflow**
We can control what happens when the buffer is full (onBufferOverflow parameter). 
There are the following options: 
SUSPEND (default)- when the buffer is full, suspend on the send method. • DROP_OLDEST- when the buffer is full, drop the oldest element. 
DROP_LATEST- when the buffer is full, drop the latest element.


**What is hot and cold data source?**
￼
**Hot data source**
Hot data source produce elements as soon as possible and store them. 
They create elements independently of their consumption. 
These are collections (List, Set) and Channel. • 

**Cold data source**
Cold data sources are lazy.
They generally do not store elements and create them on demand.
These elements are Sequence, Java Stream, Flow and RxJava streams (Observable, Single, etc)


**Difference between Channel and Flow?**
**Channel**
**=>** Channel is hot source and stores the elements.
**=>** Each item in channel can be consumed only once i.e. they can not be consumed by other receiver once all the items are consumed already.
**=>** So other receivers receive empty channel only.

**Flow**
**=>** Flow is cold source and it does not store element s rather it generates element on-demand.
**=>** Since items are generated on-demand so flow can be consumed as many times as you wish.


**Flow introduction**
**=>** Flow’s terminal operations (like collect) suspend a coroutine instead of blocking a thread.
**=>** The flow builder is not suspending and does not require any scope
**=>** They also support other coroutine functionalities, such as respecting the coroutine context and handling exceptions. Flow processing can be cancelled, and structured concurrency is supported out of the box.

interface Flow {
	suspend fun collect(collector: FlowCollector) 
}

**Flow is similar to Iterable or Sequence**

interface Iterable { 
	operator fun iterator(): Iterator 
}

interface Sequence { 
	operator fun iterator(): Iterator 
}


**How can we return more than one value in Kotlin?**
There are **three ways** to return more than one value from Kotlin code
**Collections List / Set**
**Sequence: yield() / yieldAll()**
**Flow**

**Collections**
**=>** When we need to return more than one value from a function we can use collection List or Set. 
**=>** But when we use List / Set we need to wait until all elements are calculated then the collection is returned at once.

**Sequence**
**=>** When we need to return elements one by one we can use sequence.
**=>** Unlike collection, it provided elements one-by-one on demand.
**=>** We can use sequence for complex and large amount of data(CPU intensive operation) as well as for blocking calls(reading file).
**=>** Sequence does not support suspension, hence any suspending function can not be called from inside sequence.
**=>** Sequence does not support suspension instead it supports blocking call(thread is blocked returned until it processes all items in sequence)

**Flow**
**=>** Flow is a type that can return multiples values sequentially unlike suspend functions which can return only single value.

**=>** Flow is very similar to Iterator which produces a sequence of values, but it uses suspend functions to produce and consume values asynchronously.

**=>** For example flow can make network request calls without blocking main thread.

**There are three entities involved in stream of data**
**Producer-** produces data to stream
**Intermediaries-** can modify each emitted value
**Consumer-** consumes data from stream

**channelFlow
callbackFlow**


*******************************
**Flow lifecycle functions**

**onEach-** If you want to do some operation on each item you can use onEach. Its lambda is suspending call. So inside its lambda the executing is sequential like suspending functions.

**onStart-** onStart is called immediately when a terminal operation is called on the flow and it does not wait for first element to be emitted(onStart is called immediately the flow is started)

**onCompletion-** It acts like listener for flow completion. It is called when the flow completes (all items are emitted / uncaught exception / coroutine cancellation)

**onEmpty:** 
**=>** A flow can complete without emitting any value. 
**=>** For example a flow can complete without emitting any value in case of some unexpected event.
**=>** We can use onEmpty function in such cases to perform certain operation when a flow does not emit any value.
**=>** It can be used to emit default value when there is no value to emit like error or exception.

**catch: **
**=>** If an exception occurs then operation on the flow will be stopped on the way. 
**=>** If you want to recover from such situations you can use catch function.
catch stops the exception by catching , it can emit new value and keep the flow alive.

**=>** We can also use catch to emit default value in case an exception is caught.
**=>** catch can only be used to receive exceptions that occur upstream. 
 That means if a exception is throw after catch function(downstream) then it can not receive that exception.

**=>** In android we can use catch to show view on screen by emitting appropriate value from catch function. (Like error views)
**=>** catch can not protect us from exception in collect because it can not be called after collect(catch can not be called after last operation) so we can use flow inside try-catch .

**=>** It is good practice to move operations from collect to onEach and place onEach before catch. This way we can make sure that if an exception occurs then catch can handle that exception.

**=>** We should keep all operations in onEach and put it before catch function and do not keep any operation in collect so that every exception is caught in catch function.

**collect:** collect is a suspending function which suspends the coroutine until the flow is completed. 

**flowOn:**
**=>** flowOn function can be used to modify the coroutine context that is taken from collect function.
**=>** Also, flowOn works only in upstream direction. 
That means flowOn context modification will only be applied to function that are above the flowOn and the flowOn function has no effect on the flow functions which are called below flowOn.

**launch:** 
fun Flow.launchIn(scope: CoroutineScope): Job = scope.launch { collect() }

**=>** The launchIn function takes a coroutine scope and it call launch on that scope which creates a new coroutine.
**=>** So launchIn is used to start a new coroutine for flow processing.

**Note:** 
**=>** In Android we can use onStart to show progress bar and onCompletion to hide it.
**=>** We can use onEmpty to emit default value when there is no value to emit in case of error or exception.

**How does coroutine context is passed to flow functions lambdas like onStart, onEach, onEmpty, catch, onCompletion?**

Since flow functions take argument of suspending functions that is why their lambda are suspending function. But suspending functions need coroutine context, so all flow functions take context from the coroutine scope where collect function is called and parent-child relationship is established for structured concurrency.

**Flow example for android- how flow is used in Android?**

fun updateNews() {
	newsFlow() 
	.onStart { showProgressBar() } 
	.onCompletion { hideProgressBar() }
	 .onEach { view.showNews(it) } 
	.catch { view.handleError(it) } 
	.launchIn(viewModelScope) 
}


*******************************
**Flow Processing**

**map-** collects elements from flow and emits transformed elements

**filter-** returns elements from the flow which qualifies the given criteria in filter

**take-** picks only given amount of elements from flow
Example
private suspend fun takeExample() {
    val charR: CharRange = ('A'..'Z')
    charR.asFlow()
        .take(5)
        .onEach { println("onEach :: $it") }
        .collect()
}

**drop-** drops given amount of elements from flow
Example
private suspend fun dropExample() {
    val intRange: IntRange = (1..100)
    intRange.asFlow()
        .drop(50)
        .onEach { println("onEach :: $it") }
        .collect()
}

*****************************************

**merge, zip and combine**

**merge-** 
**=>** When we use merge the elements from one flow do not wait for another flow.
**=>** If elements from one flow are delayed then merge do not stop element from other flow.

**zip-** 
**=>** zip waits to collect one-one element from both flow sources and based on the action provided in zip it transforms the pair and produces new flow with the pair.
**=>** zip collects elements from both the flows one by one and if one is faster then it waits for the slower. When it gets one element from each source then it produces new pair based on the action we define in lambda.
**=>** If one of the pair closes then zip is also closed.

**combine-**
**=>** Unlike zip combine emits until both the flow sources are closed.
**=>** When we use combine, every new element replaces its predecessor. 
**=>** If the first pair has been formed already, it will produce a new pair together with the previous element from the other flow.

**flatMapConcat, flatMapMerge and flatMapLatest**

**If you use flatMapMerge on a flow with many elements, only 16 will be processed at the same time**

**retry-** uses retryWhen under the hood
**retryWhen-**

**Distinct functions**
**distinctUntilChanged()
distinctUntilChangedBy()
distinctUntilChanged { }**


***************************************************
**SharedFlow-**
**MutableSharedFlow** is like a broadcast channel: everyone can send (emit) messages which will be received by every coroutine that is listening (collecting).


**StateFlow-**
**=>** StateFlow is an extension of the SharedFlow concept.
**=>** It works similarly to SharedFlow when the replay parameter is set to 1.
**=>** It always stores one value, which can be accessed using the value property.

**=>** On Android, StateFlow is used as a modern alternative to LiveData. First, it has full support for coroutines. Second, it has an initial value, so it does not need to be nullable. So, StateFlow is often used on ViewModels to represent its state. This state is observed, and a view is displayed and updated on this basis.

**Transformation functions?**

**Dispatchers.IO is limited to 64 threads.**
**We can use limitedParallelism on Dispatchers.IO to make a new dispatcher with an independent limit that is greater than 64 threads.**
