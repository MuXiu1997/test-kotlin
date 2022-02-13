package org.example

import cn.hutool.core.util.ReflectUtil
import cn.hutool.extra.spring.SpringUtil
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import java.util.concurrent.ConcurrentHashMap

inline fun <reified T> autowired(name: String): Lazy<T> = lazy {
    SpringUtil.getBean(name, T::class.java)
}

inline fun <reified T> autowired(): Lazy<T> = lazy {
    SpringUtil.getBean(T::class.java)
}

class StringToEnumConverterFactory(private val annotationClass: Class<out Annotation>) :
    ConverterFactory<String, Enum<*>> {
    private val converterMap: MutableMap<Class<*>, Converter<String, out Enum<*>>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Enum<*>> getConverter(targetType: Class<T>): Converter<String, T> {
        return converterMap.computeIfAbsent(targetType) { StringToEnumConverter(targetType) } as Converter<String, T>
    }

    internal inner class StringToEnumConverter<T : Enum<*>>(private val enumType: Class<T>) : Converter<String, T> {
        private val enumMap: MutableMap<String, T> = ConcurrentHashMap()

        init {
            val toKeyFunction = getToKeyFunction(enumType)
            val enums = enumType.enumConstants
            enums.forEach { e ->
                val k = toKeyFunction.invoke(e).toString()
                require(!enumMap.containsKey(k)) { "Duplicate key [$k] for enum ${enumType.name}" }
                enumMap[k] = e
            }
        }

        override fun convert(source: String): T =
            enumMap[source] ?: throw IllegalArgumentException("No element matches [$source] in enum ${enumType.name}")

        private fun getToKeyFunction(enumType: Class<T>): (T.() -> Any) {
            ReflectUtil.getFields(enumType) {
                it.isAnnotationPresent(annotationClass)
            }.forEach { field ->
                return {
                    try {
                        ReflectUtil.getFieldValue(this, field)
                    } catch (ignored: IllegalAccessException) {
                        throw IllegalArgumentException("Cannot access field [${field.name}] in enum ${enumType.name}")
                    }
                }
            }
            ReflectUtil.getMethods(enumType) {
                it.parameterCount == 0
                        && it.returnType != Void.TYPE
                        && it.isAnnotationPresent(annotationClass)
            }.forEach { method ->
                return {
                    try {
                        ReflectUtil.invoke<String>(this, method)
                    } catch (ignored: Exception) {
                        throw IllegalArgumentException("Cannot invoke method [${method.name}] in enum ${enumType.name}")
                    }
                }
            }
            return { name }
        }
    }
}
