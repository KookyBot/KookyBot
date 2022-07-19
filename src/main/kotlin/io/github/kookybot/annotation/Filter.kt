package io.github.kookybot.annotation

@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Filter(
    val pattern: String,
)
