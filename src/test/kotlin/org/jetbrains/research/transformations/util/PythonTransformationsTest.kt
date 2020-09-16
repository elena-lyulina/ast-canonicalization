import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.util.ParserSetup
import org.jetbrains.research.transformations.util.Util
import org.jetbrains.research.transformations.util.toXMLWithoutRoot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KFunction

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PythonTransformationsTest {
    protected val LOG = Logger.getLogger(javaClass.name)

    protected val xmlTreeFileName = "${Util.getTmpPath()}/test.xml"

    companion object {
        fun getInAndOutArgumentsArray(
            resourcesRootName: String,
            cls: KFunction<PythonTransformationsTest>
        ): Array<Arguments> {
            val resourcesRootPath = cls.javaClass.getResource(resourcesRootName).path
            val inAndOutFilesMap = Util.getInAndOutFilesMap(resourcesRootPath)
            return inAndOutFilesMap.entries.map { (inFile, outFile) -> Arguments.of(inFile, outFile) }.toTypedArray()
        }
    }

    @BeforeAll
    internal fun beforeAll() {
        LOG.info("beforeAll called")
        ParserSetup.checkSetup()
    }

    protected fun getPythonSourceCode(ctx: TreeContext, XMLDstPath: String): String {
        val treeSerializer = ctx.toXMLWithoutRoot()
        treeSerializer.writeTo(XMLDstPath)
        return Util.runProcessBuilder(ParserSetup.getCommandForInverseParser(XMLDstPath)).removeSuffix("\n")
    }

    protected fun assertCodeTransformation(
        inFile: File,
        outFile: File,
        transformation: (TreeContext, Boolean) -> Unit
    ) {
        LOG.info("The current input file is: ${inFile.path}")
        LOG.info("The output input file is: ${outFile.path}")
        val treeCtx = PythonTreeGenerator().generateFromFile(inFile)
        val expectedSrc = Util.getContentFromFile(outFile)
        LOG.info("The expected code is:\n$expectedSrc")
        transformation(treeCtx, true)
        val actualSrc = getPythonSourceCode(treeCtx, xmlTreeFileName)
        LOG.info("The actual code is:\n$actualSrc")
        Assertions.assertEquals(expectedSrc, actualSrc)
    }
}