package org.example
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import java.text.ParseException
import javax.validation.*
import kotlin.reflect.KClass

class DateValidator : ConstraintValidator<Date, String?> {
    private lateinit var patterns: Array<String>
    private var required: Boolean = false

    override fun initialize(constraintAnnotation: Date) {
        patterns = constraintAnnotation.patterns
        required = constraintAnnotation.required
    }

    override fun isValid(string: String?, context: ConstraintValidatorContext): Boolean {
        if (string.isNullOrEmpty()) return !required
        val dateStr = string.toString()
        return patterns.any {
            try {
                DateFormatUtils.format(DateUtils.parseDate(dateStr, it), it) == dateStr
            } catch (ignored: ParseException) {
                false
            }
        }
    }
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateValidator::class])
annotation class Date(
    val patterns: Array<String> = ["yyyy-MM-dd'T'HH:mm:ss"],
    val required: Boolean = true,
    val message: String = "invalid date",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

fun main() {
    class Test {
        @Date(patterns = ["yyyy-MM-dd"])
        val date: String = "2022-01-01"
    }
    println(Validation.buildDefaultValidatorFactory().validator.validate(Test()))
}
