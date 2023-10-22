fun main() {
    val temp by lazy {
        println("Printing inside lazy intializer")
        Exception("Lazy Exception")
    }

    println("temp :: $temp")
    println("temp :: $temp")
}
