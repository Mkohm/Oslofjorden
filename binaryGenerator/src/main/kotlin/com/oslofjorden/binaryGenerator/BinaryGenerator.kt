import com.oslofjorden.binaryGenerator.BinaryPolylineGenerator

fun main(args: Array<String>) {
    val generator = BinaryPolylineGenerator()
    val data = generator.parseKMLAndOutputLists()
    generator.writeBinaryFile(data)
}