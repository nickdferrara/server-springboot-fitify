package com.nickdferrara.fitify.shared.crypto

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class ApplicationContextProvider : ApplicationContextAware {

    companion object {
        private var context: ApplicationContext? = null

        fun getApplicationContext(): ApplicationContext =
            context ?: throw IllegalStateException("ApplicationContext has not been initialized")

        fun <T> getBean(beanClass: Class<T>): T =
            getApplicationContext().getBean(beanClass)
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}
