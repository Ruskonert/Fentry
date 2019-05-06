package work.ruskonert.fentry

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
abstract class SerializeAdapter<T> : JsonDeserializer<T>, JsonSerializer<T>
{
    @InternalType
    private var reference : Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    @Suppress("UNCHECKED_CAST")
    fun getReference() : Class<T> {
        return this.reference as Class<T>
    }
}