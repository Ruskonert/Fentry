package work.ruskonert.fentry

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

abstract class SerializeAdapter<T> : JsonDeserializer<T>, JsonSerializer<T>
{
    @Suppress("UNCHECKED_CAST")
    @InternalType
    private val reference : Class<T> = (javaClass.genericSuperclass as ParameterizedTypeImpl).actualTypeArguments[0] as Class<T>
    fun getReference() : Class<T> = this.reference
}