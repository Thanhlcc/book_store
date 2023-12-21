package vn.project.university.dbms.book_reservation.exception

import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import vn.project.university.dbms.book_reservation.model.ResponseTemplate

data class ValidationError(val field: String, val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceConflict::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun resourceCreationFail(exception: ResourceConflict): ResponseTemplate<Any> {
        return ResponseTemplate(
            success = false,
            message = exception.message ?: "Resource creation failed",
            status_code = HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(
        BookException::class,
        AccountException::class,
        ReservationException::class,
        CheckoutException::class,
        PropertyReferenceException::class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun genericResourceExceptionHandler(exception: RuntimeException): ResponseTemplate<Any> {
        return ResponseTemplate(
            success = false,
            message = exception.message ?: "Generic source failure",
            status_code = HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleWebExchangeBindException(e: MethodArgumentNotValidException): ResponseTemplate<Any> {
        val _error = e.bindingResult.fieldErrors.map {
            val defaultMessage = it.defaultMessage ?: "Validation failed"
            ValidationError(it.field, defaultMessage)}
            return ResponseTemplate<Any>(
                status_code = HttpStatus.BAD_REQUEST,
                success = false,
                message =  _error.toString()
        )
    }
}