package work.ruskonert.fentry.sample

import work.ruskonert.fentry.Fentry

/**
 * This class generated with Kotlin.
 * It cans be implement with Java platform.
 * @see work.ruskonert.fentry.sample.Student
 */
@Suppress("unused")
class School : Fentry<School>()
{
    private var className : String = "Default"
    val classes : HashMap<Int, List<Student>> = HashMap()

    fun setClassname(c : String) {
        this.className = c
    }
}