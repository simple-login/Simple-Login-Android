package io.simplelogin.android.data.util

sealed class Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>()
    data class Failure<E>(val error: E) : Result<Nothing, E>()

    inline fun <F> mapError(transform: (E) -> F): Result<T, F> = when (this) {
        is Success -> Success(value)
        is Failure -> Failure(transform(error))
    }
}