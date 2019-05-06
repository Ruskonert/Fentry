package work.ruskonert.fentry

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class InternalType(val IsExpected : Boolean = false)