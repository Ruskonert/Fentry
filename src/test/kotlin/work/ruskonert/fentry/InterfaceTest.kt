package work.ruskonert.fentry

import org.junit.Before
import org.junit.Test

internal class NumberEntity : TypeOfNumber
{
    override fun getDoubleNumber(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumber(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

interface TypeOfNumber
{
    fun getNumber() : Int

    fun getDoubleNumber() : Double
}


class InterfaceTest
{

    @Before
    fun construct()
    {

    }

    @Test
    fun test()
    {

    }
}