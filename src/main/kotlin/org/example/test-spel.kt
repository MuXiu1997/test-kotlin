package org.example

import cn.hutool.extra.spring.SpringUtil
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.ParserContext
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

val parser = SpelExpressionParser()
val beanResolver by lazy { BeanFactoryResolver(SpringUtil.getBeanFactory()) }

val templateParserContext by lazy { TemplateParserContext() }

inline fun <reified T> evalExpr(
    expressionString: String,
    parserContext: ParserContext? = null,
    contextBlock: StandardEvaluationContext.() -> Unit = {},
): T? {
    val context = StandardEvaluationContext()
    context.beanResolver = beanResolver
    contextBlock(context)
    return parser.parseExpression(expressionString, parserContext).getValue(context, T::class.java)
}

inline fun evalExpr(
    expressionString: String,
    parserContext: ParserContext? = null,
    contextBlock: StandardEvaluationContext.() -> Unit = {},
): Any? {
    return evalExpr<Any>(expressionString, parserContext, contextBlock)
}

