package work.ruskonert.fentry.sample


// The test case has been checked for errors in some versions of Java 8.
// I am trying to solve this problem. Some collections and entities are
// not registered properly.
@Deprecated("The Junit Runner does not run properly on some versions." +
            "As a result, null values were referenced in some lower versions of Java 8.")
class CollectionReferenceTest
/*
import org.junit.Before
import org.junit.Test
import work.ruskonert.fentry.CollectionHandler
import work.ruskonert.fentry.Fentry
import work.ruskonert.fentry.FentryCollector
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * If you want to use the collection, You need to configure handler at first.
 * Just implement the class for can be use for separating the each collection.
 */
internal class DefaultHandler : CollectionHandler

/**
 * It uses testing for entity serialization.
 * Some of field was uninitialized.
 */
@Suppress("unused")
internal class TestEntity : Fentry<TestEntity>()
{
    // It cans identifying this entity.
    lateinit var specificName : String

    lateinit var internalValue : String

    // It could be having null-value.
    lateinit var nullValue : String

    val number : Int = 5

    val doubleNumber : Double = 4.4
}


internal class TestCollection : FentryCollector<TestEntity>()

class CollectionTest {
    private val handler: DefaultHandler = DefaultHandler()
    private lateinit var testCollection: FentryCollector<TestEntity>
    private lateinit var entity: TestEntity

    fun construct() {
        // Construct a collection & Configure the basis value
        testCollection = TestCollection().registerTask(handler)

        // For specify the entity with field name, which is "specificName".
        testCollection.addIdentity("specificName")

        // Construct the entity and registerTask its object & refer to collection which was created
        entity = TestEntity().register()

        // Disable the ability of identity the field which specifies the annotation Transient.
        entity.disableTransient(true)
    }

    fun identityTest()
    {
        // Set name which is can be specify for get the object
        entity.specificName = "Foo"

        // Set the internal value
        entity.internalValue = "Internal Value"

        val uniqueId = entity.getUniqueId()

        var foundEntity = testCollection.getEntity("Foo")
        assertEquals("Internal Value", foundEntity!!.internalValue)

        foundEntity = testCollection.getEntity(uniqueId)
        assertEquals(foundEntity, entity)

        testCollection.terminate()
    }

    fun handlerTest()
    {
        entity.specificName = "Foo43"
        entity.internalValue = "Inter1"
        val collection = FentryCollector.handlerFrom(this.handler)
        assertNotNull(collection)
        collection[0].addIdentity("internalValue")

        val entity = collection[0].getEntity("Inter1")
        assertNotNull(entity)
    }

    fun collectionTest()
    {
        val collection = entity.getEntityCollector()
        assertNotNull(collection)

        val others = FentryCollector.getEntityCollection(TestEntity::class.java)
        assertEquals(collection, others)

        val collectionList = FentryCollector.getEntityCollections()[handler]
        assertEquals(collection, collectionList[0])

        collection.terminate()
        assertEquals(collectionList.size, 0)
    }
}
*/