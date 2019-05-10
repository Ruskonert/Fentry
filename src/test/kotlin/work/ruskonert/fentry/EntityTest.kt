package work.ruskonert.fentry

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class People : Fentry<People>()
{
    var name : String? = "Test"
}

class PeopleCollection : FentryCollector<People>()

class EntityTest
{
    var peopleList : ArrayList<People> = ArrayList()
    @Before
    fun construct() {
        for(i in 0..9) {
            val p = People().registerNonUnique()
            p.name = "Hello $i"
            peopleList.add(p)
        }
    }

    @Test
    fun entityTest() {
        val people = People()
        val handler = object : CollectionHandler {}
        PeopleCollection().registerTask(handler)

        val elements = people.getSerializeElements()
        val fromElements = FentryCollector.deserialize<People>(elements)
        assertNotNull(fromElements)
        assertEquals(people, fromElements)

        people.register()
        fromElements.register()

        assertNotEquals(people, fromElements)

        people.name = "Name was changed"
        fromElements.applyFromBaseElement(people)
        assertEquals(people.name, fromElements.name)
    }

    @Test
    fun singletonTest() {
        val gson = Fentry.getBuilderWithAdapter( People::class.java).create()
        val str = gson.toJson(peopleList)
        assertNotNull(str)

    }
}