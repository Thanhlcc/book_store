package vn.project.university.dbms.book_reservation.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import vn.project.university.dbms.book_reservation.constant.ResponseState
import vn.project.university.dbms.book_reservation.model.ResponseTemplate


@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceConflict::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun resourceCreationFail(exception: ResourceConflict) : ResponseTemplate<Any> {
        return ResponseTemplate(
            status= ResponseState.FAIL,
            message=exception.message?:"Resource creation failed"
        )
    }
    @ExceptionHandler(BookException::class, AccountException::class, ReservationException::class, CheckoutException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun genericResourceExceptionHandler(exception: RuntimeException) : ResponseTemplate<Any> {
        return ResponseTemplate(
            status = ResponseState.FAIL,
            message = exception.message?:"Generic source failure"
        )
    }
}