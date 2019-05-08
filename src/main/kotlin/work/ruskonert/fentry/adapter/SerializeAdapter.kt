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

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

@Suppress("UNCHECKED_CAST")
abstract class SerializeAdapter<T>(private var reference : Class<*>) : JsonDeserializer<T>, JsonSerializer<T>
{
    fun setReference(c : Class<*>) {
        this.reference = c
    }

    fun getReference() : Class<*> {
        return this.reference
    }

    override fun hashCode(): Int {
        return reference.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SerializeAdapter<*>
        if (reference != other.reference) return false
        return true
    }
}