package vn.project.university.dbms.book_reservation.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import vn.project.university.dbms.book_reservation.constant.AccountStatus
import vn.project.university.dbms.book_reservation.model.*
import java.util.*

interface BookRepository : CrudRepository<Book, Long>, QuerydslPredicateExecutor<Book> {
    @Query(
        """
        SELECT * FROM book
    """, nativeQuery = true
    )
    fun findAll(pageable: Pageable): Page<Book>

//    fun findByIdAndVersionsId(bookId: Long, version: Int) : Book
}

interface BookCopyRepository : CrudRepository<BookCopy, Long> {
    fun findByBookDataIdAndVersion(bookId: Long, version: Int): BookCopy?
}

interface AccountRepository : CrudRepository<Account, UUID> {
    @Query(
        """
        SELECT acc
        FROM Account acc
        WHERE acc.email = :email OR acc._username =:username
    """
    )
    fun validateLoginIdentity(
        @Param("username") username: String,
        @Param("email") email: String
    ): List<Account>

    @Query("""
        FROM Account acc where acc._username = :username
    """
    )
    fun findByUsername(@Param("username") username: String): Account?

    @Modifying
    @Query(
        """
        UPDATE Account acc 
        SET acc.status=:status 
        WHERE acc.id=:id
    """
    )
    fun updateStatus(
        @Param(value = "id") id: UUID,
        @Param(value = "status") status: AccountStatus
    ): Account
}

interface ReservationRepository : CrudRepository<Reservation, UUID>, QuerydslPredicateExecutor<Reservation> {
    fun findAll(pageable: Pageable): Page<Reservation>
}

interface CheckoutRepository : CrudRepository<Checkout, UUID>
