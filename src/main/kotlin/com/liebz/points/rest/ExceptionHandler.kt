package com.liebz.points.rest

import com.liebz.points.exception.NotEnoughPointsException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ValidationException

@RestControllerAdvice
class ExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidParams(e: MethodArgumentNotValidException): Map<String, List<String?>> {
        val errors = e.bindingResult.allErrors.map { error ->
            val fieldError = error as FieldError
            fieldError.defaultMessage
        }

        return mapOf("errors" to errors)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMissingRequestBody(e: HttpMessageNotReadableException): Map<String, List<String?>> {
        val errors = listOf("Unable to parse request body")

        return mapOf("errors" to errors)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotEnoughPointsException::class)
    fun handleNotEnoughPointsException(e: NotEnoughPointsException): Map<String, List<String?>> {
        return mapOf("errors" to listOf(e.message))
    }

}