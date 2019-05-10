package work.ruskonert.fentry

import org.junit.Test
import work.ruskonert.fentry.sample.School
import work.ruskonert.fentry.sample.Student

internal class Default : CollectionHandler
internal class StudentCollection : FentryCollector<Student>()

class MapTypeTest {

    @Test
    fun test() {
        val defaultHandler = Default()
        StudentCollection().registerTask(defaultHandler)
        val school = School().register()
        school.setClassname("Our class")
        for(i in 0..1) {
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