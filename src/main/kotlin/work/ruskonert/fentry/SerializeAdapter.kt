package work.ruskonert.fentry

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

@Suppress("UNCHECKED_CAST")
abstract class SerializeAdapter<T>(private val reference : Class<*>) : JsonDeserializer<T>, JsonSerializer<T>
{
    fun getReference() : Class<*> {
        return this.reference
    }
}