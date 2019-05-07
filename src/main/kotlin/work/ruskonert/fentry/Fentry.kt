/*
          Fentry: The Flexible-Serialization Entry
       Copyright (c) 2019 Ruskonert all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package work.ruskonert.fentry

import com.google.gson.*
import kotlinx.coroutines.runBlocking
import work.ruskonert.fentry.adapter.DefaultSerializer
import work.ruskonert.fentry.adapter.MapTypeAdapter
import work.ruskonert.fentry.adapter.SerializeAdapter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy.getInvocationHandler
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/*
 * The object does not depend on the Fentry method, but it acts as a utility
 * to get the necessary values. You can use these objects to get values that
 * satisfy certain conditions, which can only be used in the Fentry class.
 */
object Util0 {
    /**
     * Determines the Field is not serializable value.
     * This is considered Internal Type when the annotation is referenced or it
     * is an unpredictable value (isExpected is False).
     *
     * @param f The field that you want to check that is internal value
     * @return returns true if the field is internal value, otherwise false
     */
    fun isInternalField(f: Field): Boolean {
        if(f.isAnnotationPresent(InternalType::class.java)) {
            val anno = f.getAnnotation(InternalType::class.java)
            return !anno.IsExpected
        }
        return false
    }

    /**
     * Changes the annotation value for the given key of the given annotation to newValue and returns
     * the previous value.
     *
     * @param annotation the target of annotation
     * @param key the field of the target that is annotation
     * @param newValue the value that will be applied to selected key
     * @return The old value fo annotation's key
     */
    @Suppress("UNCHECKED_CAST")
    fun changeAnnotationValue(annotation: Annotation, key: String, newValue: Any): Any {
        val handler = getInvocationHandler(annotation)
        val f: Field
        try {
            f = handler.javaClass.getDeclaredField("memberValues")
            f.isAccessible = true
            val memberValues: HashMap<Any, Any> = f.get(handler) as HashMap<Any, Any>
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

    /**
     * Configure the gson for fentry de/serialization.
     * The default adapter of fentry will be detect your made the classes.
     *
     * @param adapterColl The collection of adapter for de/serialization
     * @param fentryTypeOf The class for default adapter of fentry type
     * @param isPretty determines the json string is easy for understanding structures
     * @return Returns gson that was configured the adapters & properties
     * @see work.ruskonert.fentry.DefaultSerializer
     */
    fun configureFentryGson(adapterColl : Collection<SerializeAdapter<*>>?, fentryTypeOf : Class<out Fentry<*>>? = null, isPretty : Boolean = false) : Gson {
        var gsonBuilder = GsonBuilder()
        var adapters = adapterColl
        if(adapters != null) {
            for (adapter in adapters) {
                // If the DefaultSerializer is in the adapter, register the class type of
                // 'fentryTypeOf' so that it can be serialized. DefaultSerializer is available
                // for all classes for Fentry Type.
                val adapterType: Class<*> = if (adapter is DefaultSerializer) {
                    fentryTypeOf ?: adapter.getReference()
                } else adapter.getReference()
                gsonBuilder.registerTypeAdapter(adapterType, adapter)
            }
        }
        else {
            val cz = fentryTypeOf ?: Fentry::class.java
            gsonBuilder = Fentry.registerDefaultAdapter(gsonBuilder, cz)
        }
        if(isPretty) gsonBuilder.setPrettyPrinting()
        return gsonBuilder.serializeNulls().create()
    }

    /**
     * Sets the property on the JsonObject.
     * @param jsonObject will be configure the object and apply the value with serialize adapter
     * @param key
     * @param value
     * @param adapterColl
     * @param disableTransient
     * @param fentryTypeOf
     * @return Returns the configured JsonObject, which was applied it
     */
     fun setProperty(jsonObject : JsonObject, key : String, value : Any?, adapterColl : Collection<SerializeAdapter<*>>? = null,
                     disableTransient : Boolean = false, fentryTypeOf : Class<out Fentry<*>>? = null) : JsonObject {
        val gson = configureFentryGson(adapterColl, fentryTypeOf)
        when(value) {
            // Those types can use the default method.
            // There's no need specific process working.
            is Number  -> jsonObject.addProperty(key, value)
            is Char    -> jsonObject.addProperty(key, value)
            is String  -> jsonObject.addProperty(key, value)
            is Boolean -> jsonObject.addProperty(key, value)

            // It needs to other method.
            // Most of type could be Fentry type or Collection, and Iterable.
            // Otherwise, It can't serialize.
            else -> {
                when(value) {
                    is Fentry<*> -> {
                        // It depends the owner value.
                        value.disableTransient(disableTransient)
                        jsonObject.add(key, value.getSerializeElements())
                    }
                    is Map<*,*> -> {
                        jsonObject.add(key, MapTypeAdapter.INSTANCE.serialize(value as Map<Any, Any?>?, fentryTypeOf,null))
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
        return jsonObject
    }
}

/**
 * Fentry is an automatic serializable class that can detect the values that need serialization
 * for defined fields and class types, and build the adapter. This saves development time by
 * eliminating the developer writing an adapter and registering it. it is not an abstract class,
 * but it is not intended to be used by itself; it inherits from child classes and recognizes
 * the values of child fields.
 *
 * @param Entity The class type of child class only, which is inherited
 * @since 2.0.0
 * @author ruskonert
 */
open class Fentry<Entity : Fentry<Entity>>
{
    // The value of class of referenced type at the child.

    @InternalType
    private var reference : Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    @Suppress("UNCHECKED_CAST")
    fun getReference() : Class<Entity> {
        return this.reference as Class<Entity>
    }

    private val typeOf : String = this.reference.typeName

    /**
     * Updates to the entity collection, which sames the type of this entity.
     * This will be have the unique id, It cans distinguishing the object compare with others.
     * If the task of hooked collection is enabled, the collection can detect it that about
     * changed this entity.
     *
     * @param containable Determines the collection of this entity's base
     *        can be built to the database form
     * @return Return this entity that was completed setting the reference to the collection
     */
    @Suppress("UNCHECKED_CAST")
    fun register(containable : Boolean = true) : Entity {
        val obj = this
        runBlocking {
            FentryCollector.setReference(obj, containable)
        }
        return this as Entity
    }

    /**
     * Creates the entity without hooking to the collection. It is recommended when
     * it just uses for singleton-based. it has no unique id, It needs to add the
     * identifier field name that can be specified.
     *
     * @see work.ruskonert.fentry.FentryCollector.identifier
     * @see work.ruskonert.fentry.Fentry.register
     */
    @Suppress("UNCHECKED_CAST")
    fun registerNonUnique(isUnreferenced: Boolean = true) : Entity {
        val obj = this
        runBlocking {
            if(!isUnreferenced) FentryCollector.setReference(obj)
            var clz : Class<out Fentry<*>>? = obj::class.java.superclass as Class<out Fentry<*>>
            while(clz != null)  {
                for(f in clz.declaredFields) {
                    f.isAccessible = true
                    if(f.name == "uid" && f.declaringClass == Fentry::class.java) {
                        Util0.changeAnnotationValue(f.getDeclaredAnnotation(InternalType::class.java), "IsExpected", false)
                        break
                    }
                }
                clz = clz::class.java.superclass as? Class<out Fentry<*>>
                if(clz == Any::class.java) {
                    break
                }
            }
        }
        return this as Entity
    }

    /**
     * The unique id of this entity.
     * if the key of annotation is false, it excepts the elements of serialization.
     * @see work.ruskonert.fentry.InternalType
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
    private val serializeAdapters : MutableList<SerializeAdapter<*>> = this.getDefaultAdapterInit().toMutableList()
    fun getSerializeAdapters() : List<SerializeAdapter<*>> = this.serializeAdapters

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
        for(field in this.getSerializableEntityFields()) {
            this.addPropertyOfField(jsonObject, field, this)
        }
        return jsonObject
    }

    /**
     *
     */
    private fun addPropertyOfField(jsonObject : JsonObject, field : Field, target : Any) {
        field.isAccessible = true
        val fieldName : String = field.name
        val value: Any? = try {
            field.get(target)
        }
        catch(_ : IllegalArgumentException){ null }
        catch(_ : IllegalAccessException)  { null }
        if(value == null) {
            jsonObject.add(fieldName, JsonNull.INSTANCE)
            return
        }
        Util0.setProperty(jsonObject, fieldName, value, this.serializeAdapters, this.ignoreTransient, this::class.java)
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
            // However, Fentry queries that because it serializes all of these fields.
            if(ignoreTransient) {
                for (f in kClass.declaredFields)
                    // The field that was annotated it is no need to serialize because they just use for internal.
                    // The named 'Companion' is default object class of Kotlin, which collected the static fields & methods.
                    // Therefore no need to serialize this field.
                    if (!(f.type.name.endsWith("\$Companion") || Util0.isInternalField(f))) fList.add(f)
            }
            else {
                try {
                    for(f in kClass.declaredFields) {
                        f.isAccessible = true
                        if(f.type.name.endsWith("\$Companion")) continue
                        // It can't be ignored the transient annotation, Because 'ignoreTransient' is false.
                        if(!Modifier.isTransient(f.modifiers) && !Util0.isInternalField(f)) fList.add(f)
                    }
                }
                catch(e : NoSuchFieldException) {
                    e.printStackTrace()
                }
            }
            // If the routine reaches the end of the entity(itself type), That same the search is ended.
            if(kClass == Fentry::class.java) break

            // It means there's remains need to search, maybe the class is superclass type.
            kClass = kClass.superclass
        }
        return fList
    }

    fun registerSerializeAdapter(vararg adapter : SerializeAdapter<*>) {
        this.serializeAdapters.addAll(adapter)
    }

    fun registerSerializeAdapter(vararg adapters : KClass<out SerializeAdapter<*>>)
    {
        for(kClazz in adapters) {
            val adapterConstructor = kClazz.primaryConstructor
            if(adapterConstructor != null) {
                // The constructor of adapter must be have the empty parameter.
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
                // The constructor of adapter must be have the empty parameter.
                if(adapterConstructor.parameters.isEmpty()) {
                    this.serializeAdapters.add(adapterConstructor.newInstance() as SerializeAdapter<*>)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is Fentry<*>) {
            return (this.uid == other.uid) && (this.reference == other.reference) && (this.collection == other.collection)
        }
        return false
    }

    @InternalType
    private var ignoreTransient : Boolean = false
    fun disableTransient(status : Boolean) { this.ignoreTransient = status }

    fun applyFromBaseElement(serialize : String) {
        val fields = JsonParser().parse(serialize)
        return this.applyFromBaseElement(fields)
    }

    fun applyFromBaseElement(fields : JsonElement) {
        val instance = FentryCollector.deserializeFromClass(fields, this::class.java) ?: throw RuntimeException("Cannot create new instance from" +
                " deserializeFromClass Class<${this::class.java.name}> function")
        return this.applyFromBaseElement(instance)
    }

    fun applyFromBaseElement(victim : Fentry<Entity>) {
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

    private fun getDefaultAdapterInit() : Array<out SerializeAdapter<*>> {
        return try {
            getDefaultAdapter()
        }
        catch(e: NotImplementedError) {
            Companion.getDefaultAdapter()
        }
    }

    open fun getDefaultAdapter() : Array<out SerializeAdapter<*>> {
        throw NotImplementedError("Not Implemented yet.")
    }

    override fun hashCode(): Int {
        var result = reference.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + (collection?.hashCode() ?: 0)
        result = 31 * result + serializeAdapters.hashCode()
        result = 31 * result + ignoreTransient.hashCode()
        return result
    }

    companion object {
        fun registerDefaultAdapter(gsonBuilder : GsonBuilder) : GsonBuilder {
            for(adapter in getDefaultAdapter()) {
                gsonBuilder.registerTypeAdapter(adapter.getReference(), adapter)
            }
            return gsonBuilder.serializeNulls()
        }

        fun registerDefaultAdapter(gsonBuilder : GsonBuilder, defaultReference : Class<out Fentry<*>>) : GsonBuilder {
            for(adapter in getDefaultAdapter()) {
                if(adapter is DefaultSerializer)
                    gsonBuilder.registerTypeAdapter(defaultReference, adapter)
            }
            return gsonBuilder.serializeNulls()
        }

        fun getDefaultAdapter() : Array<out SerializeAdapter<*>> {
            return arrayOf(DefaultSerializer.INSTANCE, MapTypeAdapter.INSTANCE)
        }
    }
}