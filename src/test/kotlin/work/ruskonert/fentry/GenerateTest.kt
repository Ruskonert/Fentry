package work.ruskonert.fentry

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class GenerationTestEntity : Fentry<GenerationTestEntity>()
{
    var friend : List<GenerationTestEntity>? = null
    lateinit var studentName : String
    lateinit var description : List<String>
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
        human.description = arrayOf("First description", "Second description").toList()

        val human2 = GenerationTestEntity()
        human2.studentName = "Jame"
        human2.grade = 2
        human2.score1 = 96.2
        human2.score2 = 91.0
        human2.score3 = 86.5
        human2.description = arrayOf("Another First description", "Another Second description").toList()
        human.friend = arrayOf(human).toList()


        val element = FentryCollector.deserialize<GenerationTestEntity>(human.getSerializeElements())
        val element2 = FentryCollector.deserializeFromClass(human.getSerializeElements(), GenerationTestEntity::class.java)
        assertNotNull(element)
        assertNotNull(element2)
        assertEquals(human.getSerializeString(), element.getSerializeString())
        assertEquals(human.getSerializeString(), element2.getSerializeString())
    }
}