package vn.project.university.dbms.book_reservation.controller

import com.querydsl.core.types.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.querydsl.binding.QuerydslPredicate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import vn.project.university.dbms.book_reservation.exception.ReservationException
import vn.project.university.dbms.book_reservation.model.Account
import vn.project.university.dbms.book_reservation.model.Checkout
import vn.project.university.dbms.book_reservation.model.Reservation
import vn.project.university.dbms.book_reservation.model.ResponseTemplate
import vn.project.university.dbms.book_reservation.repository.BookCopyRepository
import vn.project.university.dbms.book_reservation.repository.CheckoutRepository
import vn.project.university.dbms.book_reservation.repository.ReservationRepository
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationRepository: ReservationRepository,
    private val checkoutRepository: CheckoutRepository,
    private val bookCopyRepository: BookCopyRepository
) {
    @GetMapping
    fun getReservation(
        @PageableDefault(
            page = 0,
            sort = ["startTime"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable,
        @QuerydslPredicate(root = Reservation::class) sampleReservation: Predicate
    ): ResponseTemplate<List<ReservationDTO>> {
        val page: Page<Reservation> = reservationRepository.findAll(sampleReservation, pageable)
        return ResponseTemplate(
            success = true,
            payload = page.content.map { row -> row.mapDTO() },
            status_code = HttpStatus.OK,
            totalPage = page.totalPages,
            page_size = page.numberOfElements,
            page = page.number
        )
    }
    @PutMapping("/{id}")
    fun checkout(@PathVariable id: UUID) : ResponseTemplate<Checkout>{
        var reservation = reservationRepository.findByIdOrNull(id)?: throw ReservationException("Unknown Reservation id = $id")
        var savedCheckout = checkoutRepository.save(Checkout(book = reservation.book, borrower = reservation.borrower))
        reservation.isDeleted = true
        reservationRepository.save(reservation)
        return ResponseTemplate<Checkout>(
            success = true,
            status_code = HttpStatus.CREATED,
            message = "a checkout created",
            payload = savedCheckout
        )
    }

    data class ReservationDTO(
        val book: Long,
        val borrower: AccountController.AccountRes,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val isDeleted: Boolean
    )

    private fun Reservation.mapDTO(): ReservationDTO {
        return ReservationDTO(
            book = book.id,
            borrower = borrower.mapDTO(),
            startTime = startTime,
            endTime = endTime,
            isDeleted = isDeleted
        )
    }

    fun Account.mapDTO(): AccountController.AccountRes {
        return AccountController.AccountRes(
            id = id!!,
            username = _username,
            status = status,
            email = email,
        )
    }
}