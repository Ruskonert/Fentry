package work.ruskonert.fentry

import com.google.gson.GsonBuilder
import work.ruskonert.fentry.adapter.MapTypeAdapter
import work.ruskonert.fentry.sample.School
import work.ruskonert.fentry.sample.Student

internal class Default : CollectionHandler
internal class StudentCollection : FentryCollector<Student>()

class MapTypeTest {
    fun test() {
        val defaultHandler = Default()
        val studentCollection = StudentCollection().registerTask(defaultHandler)
        val gsonBuilder = Fentry.registerDefaultAdapter(GsonBuilder(), School::class.java)

        gsonBuilder.registerTypeAdapter(Map::class.java, MapTypeAdapter.INSTANCE)
        val gson = gsonBuilder.setPrettyPrinting().serializeNulls().create()
        val school = School()
        for(i in 0..9) {
            val stdList = ArrayList<Student>()
            for(j in 0..10) {
                val si = Student().register()
                si.grade = j
                si.name = "student $i-$j"
                stdList.add(si)
            }
            school.classes[i] = stdList
        }
        val s = school.getSerializeString()
        println(s)
    }
}