package vn.project.university.dbms.book_reservation.model

import org.springframework.http.HttpStatusCode


data class ResponseTemplate<T>(
    val success: Boolean,
    val status_code: HttpStatusCode,
    val message: String? = null,
    val payload: T? = null,
    val page_size: Int? = null,
    val page: Int? = null,
    val totalPage: Int? = null
) {
    init {
        if (!success && message == null)
            throw Error("Message required for failure response")
    }
}
