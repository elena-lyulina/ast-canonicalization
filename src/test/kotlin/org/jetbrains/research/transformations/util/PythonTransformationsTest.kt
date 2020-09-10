import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.util.ParserSetup
import org.jetbrains.research.transformations.util.Util
import org.jetbrains.research.transformations.util.toXMLWithoutRoot
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.util.logging.Logger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PythonTransformationsTest {
    protected val LOG = Logger.getLogger(javaClass.name)

    protected val XMLTreeFileName = "${Util.getTmpPath()}/test.xml"

    @BeforeAll
    internal fun beforeAll() {
        LOG.info("beforeAll called")
        ParserSetup.checkSetup()
    }

    protected fun getSourcePythonCode(ctx: TreeContext, XMLDstPath: String): String {
        val treeSerializer = ctx.toXMLWithoutRoot()
        treeSerializer.writeTo(XMLDstPath)
        return Util.runProcessBuilder(*ParserSetup.getCommandForInverseParser(XMLDstPath))
    }
}