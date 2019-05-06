package work.ruskonert.fentry

/**
 * InternalType annotates whether the field will be included in the serialization value for that
 * object when the field is serialized. If IsExpected is True, it is included in the serialization
 * value, even if the annotation is specified. This can be changed when there is an exceptional
 * change in the runtime process.
 * @since 2.0.0
 * @author ruskonert
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class InternalType(val IsExpected : Boolean = false)