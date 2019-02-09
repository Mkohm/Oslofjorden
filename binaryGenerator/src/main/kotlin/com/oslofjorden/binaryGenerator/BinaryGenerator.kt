import com.oslofjorden.binaryGenerator.BinaryGenerator

fun main(args: Array<String>) {
    val generator = BinaryGenerator()
    val data = generator.parseKMLAndOutputLists()
    generator.writeBinaryFile(data)
}