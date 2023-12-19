package vn.project.university.dbms.book_reservation.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vn.project.university.dbms.book_reservation.constant.ResponseState
import vn.project.university.dbms.book_reservation.exception.BookException
import vn.project.university.dbms.book_reservation.model.Book
import vn.project.university.dbms.book_reservation.model.Checkout
import vn.project.university.dbms.book_reservation.model.Reservation
import vn.project.university.dbms.book_reservation.model.ResponseTemplate
import vn.project.university.dbms.book_reservation.repository.BookRepository

@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookRepository: BookRepository
) {
    @GetMapping
    fun getBookCatalog(): List<Book> = bookRepository.findAll().toList()

    @GetMapping(path = ["/{id}"])
    fun getBookDetail(@PathVariable(name = "id") id: Long): ResponseTemplate<Book> {
        val book = bookRepository.findById(id).orElseThrow { BookException("Book with id=${id} cannot found") }
        return ResponseTemplate<Book>(status = ResponseState.SUCCESS, data = book)
    }
    @PutMapping("/{id}/reserve")
    fun reserveBook(@PathVariable id: String) : ResponseTemplate<Reservation>{
        return ResponseTemplate(status = ResponseState.SUCCESS)
    }

    @PutMapping("/{id}/checkout")
    @PreAuthorize("hasRole('ROLE_LIBRIRIAN')")
    fun checkoutBook(@PathVariable id: String) : ResponseTemplate<Checkout>{
        return ResponseTemplate(status = ResponseState.SUCCESS)
    }
}