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

import com.google.common.collect.ArrayListMultimap
import com.google.gson.*
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.*
import java.io.File
import java.io.FileReader
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.collections.ArrayList

/**
 * FentryCollector is the collection the entity object which was registered by Fentry$register at
 * generic type, inherited with Fentry Class.
 * It cans monitor the change of the class value or field, and built in database form.
 *
 * @since 2.0.0
 * @author ruskonert
 */
abstract class FentryCollector<Entity : Fentry<Entity>>
{
    @Suppress("UNCHECKED_CAST")
    @InternalType
    private val persistentBaseClass : Class<Entity> = (javaClass.genericSuperclass as? ParameterizedType)!!.actualTypeArguments[0] as Class<Entity>
    fun getPersistentBaseClass() : Class<Entity> = this.persistentBaseClass

    /**
     *
     */
    private val uid : String
    fun getUniqueId() : String = this.uid

    /**
     *
     */
    private var entityCollection : MutableList<Entity>
    constructor() : this(UUID.randomUUID().toString())
    protected constructor(uuid : String) : super() {
        this.uid = uuid
        this.entityCollection = ArrayList()
    }

    /**
     *
     */
    fun registerTask(handler : CollectionHandler? = null) : FentryCollector<Entity> {
        if(handler != null) {
            handlerCollections.put(handler, this)
            this.handler = handler
        }
        else {

        }
        return this
    }

    /**
     *
     */
    private lateinit var handler : CollectionHandler
    fun getHandler() : CollectionHandler = this.handler

    /**
     * Retrieves all entities that have the class type of the Collection.
     * The entities are those in which disk I/O synchronization is continuously performed by the create function.
     * @return The entities with continuous disk I/O synchronization
     * @see work.ruskonert.fentry.Fentry.register
     */
    fun getEntities() : MutableList<Entity> = this.entityCollection

    /**
     * A collection of field names for identifying entities.
     * It will be use 'uid' to identify the Entity only if it is empty.
     */
    private val identifier : MutableList<String> = arrayOf("uid").toMutableList()

    /**
     * Add a field name to identify the Entity you want to import.
     * The getEntity method can identify and retrieve an Entity through a stored signature.
     * @param signature The field names defined or inferred within the class
     * @see work.ruskonert.fentry.FentryCollector.getEntity
     */
    fun addIdentity(vararg signature : String) {
        for(sig in signature) {
            if(! this.identifier.contains(sig)) {
                this.identifier.add(sig)
            }
        }
    }

    fun getIdentifier() : List<String> = this.identifier

    open fun getEntity(objectData: Any?) : Entity?
    {
        if(objectData == null) return null
        return getEntity0(objectData, this.getPersistentBaseClass())
    }

    fun terminate() {
        val obj = this
        val collection = handlerCollections[obj.handler]
        collection.remove(obj)
        for(entity in obj.entityCollection) {
            entity.convertNonDependent()
        }
        obj.entityCollection.clear()
    }

    fun toDatabase(): Boolean {
        val dataFolder = this.handler.getPath()
        val name = this.handler.getHandlerName()
        val entityCollection = this.entityCollection; if(entityCollection.size > 0) { return false }
        val baseClazz = this.persistentBaseClass
        val workspace = File(dataFolder.toFile(), "data/$name@${this.getPersistentBaseClass().name}")
        if(! workspace.exists()) workspace.mkdirs()
        GlobalScope.launch {
            for (jsonFile in workspace.listFiles()){
                try {
                    val parsedValue = JsonParser().parse(JsonReader(FileReader(jsonFile)))
                    val entity = deserializeFromClass(parsedValue, baseClazz)
                    if (entity == null) {
                        continue
                    } else {
                        entityCollection.add(entity)
                    }
                } catch (e: JsonSyntaxException) {
                    continue
                }
                catch(e : CancellationException) {
                    e.printStackTrace()
                }

            }
        }
        return true
    }


