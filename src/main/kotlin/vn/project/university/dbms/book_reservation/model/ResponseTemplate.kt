package vn.project.university.dbms.book_reservation.model

import vn.project.university.dbms.book_reservation.constant.ResponseState


data class ResponseTemplate<T>(
    val status: ResponseState,
    val message: String? = null,
    val data: T? = null
) {
    init {
        if (status == ResponseState.FAIL && message == null)
            throw Error("Message required for failure response")
    }
}