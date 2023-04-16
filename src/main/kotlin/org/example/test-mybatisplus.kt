package org.example

import cn.hutool.core.util.ReflectUtil
import cn.hutool.extra.spring.SpringUtil
import com.baomidou.mybatisplus.core.conditions.SharedString
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.toolkit.PluginUtils
import com.baomidou.mybatisplus.extension.kotlin.AbstractKtWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.statement.StatementHandler
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.ParserContext
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Constructor
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty

inline fun <reified T : Any> queryWrapper(block: KtQueryWrapper<T>.() -> Unit): KtQueryWrapper<T> {
    return KtQueryWrapper(T::class.java).apply(block)
}

inline fun <reified T : Any> updateWrapper(block: KtUpdateWrapper<T>.() -> Unit): KtUpdateWrapper<T> {
    return KtUpdateWrapper(T::class.java).apply(block)
}


inline fun <reified T : Any> BaseMapper<T>.selectOneQW(block: KtQueryWrapper<T>.() -> Unit): T? {
    return this.selectOne(queryWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.selectCountQW(block: KtQueryWrapper<T>.() -> Unit): Int {
    return this.selectCount(queryWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.selectListQW(block: KtQueryWrapper<T>.() -> Unit): MutableList<T> {
    return this.selectList(queryWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.selectMapsQW(block: KtQueryWrapper<T>.() -> Unit): MutableList<Map<String, Any>> {
    return this.selectMaps(queryWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.selectObjsQW(block: KtQueryWrapper<T>.() -> Unit): MutableList<Any> {
    return this.selectObjs(queryWrapper(block))
}

inline fun <reified T : Any, P : IPage<T>> BaseMapper<T>.selectPageQW(page: P, block: KtQueryWrapper<T>.() -> Unit): P {
    return this.selectPage(page, queryWrapper(block))
}

inline fun <reified T : Any, P : IPage<Map<String, Any>>> BaseMapper<T>.selectMapsPageQW(page: P, block: KtQueryWrapper<T>.() -> Unit): P {
    return this.selectMapsPage(page, queryWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.updateUW(entity: T?, block: KtUpdateWrapper<T>.() -> Unit): Int {
    return this.update(entity, updateWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.updateUW(block: KtUpdateWrapper<T>.() -> Unit): Int {
    return this.update(null, updateWrapper(block))
}

inline fun <reified T : Any> BaseMapper<T>.deleteQW(block: KtQueryWrapper<T>.() -> Unit): Int {
    return this.delete(queryWrapper(block))
}

inline fun <reified T, Children : AbstractKtWrapper<T, Children>> AbstractKtWrapper<T, Children>.columnStr(
    column: KProperty<*>,
): String {
    return ReflectUtil.invoke(this, "columnToString", column)
}

inline fun <reified T, Children : AbstractKtWrapper<T, Children>> AbstractKtWrapper<T, Children>.inOrEmpty(
    column: KProperty<*>,
    coll: Collection<*>?,
): AbstractKtWrapper<T, Children> {
    return apply {
        if (coll.isNullOrEmpty()) {
            apply("1 = 0")
        } else {
            `in`(column, coll)
        }
    }
}

private val queryWrapperConstructorMap = ConcurrentHashMap<Class<*>, Constructor<QueryWrapper<*>>>()
private val updateWrapperConstructorMap = ConcurrentHashMap<Class<*>, Constructor<UpdateWrapper<*>>>()

private fun Any.getField(name: String): Any? {
    return ReflectUtil.getFieldValue(this, name)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KtQueryWrapper<T>.baseWrapper(): QueryWrapper<T> {
    val constructor = queryWrapperConstructorMap.computeIfAbsent(this.entityClass) {
        ReflectionUtils.accessibleConstructor(
            QueryWrapper::class.java,
            Any::class.java,
            Class::class.java,
            AtomicInteger::class.java,
            Map::class.java,
            MergeSegments::class.java,
            SharedString::class.java,
            SharedString::class.java,
            SharedString::class.java,
            SharedString::class.java
        )
    }
    return constructor.newInstance(
        this.getField("entity"),
        this.getField("entityClass"),
        this.getField("paramNameSeq"),
        this.getField("paramNameValuePairs"),
        this.getField("expression"),
        this.getField("paramAlias"),
        this.getField("lastSql"),
        this.getField("sqlComment"),
        this.getField("sqlFirst")
    ) as QueryWrapper<T>
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KtUpdateWrapper<T>.baseWrapper(): UpdateWrapper<T> {
    val constructor = updateWrapperConstructorMap.computeIfAbsent(this.entityClass) {
        ReflectionUtils.accessibleConstructor(
            UpdateWrapper::class.java,
            Any::class.java,
            List::class.java,
            AtomicInteger::class.java,
            Map::class.java,
            MergeSegments::class.java,
            SharedString::class.java,
            SharedString::class.java,
            SharedString::class.java,
            SharedString::class.java
        )
    }
    return constructor.newInstance(
        this.getField("entity"),
        this.getField("sqlSet"),
        this.getField("paramNameSeq"),
        this.getField("paramNameValuePairs"),
        this.getField("expression"),
        this.getField("paramAlias"),
        this.getField("lastSql"),
        this.getField("sqlComment"),
        this.getField("sqlFirst")
    ) as UpdateWrapper<T>
}


class SpelExpInterceptor(private val parserContext: ParserContext) : InnerInterceptor {
    constructor() : this(defaultParserContext)

    override fun beforeQuery(
        executor: Executor?,
        ms: MappedStatement?,
        parameter: Any?,
        rowBounds: RowBounds?,
        resultHandler: ResultHandler<*>?,
        boundSql: BoundSql?,
    ) {
        val mpBs = PluginUtils.mpBoundSql(boundSql)
        evaluateSpELInBoundSql(mpBs)
    }

    override fun beforePrepare(sh: StatementHandler?, connection: Connection?, transactionTimeout: Int?) {
        val mpSh = PluginUtils.mpStatementHandler(sh)
        val mpBs = mpSh.mPBoundSql()
        evaluateSpELInBoundSql(mpBs)
    }

    private fun evaluateSpELInBoundSql(mpBs: PluginUtils.MPBoundSql) {
        val sql = mpBs.sql()
        val context = StandardEvaluationContext()
        context.beanResolver = BeanFactoryResolver(SpringUtil.getBeanFactory())
        mpBs.sql(parser.parseExpression(sql, parserContext).getValue(context) as String)
    }

    companion object {
        private val defaultParserContext: ParserContext = object : ParserContext {
            override fun isTemplate(): Boolean = true
            override fun getExpressionPrefix(): String = "{{- "
            override fun getExpressionSuffix(): String = " -}}"
        }
    }
}
