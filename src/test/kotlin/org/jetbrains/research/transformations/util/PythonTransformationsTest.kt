import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.Transformation
import org.jetbrains.research.transformations.util.ParserSetup
import org.jetbrains.research.transformations.util.Util
import org.jetbrains.research.transformations.util.toXMLWithoutRoot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.util.logging.Logger
import java.util.stream.Stream
import kotlin.reflect.KFunction

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PythonTransformationsTest {
    protected val LOG = Logger.getLogger(javaClass.name)

    protected val XMLTreeFileName = "${Util.getTmpPath()}/test.xml"

    companion object {
        fun getResourcePath(resourceName: String, cls: KFunction<PythonTransformationsTest>): String {
            return cls.javaClass.getResource(resourceName).path
        }
    }

    @BeforeAll
    internal fun beforeAll() {
        LOG.info("beforeAll called")
        ParserSetup.checkSetup()
    }

    protected fun getSourcePythonCode(ctx: TreeContext, XMLDstPath: String): String {
        val treeSerializer = ctx.toXMLWithoutRoot()
        treeSerializer.writeTo(XMLDstPath)
        val (command, runningDirectory, variables) = ParserSetup.getCommandForInverseParser(XMLDstPath)
        println(runningDirectory)
        println(command.joinToString(" "))
        println(variables)
        return Util.runProcessBuilder(command, runningDirectory, variables)
    }

    protected fun transformCode(inFile: File, outFile: File, transformation: (TreeContext, Boolean) -> Unit){
        LOG.info("The current input file is: ${inFile.path}")
        LOG.info("The output input file is: ${outFile.path}")
        val treeCtx = PythonTreeGenerator().generateFromFile(inFile)
        val expectedSrc = Util.getContentFromFile(outFile)
        LOG.info("The expected code is:\n$expectedSrc")
        transformation(treeCtx, true)
        val resultSrc = getSourcePythonCode(treeCtx, XMLTreeFileName)
        LOG.info("The actual code is:\n$resultSrc")
        Assertions.assertEquals(expectedSrc, resultSrc)
    }
}