package work.ruskonert.fentry

import org.junit.Test
import kotlin.test.assertNotNull

internal class GenerationTestEntity : Fentry<GenerationTestEntity>()
{
    var friend : Array<GenerationTestEntity>? = null
    lateinit var studentName : String
    lateinit var description : List<Any>
    var grade : Int  = 1
    var score1 : Double = 0.0
    var score2 : Double = 0.0
    var score3 : Double = 0.0
}

class GenerateTest
{
    @Test
    fun test()
    {
        val human = GenerationTestEntity()
        human.studentName = "Bob"
        human.grade = 2
        human.score1 = 100.0
        human.score2 = 97.4
        human.score3 = 90.5
        human.description = arrayOf("First description", "Second description", 355).toList()

        val human2 = GenerationTestEntity()
        human2.studentName = "Jame"
        human2.grade = 2
        human2.score1 = 96.2
        human2.score2 = 91.0
        human2.score3 = 86.5
        human2.description = arrayOf("Another First description", "Another Second description").toList()
        human2.friend = arrayOf(human, human)

        // Create the element to test it is same about the generated element from the entity's serialize value.
        val element = FentryCollector.deserialize<GenerationTestEntity>(human.getSerializeElements())
        assertNotNull(element)
        assert(human == element)

        val element2 = FentryCollector.deserializeFromClass(human2.getSerializeElements(), GenerationTestEntity::class.java)
        assertNotNull(element2)
        assert(human2 == element2)
    }
}