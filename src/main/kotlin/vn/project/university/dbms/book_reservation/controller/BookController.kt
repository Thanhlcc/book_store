package vn.project.university.dbms.book_reservation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.querydsl.core.types.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.querydsl.binding.QuerydslPredicate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import vn.project.university.dbms.book_reservation.exception.BookException
import vn.project.university.dbms.book_reservation.exception.ReservationException
import vn.project.university.dbms.book_reservation.model.*
import vn.project.university.dbms.book_reservation.repository.BookCopyRepository
import vn.project.university.dbms.book_reservation.repository.BookRepository
import vn.project.university.dbms.book_reservation.repository.CheckoutRepository
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookRepository: BookRepository,
    private val bookCopyRepository: BookCopyRepository,
    private val reservationRepository: BookCopyRepository,
    private val checkoutRepository: CheckoutRepository
) {
    @GetMapping
    fun getBookCatalog(
        @PageableDefault(page = 0) pageable: Pageable,
        @QuerydslPredicate(root = Book::class) sampleBook: Predicate
    ): ResponseTemplate<List<BookDTO>> {
        val page: Page<Book> = bookRepository.findAll(sampleBook, pageable)
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
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_BORROWER')")
    fun reserveBook(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal borrower: UserDetails
    ): ResponseTemplate<Reservation> {
        var updatedBook : BookCopy = with(bookCopyRepository.findByIdOrNull(id)?: throw BookException("Book with id=$id not found")){
            if(available < 0) throw ReservationException("There is no available book")
            available--
            reservations.add(Reservation(this, borrower as Account) )
            this
        }
        updatedBook = bookCopyRepository.save(updatedBook)
            return ResponseTemplate(
                success = true,
                message = "Reservation placed",
                payload =  updatedBook.reservations.last(),
                status_code = HttpStatus.CREATED
            )
        }
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
        val bookData: BookDTO,
        val available: Int? = null,
        val isbn: String
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

    private fun BookCopy.mapDTO()  = BookCopyDTO(
        id = this.id,
        yearPublish = yearPublish,
        publisher = publisher.name,
        quantity = quantity,
        version = version,
        bookData = bookData.mapDTO(),
        available = available,
        isbn = isbn
    )