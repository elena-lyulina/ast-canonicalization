import org.jetbrains.research.transformations.util.ParserSetup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class ParserSetupForTests {
    @BeforeAll
    internal fun beforeAll() {
        println("beforeAll called")
        ParserSetup.checkSetup()
    }
}