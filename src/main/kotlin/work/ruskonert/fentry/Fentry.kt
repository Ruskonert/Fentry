package work.ruskonert.fentry

import com.google.gson.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy.getInvocationHandler
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object Util0 {
    /**
     * Changes the annotation value for the given key of the given annotation to newValue and returns
     * the previous value.
     */
    @Suppress("UNCHECKED_CAST")
    fun changeAnnotationValue(annotation: Annotation, key: String, newValue: Any): Any {
        val handler = getInvocationHandler(annotation)
        val f: Field
        try {
            f = handler.javaClass.getDeclaredField("memberValues")
            f.isAccessible = true
            val memberValues: MutableMap<String, Any> = f.get(handler) as MutableMap<String, Any>
            val oldValue = memberValues[key]
            if (oldValue == null || oldValue.javaClass != newValue.javaClass) {
                throw IllegalArgumentException()
            }
            memberValues[key] = newValue
            return oldValue
        }
        catch (e: IllegalArgumentException) {
            throw IllegalStateException(e)
        } catch (e: IllegalAccessException) {
            throw IllegalStateException(e)
        }
    }
}

/**
 *
 */
open class Fentry<Entity : Fentry<Entity>> : SerializeAdapter<Entity>()
{
    override fun serialize(p0: Entity, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     *
     */
    @Suppress("UNCHECKED_CAST")
    fun register(containable : Boolean = true) : Entity {
        val obj = this
        GlobalScope.launch {
            FentryCollector.setReference(obj, containable)
        }
        return this as Entity
    }

    /**
     *
     */
    @Suppress("UNCHECKED_CAST")
    fun registerNonUnique() : Entity {
        val obj = this
        runBlocking {
            FentryCollector.setReference(obj)
            val field = obj::class.java.getDeclaredField("uid")
            Util0.changeAnnotationValue(field.getAnnotation(InternalType::class.java), "isExpected", false)
        }
        return this as Entity
    }

    /**
     *
     */
    @InternalType(IsExpected = true)
    private var uid : String = "Please call the method 'register' if you want to identity it."
    fun getUniqueId() : String = this.uid

    /**
     *
     */
    @InternalType
    private var collection : FentryCollector<Entity>? = null
    fun getEntityCollector() : FentryCollector<Entity>? = this.collection
    fun convertNonDependent() { this.collection = null }

    /**
     *
     */
    fun getEntity(referenceObject: Any?) : Entity? {
        val ref = this.collection ?: return null
        return ref.getEntity(referenceObject)
    }

    /**
     *
     */
    @InternalType
    private val serializeAdapters : MutableList<SerializeAdapter<*>> = ArrayList()

    /**
     *
     */
    fun getSerializeString(isPretty : Boolean = true) : String {
        val element = this.getSerializeElements()
        val gsonBuilder = GsonBuilder().serializeNulls()
        if(isPretty) gsonBuilder.setPrettyPrinting()
        return gsonBuilder.create().toJson(element)
    }

    /**
     *
     */
    fun getSerializeElements() : JsonElement {
        val jsonObject = JsonObject()
        for(field in this.getSerializableEntityFields())
            this.addFieldProperty(jsonObject, field, this)
        return jsonObject
    }

    /**
     *
     */
    fun getSerializableEntityFields(target : Class<*> = this::class.java, specific : List<String>? = null) : Iterable<Field> {
        val fieldList = this.getFindableFieldList(target, this.ignoreTransient)
        return if(specific == null) fieldList
        else fieldList.filter { field: Field -> specific.contains(field.name)  }
    }

    /**
     *
     */
    private fun getFindableFieldList(base : Class<*>, ignoreTransient: Boolean) : Iterable<Field> {
        val fList = ArrayList<Field>()
        var kClass : Class<*> = base
        while(true) {
            // Transient annotations are commonly used to exclude from serialization.
            // However, Fentry queries this because it serializes all of these fields.
            if(ignoreTransient) {
                for (f in kClass.declaredFields)
                    // The field that was annotated it is no need to serialize because they just use for internal.
                    // The named 'Companion' is default object class of Kotlin, which collected the static fields & methods.
                    // Therefore no need to serialize this field.
                    if (!(f.type.name.endsWith("\$Companion") || this.isInternalField(f))) fList.add(f)
            }
            else {
                try {
                    for(f in kClass.declaredFields) {
                        f.isAccessible = true
                        if(f.type.name.endsWith("\$Companion")) continue
                        if(! Modifier.isTransient(f.modifiers) && !this.isInternalField(f)) fList.add(f)
                    }
                }
                catch(e : NoSuchFieldException) {
                    e.printStackTrace()
                }
            }
            if(kClass == Fentry::class.java) break
            kClass = kClass.superclass
        }
        return fList
    }




    fun registerSerializeAdapter(vararg adapters : KClass<out SerializeAdapter<*>>)
    {
        for(kClazz in adapters) {
            val adapterConstructor = kClazz.primaryConstructor
            if(adapterConstructor != null) {
                if(adapterConstructor.parameters.isEmpty()) {
                    this.serializeAdapters.add(adapterConstructor.call())
                }
            }
        }
    }

    fun registerSerializeAdapter(vararg adapters : Class<out SerializeAdapter<*>>) {
        for(kClazz in adapters) {
            val adapterConstructor = kClazz.constructors[0]
            if(adapterConstructor != null) {
                if(adapterConstructor.parameters.isEmpty()) {
                    this.serializeAdapters.add(adapterConstructor.newInstance() as SerializeAdapter<*>)
                }
            }
        }
    }

    @InternalType
    private var ignoreTransient : Boolean = false
    fun disableTransient(status : Boolean) { this.ignoreTransient = status }

    private fun addFieldProperty(jsonObject : JsonObject, field : Field, target : Any) {
        field.isAccessible = true
        val fieldName : String = field.name
        val value: Any? = try {
            field.get(target)
        }
        catch(_ : IllegalArgumentException){ null }
        catch(_ : IllegalAccessException)  { null }
        if(value == null) {
            jsonObject.add(fieldName, null); return
        }
        setProperty(jsonObject, fieldName, value, this.serializeAdapters, this.ignoreTransient, referenceOf = this)
    }

    private fun isInternalField(f: Field): Boolean {
        if(f.isAnnotationPresent(InternalType::class.java)) {
            val anno = f.getAnnotation(InternalType::class.java)
            return !anno.IsExpected
        }
        return false
    }

    fun applyToBase(serialize : String) {
        val fields = JsonParser().parse(serialize)
        return this.applyToBase(fields)
    }

    fun applyToBase(fields : JsonElement) {
        val instance = FentryCollector.deserializeFromClass(fields, this::class.java) ?: throw RuntimeException("Cannot create new instance from" +
                " deserializeFromClass Class<${this::class.java.name}> function")
        return this.applyToBase(instance)
    }

    fun applyToBase(victim : Fentry<Entity>) {
        if(victim::class.java == this::class.java) {
            for (k in victim.getSerializableEntityFields()) this.applyField(k, victim)
        }
    }

    private fun applyField(field : Field, target : Any?) : Boolean
    {
        return try {
            field.isAccessible = true
            field.set(this, field.get(target))
            true }
            catch(e : Exception) { false }
    }

    private fun getDefaultAdapterInit() : Array<Class<out SerializeAdapter<*>>> {
        return try {
            getDefaultAdapter()
        }
        catch(e: NotImplementedError) {
            Companion.getDefaultAdapter()
        }
    }

    open fun getDefaultAdapter() : Array<Class<out SerializeAdapter<*>>> {
        throw NotImplementedError("Not Implemented")
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): Entity? {
        return FentryCollector.deserializeFromClass(p0!!, this.getReference())
    }

    fun getSerializeString(p0: Entity?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        if(p0 == null) return JsonNull.INSTANCE
        return p0.getSerializeElements()
    }

    companion object {
        private val DEFAULT_SERIALIZE_ADAPTER = DefaultSerializer()

        fun registerDefaultAdapter(gsonBuilder : GsonBuilder) : GsonBuilder {
            for(adapter in getDefaultAdapter()) {
                val jcs = adapter.constructors[0].newInstance() as SerializeAdapter<*>
                gsonBuilder.registerTypeAdapter(jcs.getReference(), jcs)
            }
            return gsonBuilder
        }

        fun registerDefaultAdapter(gsonBuilder : GsonBuilder, ref : Fentry<*>) : GsonBuilder {
            for(adapter in getDefaultAdapter()) {
                val jcs = adapter.constructors[0].newInstance() as SerializeAdapter<*>
                gsonBuilder.registerTypeAdapter(jcs.getReference(), jcs)
            }
            gsonBuilder.registerTypeAdapter(ref.getReference(), DEFAULT_SERIALIZE_ADAPTER)
            return gsonBuilder
        }

        fun getDefaultAdapter() : Array<Class<out SerializeAdapter<*>>> {
            return arrayOf()
        }

        private fun setProperty(jsonObject : JsonObject, key : String, value : Any?, adapterColl : Collection<SerializeAdapter<*>>? = null,
                                disableTransient : Boolean = false, referenceOf : Fentry<*>) {
            val gsonBuilder = registerDefaultAdapter(GsonBuilder(), referenceOf)
            var adapters = adapterColl
            if(adapters == null)
                adapters = ArrayList()

            for(adapter in adapters) {
                val adapterType = adapter.getReference()
                gsonBuilder.registerTypeAdapter(adapterType, adapter)
            }

            val gson = gsonBuilder.serializeNulls().create()
            when(value) {
                is Number  -> jsonObject.addProperty(key, value)
                is Char    -> jsonObject.addProperty(key, value)
                is String  -> jsonObject.addProperty(key, value)
                is Boolean -> jsonObject.addProperty(key, value)
                else -> {
                    when (value) {
                        is Fentry<*> -> {
                            value.disableTransient(disableTransient)
                            jsonObject.add(key, value.getSerializeElements())
                            return
                        }
                        is Collection<*> -> {
                            for((indexOf, v) in value.withIndex()) {
                                jsonObject.add(indexOf.toString(), JsonParser().parse(gson.toJson(v)))
                            }
                        }

                        else -> try {
                            val result = gson.toJson(value)
                            val parser = JsonParser()
                            val element = parser.parse(result)
                            jsonObject.add(key, element)
                        } catch(e : Exception) {
                            e.printStackTrace()
                            jsonObject.addProperty(key, "FAILED_SERIALIZED_OBJECT")
                        }
                    }
                }
            }
        }
    }
}