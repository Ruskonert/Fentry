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
package work.ruskonert.fentry.adapter

import com.google.gson.*
import work.ruskonert.fentry.Fentry
import java.lang.reflect.Type
import java.util.*

class MapTypeAdapter private constructor(): SerializeAdapter<Map<Any, Any?>>(Map::class.java)
{
    companion object {
        val INSTANCE : MapTypeAdapter = MapTypeAdapter()
    }

    override fun serialize(value: Map<Any, Any?>?, fentryTypeOf: Type?, p2: JsonSerializationContext?): JsonElement {
        if(value == null) return JsonNull.INSTANCE
        val gsonBuilder = GsonBuilder()
        val entireJsonObject = JsonObject()
        val adapter = JsonObject()
        val adapterType = HashMap<String, String>()

        var gson : Gson = gsonBuilder.serializeNulls().create()
        for((k, v) in value) {
            val elementJsonObject = JsonObject()
            try {
                val keyOfJsonObject = JsonObject()
                when (k) {
                    is Number -> {
                        keyOfJsonObject.addProperty("\$mapIndex?", k)
                    }
                    is String -> {
                        keyOfJsonObject.addProperty("\$mapIndex?", k)
                    }
                    is Boolean -> {
                        keyOfJsonObject.addProperty("\$mapIndex?", k)
                    }
                    is Char -> {
                        keyOfJsonObject.addProperty("\$mapIndex?", k)
                    }
                    else -> {
                        if (k is Fentry<*>) {
                            @Suppress("UNCHECKED_CAST")
                            for (sa in Fentry.getDefaultAdapter(k::class.java as Class<out Fentry<*>>)) {
                                gsonBuilder.registerTypeAdapter(sa.getReference(), sa)
                            }
                        }
                        gson = gsonBuilder.create()
                        val classname = k::class.java.name
                        keyOfJsonObject.add("\$mapIndex?$classname", JsonParser().parse(gson.toJson(k)))
                    }
                }
                elementJsonObject.add("key", keyOfJsonObject)
                val elementValueOfJsonObject = JsonObject()
                when (v) {
                    !is Number, Char, String, Boolean -> elementValueOfJsonObject.addProperty("typeOf", v!!::class.java.name)
                }
                if (v is Iterable<*>) {
                    for (v2 in v) {
                        if (v2 is Fentry<*>) {
                            for(sa in v2.getSerializeAdapters()) {
                                val saJson = JsonObject()
                                val referenceString = sa.getReference().name
                                val adapterClazz = sa::class.java.name

                                saJson.addProperty("reference", referenceString)
                                saJson.addProperty("targetOf", adapterClazz)

                                if(! adapterType.containsKey(referenceString)) {
                                    gsonBuilder.registerTypeAdapter(sa.getReference(), sa)
                                    adapter.add(UUID.randomUUID().toString().replace("-", ""), saJson)
                                    adapterType[referenceString] = adapterClazz
                                    continue
                                }
                                else {
                                    if(adapterClazz != adapterType[referenceString]) {
                                        gsonBuilder.registerTypeAdapter(sa.getReference(), sa)
                                        adapter.add(UUID.randomUUID().toString().replace("-", ""), saJson)
                                        continue
                                    }
                                }
                            }
                        }
                    }
                    gson = gsonBuilder.create()
                }
                elementValueOfJsonObject.add("value", JsonParser().parse(gson.toJson(v)))
                elementJsonObject.add("element", elementValueOfJsonObject)
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
            entireJsonObject.add("adapters", adapter)
            entireJsonObject.add(UUID.randomUUID().toString(), elementJsonObject)
        }
        return entireJsonObject
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): Map<Any, Any?>
    {
        throw NotImplementedError("Not implemented")
    }
}
