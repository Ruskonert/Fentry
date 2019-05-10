[![logo](https://img.icons8.com/ultraviolet/80/000000/acrobatics.png)](https://github.com/ruskonert/fentry)
# Fentry (Flexible-serialization ENTRY)
[![Build Status](https://travis-ci.org/Ruskonert/Fentry.svg?branch=master)](https://travis-ci.org/Ruskonert/Fentry)
[![license](https://img.shields.io/badge/License-MIT-orange.svg)](https://github.com/Ruskonert/Fentry/blob/master/LICENSE.md)
![version](https://img.shields.io/badge/Version-2.0.0-green.svg)
<br />
Fentry is flexible-serialization entry framework.<br />
<b>The de/serialization for efficient community to other platform & More flexible management your project, That's all what I talking about.</b>

# The purpose of development
Sometimes we need to serialize the entity for communication or preserve the object.
The reason of developing this project in the issues is too use your time for make your serialization adapter manually.
For example, You need to serialize this following:
```java
public class Friend
{
    public String name;
    public int grade;
    public double score;
}
```
And assume we use `GSON`:
```java
public class FriendList
{
    public List<Friend> friends = new ArrayList<>();
    public FriendList() {
        ...code
    }
    ...
}
```
After that, You try to construct the ```Gson```, Configure the property of builder:
```java
FriendList list = new FriendList();
Gson gson = new Gson();
String serialize = gson.toJson(list);
```
Maybe you can see this frustrating message like this:
```java
java.lang.UnsupportedOperationException: Attempted to serialize java.lang.Class: FriendList. Forgot to register a type adapter?
```
Because of not configuring & constructing the object adapter, So you will make the class for usuge properly.
That's why I think It needs to make this.
# The example for Usage
If you want to serialize automatically, It just inherits the class `Fentry<Child>`.
For Example:
```java
import work.ruskonert.fentry.Fentry;

public class MyEntity extends Fentry<MyEntity>
{
    private int number;
    private String name;
    private List<Integer> listOfScore;
    // ... 
}
```
And call the method `getSerializeString` or `getSerializeElements` for getting the elements:
```java
MyEntity entity = new MyEntity();

// The string that was serialized the object
String entitySerializeStr = entity.getSerializeString();

// another
JsonElement element = entity.getSerializeElements();
```
You can also register to the internal engine, which detects the changed value or class property, 
you need to generate the collection which is related to entity type, referred the class. For example, The entity type equals `MyEntity`, Then that is the following:
```Java
import work.ruskonert.fentry.FentryCollector;
public class MyCollection extends FentryCollector<MyEntity>
{
    public MyCollection() {
        super();
    }
    // You want to the code ..
}
```
Next you need to instance this class & connecting the handler. The handler is `CollectionHandler`,
It just implements for using it.
```java
public class MyHandler implements CollectionHandler
{
    // You want to the code ..
    
}
```
The object uses when it needs checking or getting to the collection which was handled by some handler.
Here is the example for getting the entity from collection:
```java
// Creating collection for service and register to memory.
MyHandler handler = new MyHandler();
MyCollection collection = new MyCollection().registerTask(handler);

...
// Create entity and register to it.
MyEntity entity = new MyEntity();
entity.register();

// or you can create with register to collection at the same time.
MyEntity entity = new MyEntity().regsiter();
...
// if the entity's unique id equals "eb83c6de-1909-4193-b05f-6fbce6b2c324"
MyEntity refered = Fentry.getEnttiy("eb83c6de-1909-4193-b05f-6fbce6b2c324", MyEntity.getClass())
... Then you want to go ..
```
# Licence
<b>Fentry: The Flexible-Serialization Entry<br />
Copyright (c) 2019 Ruskonert all rights reserved.</b>

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