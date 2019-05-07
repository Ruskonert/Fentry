package work.ruskonert.fentry.sample

import work.ruskonert.fentry.Fentry

class School : Fentry<School>()
{
    private var className : String = "Default"
    val classes : HashMap<Int, List<Student>> = HashMap()

    fun setClassname(c : String) {
        this.className = c
    }
}