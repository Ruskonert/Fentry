package work.ruskonert.fentry

import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class People : Fentry<People>()
{
    var name : String? = "Test"
}


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

    }

    @Test
    fun singletonTest() {
        val gson = Fentry.registerDefaultAdapter(GsonBuilder(), People::class.java).create()
        val str = gson.toJson(peopleList)
        assertNotNull(str)
    }
}