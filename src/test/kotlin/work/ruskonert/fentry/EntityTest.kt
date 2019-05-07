package work.ruskonert.fentry

import org.junit.Before
import org.junit.Test

internal class People : Fentry<People>()
{
    var name : String? = null
}


class EntityTest
{
    @Before
    fun construct() {
        val peopleList = ArrayList<People>()
        for(i in 0..9) {
            val p = People().registerNonUnique()
            p.name = "People $i"
            peopleList.add(p)
        }

        val gson = Util0.configureFentryGson(People().getSerializeAdapters(), People::class.java, false)
        val string = gson.toJson(peopleList)
        println(string)
    }

    @Test
    fun entityTest() {

    }
}