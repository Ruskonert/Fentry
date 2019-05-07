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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import work.ruskonert.fentry.Fentry
import work.ruskonert.fentry.FentryCollector
import java.lang.reflect.Type

class DefaultSerializer private constructor() : SerializeAdapter<Fentry<*>>(Fentry::class.java)
{
    companion object {
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