    companion object
    {
        private val handlerCollections : ArrayListMultimap<CollectionHandler, FentryCollector<*>> = ArrayListMultimap.create()
        fun getEntityCollections() : ArrayListMultimap<CollectionHandler, FentryCollector<*>> = handlerCollections
        fun <U : Fentry<U>> getEntityCollection(ref : Class<U>) : FentryCollector<U>?
        {
            @Suppress("UNCHECKED_CAST")
            for(k in getEntityCollections().values()) {
                if(ref.isAssignableFrom(k.getPersistentBaseClass()))
                    return k as? FentryCollector<U>
            }
            return null
        }

        private fun <V, E> inlineNullCheck(value : V, entity : E, function : (V, E) -> Any?) : Boolean {
            val result : Any? = function(value, entity)
            return result != null
        }


        @Suppress("UNCHECKED_CAST")
        private fun <E : Fentry<E>> getEntity0(objectData: Any, refClazz : Class<E>): E?
        {
            try {
                val collection = getEntityCollection(refClazz) ?: return null
                val registerEntities = collection.getEntities()
                if(registerEntities.isEmpty()) return null

                val checkFunction0 = fun(value : String, target : Fentry<*>) : E? {
                    for (field in target.getSerializableEntityFields(specific = collection.getIdentifier())) {
                        field.isAccessible = true
                        if(field.type != String::class.java) continue
                        if((field.get(target) as String) == value) return target as E?
                    }
                    return null
                }

                for(entity in registerEntities) {
                    when (objectData) {
                        is String -> {
                            if (inlineNullCheck(objectData, entity, checkFunction0)) return entity
                        }
                        else -> {
                            throw NotImplementedError("Not implemented this case.")
                        }
                    }
                }
                return null
            }
            catch(e : TypeCastException) {
                return null
            }
        }

        @Suppress("UNCHECKED_CAST", "PROTECTED_CALL_FROM_PUBLIC_INLINE")
        inline fun <reified U : Fentry<out U>> deserialize(element : JsonElement,
                                                           constructorIndexOf: Int = 0, vararg constructorParam: Any? = arrayOfNulls(0)) : U? {
            val targetObject: U?
            val constructColl = U::class.constructors
            val toJsonObject: JsonObject
            when(element) {
                is JsonNull -> return null
                else -> toJsonObject = element as JsonObject
            }
            targetObject = constructColl.toList()[constructorIndexOf].call(*constructorParam)
            for(field in (targetObject as Fentry<*>).getSerializableEntityFields()) {
                val refValue = toJsonObject.get(field.name)
                if(refValue == null) {
                    println("The variable '${field.name}' is invalid value that compare with base class.")
                    continue
                }
                when {
                    Fentry::class.java.isAssignableFrom(field.type) -> {
                        field.set(targetObject, deserializeFromClass(refValue, U::class.java, constructorIndexOf, *constructorParam))
                    }
                    else -> {
                        val result = availableSerialize(refValue, field.type)
                        if(result != null) {
                            if(Modifier.isFinal(field.modifiers))
                                field.setInt(null, field.modifiers and Modifier.FINAL.inv())
                            val resultClazz = result::class.java
                            field.set(targetObject, resultClazz.cast(result))
                        }
                    }
                }
            }
            return targetObject
        }


        fun <U : Fentry<*>> deserializeFromClass(element : JsonElement, reference: Class<U>,
                                                 constructorIndexOf: Int = 0, vararg constructorParam: Any? = arrayOfNulls(0)) : U? {
            val targetObject: U?
            val constructColl = reference.constructors
            val toJsonObject: JsonObject
            when(element) {
                is JsonNull -> {
                    return null
                }
                else -> {
                    toJsonObject = element as JsonObject
                }
            }
            @Suppress("UNCHECKED_CAST")
            targetObject = constructColl[constructorIndexOf].newInstance(*constructorParam) as? U
            if(targetObject == null) return null
            for(field in targetObject.getSerializableEntityFields()) {
                val refValue = toJsonObject.get(field.name)
                if(refValue == null) {
                    println("The variable '${field.name}' is invalid value that compare with base class.")
                    continue
                }

                when {
                    Fentry::class.java.isAssignableFrom(field.type) -> field.set(targetObject, deserializeFromClass(refValue, targetObject::class.java,
                            constructorIndexOf, *constructorParam))
                    else -> {
                        val result = availableSerialize(refValue, field.type)
                        if(result != null) {
                            if(Modifier.isFinal(field.modifiers))
                                field.setInt(null, field.modifiers and Modifier.FINAL.inv())
                            val resultClazz = result::class.java
                            field.set(targetObject, resultClazz.cast(result))
                        }
                    }
                }
            }
            return targetObject
        }

        protected fun availableSerialize(jsonElement : JsonElement, ref : Class<*>) : Any?
        {
            return try {
                val gson = Fentry::class.java.newInstance().configureSerializeBuilder().create()
                gson.fromJson(jsonElement, ref)
            } catch(e : Exception) {
                null
            }
        }

        @Suppress("UNCHECKED_CAST")
        suspend fun <E : Fentry<E>> setReference(entity: Fentry<E>, containable : Boolean = false)
        {
            for(k in getEntityCollections().values()) {
                if(entity::class.java.isAssignableFrom(k.getPersistentBaseClass()))
                {
                    lateinit var eField : Field
                    runBlocking {
                        launch {
                            // Hook the reference collection.
                            eField = entity::class.java.superclass.getDeclaredField("collection")
                            eField.isAccessible = true
                            eField.set(entity, k)
                        }

                        coroutineScope {
                            launch {
                                eField = entity::class.java.superclass.getDeclaredField("uid")
                                eField.isAccessible = true
                                // Generate the unique signature if the entity have no id.
                                val uuid = eField.get(entity) as? String
                                if(uuid == null || !Util0.isUUID(uuid) || uuid == Fentry.UNREFERENCED_UNIQUE_ID)
                                    eField.set(entity, UUID.randomUUID().toString())
                                val targetRef = k.entityCollection as MutableList<E>
                                targetRef.add(entity as E)
                            }
                        }
                    }
                    return
                }
            }
            println("Not exist EntityUnitCollection<${entity::class.java.simpleName}>, It needs to specific entity collection from registerTask class.")
        }

        fun handlerFrom(handler: CollectionHandler): List<FentryCollector<*>>? {
            return handlerCollections[handler]
        }
    }

}