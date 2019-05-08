package work.ruskonert.fentry

import work.ruskonert.fentry.sample.School
import work.ruskonert.fentry.sample.Student

internal class Default : CollectionHandler
internal class StudentCollection : FentryCollector<Student>()

class MapTypeTest {
    fun test() {
        val defaultHandler = Default()
        val _collector = StudentCollection().registerTask(defaultHandler)
        val school = School()
        for(i in 0..9) {
            val stdList = ArrayList<Student>()
            for(j in 0..10) {
                val si = Student()
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