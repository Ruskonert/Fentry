package work.ruskonert.fentry

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import java.lang.reflect.Type

class DefaultSerializer private constructor() : SerializeAdapter<Fentry<*>>(Fentry::class.java)
{
    companion object
    {
        val INSTANCE : DefaultSerializer = DefaultSerializer()
    }

    override fun serialize(p0: Fentry<*>, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        return p0.getSerializeElements()
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(p0: JsonElement, p1: Type?, p2: JsonDeserializationContext?): Fentry<*>? {
        return FentryCollector.deserializeFromClass(p0, this.getReference() as Class<Fentry<*>>)
    }
}
