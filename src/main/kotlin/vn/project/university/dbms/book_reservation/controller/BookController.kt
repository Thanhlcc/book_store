package vn.project.university.dbms.book_reservation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import vn.project.university.dbms.book_reservation.constant.BookStatus
import vn.project.university.dbms.book_reservation.exception.BookException
import vn.project.university.dbms.book_reservation.exception.ReservationException
import vn.project.university.dbms.book_reservation.model.*
import vn.project.university.dbms.book_reservation.repository.BookCopyRepository
import vn.project.university.dbms.book_reservation.repository.BookRepository
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookRepository: BookRepository,
    private val bookCopyRepository: BookCopyRepository
) {
    @GetMapping
    fun getBookCatalog(
        @PageableDefault(page = 0) pageable: Pageable
    ): ResponseTemplate<List<BookDTO>> {
        val page: Page<Book> = bookRepository.findAll(pageable)
        return ResponseTemplate<List<BookDTO>>(
            success = true,
            payload = page.content.map { book -> book.mapDTO() },
            status_code = HttpStatus.OK,
            totalPage = page.totalPages,
            page_size = page.numberOfElements,
            page = page.number
        )
    }

    // TODO: Change this to bookCopyRepository
    @GetMapping("/{id}")
    fun getBookDetail(@PathVariable id: Long): ResponseTemplate<BookDTO> {
        val book = bookRepository.findByIdOrNull(id) ?: throw BookException("Book with id=${id} cannot found")
        return ResponseTemplate<BookDTO>(
            success = true,
            payload = book.mapDTO(),
            status_code = HttpStatus.OK
        )
    }

    @GetMapping("/{id}/{version}")
    fun getBookCopyDetail(@PathVariable("id") id: Long, @PathVariable("version") version: Int)
            : ResponseTemplate<BookCopyDTO> {
        var book: BookCopy = bookCopyRepository.findByBookDataIdAndVersion(id, version)
            ?: throw BookException("Book (id=${id}) with version ${version} not found")
        return ResponseTemplate<BookCopyDTO>(
            success = true,
            status_code = HttpStatus.OK,
            payload = book.mapDTO()
        )
    }

    @PutMapping("/{id}/reserve")
    fun reserveBook(
        @PathVariable id: Long,
        @AuthenticationPrincipal borrower: Account
    ): ResponseTemplate<Reservation> {
        var reservation: Reservation?
        val updatedBook = bookCopyRepository.findByIdOrNull(id).apply {
            reservation = this?.let { Reservation(it, borrower) }
            if (this?.quantity!! >= this.reservations.size && reservation != null) {
                this.reservations.add(reservation!!)
            } else {
                throw ReservationException("There is no remaining copy")
            }
        }
        if (updatedBook != null) {
            bookCopyRepository.save(updatedBook)
            return ResponseTemplate(
                success = true,
                message = "Reservation placed",
                payload = reservation,
                status_code = HttpStatus.CREATED
            )
        } else {
            return ResponseTemplate(
                success = false,
                message = "Cannot reserved",
                status_code = HttpStatus.CONFLICT
            )
        }
    }

    @PutMapping("/{id}/checkout")
    @PreAuthorize("hasRole('ROLE_LIBRIRIAN')")
    fun checkoutBook(@PathVariable id: String): ResponseTemplate<Checkout> {
        return ResponseTemplate(success = true, status_code = HttpStatus.OK)
    }

    data class BookDTO(
        var id: Long,
        var title: String,
        var genre: String,
        var description: String,
        var authors: MutableList<Author>
    )

    data class BookCopyDTO(
        val id: Long,
        val yearPublish: LocalDate,
        val publisher: String,
        var quantity: Int,
        val version: Int? = 1,
        val bookData: BookDTO
    )

    private fun Book.mapDTO(): BookDTO {
        val jsonParser = ObjectMapper()
        val authorDTOs = jsonParser.readTree(this.authors)
            .map { Author(it["author"].asText()) }
            .toMutableList()
        return BookDTO(
            id = id,
            title = title,
            genre = category.name,
            description = description,
            authors = authorDTOs
        )
    }

    private fun BookCopy.mapDTO() = BookCopyDTO(
        id = this.id,
        yearPublish = yearPublish,
        publisher = publisher.name,
        quantity = quantity,
        version = version,
        bookData = bookData.mapDTO()
    )
}
