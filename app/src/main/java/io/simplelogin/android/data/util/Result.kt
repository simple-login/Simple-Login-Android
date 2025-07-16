package io.simplelogin.android.data.util

sealed class Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>()
    data class Failure<E>(val error: E) : Result<Nothing, E>()

    inline fun <U> mapValue(transform: (T) -> U): Result<U, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun <F> mapError(transform: (E) -> F): Result<T, F> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }

    suspend fun <R> fold(onSuccess: suspend (T) -> R, onFailure: suspend (E) -> R): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}