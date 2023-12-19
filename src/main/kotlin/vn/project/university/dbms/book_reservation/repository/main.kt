package vn.project.university.dbms.book_reservation.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import vn.project.university.dbms.book_reservation.constant.AccountStatus
import vn.project.university.dbms.book_reservation.model.Account
import vn.project.university.dbms.book_reservation.model.Book
import vn.project.university.dbms.book_reservation.model.Checkout
import vn.project.university.dbms.book_reservation.model.Reservation
import java.util.*

interface BookRepository : CrudRepository<Book, Long>
interface AccountRepository : CrudRepository<Account, UUID>{
    @Query("""
        SELECT acc
        FROM Account acc
        WHERE acc.email = :email OR acc._username =:username
    """)
    fun validateLoginIdentity(@Param("username") username: String,
                              @Param("email") email: String
    ): List<Account>

    fun findBy_username(username: String) : Account?
    @Modifying
    @Query("""
        UPDATE Account acc 
        SET acc.status=:status 
        WHERE acc.id=:id
    """)
    fun updateStatus(
        @Param(value="id") id: UUID,
        @Param(value="status") status: AccountStatus
    ) : Account
}
interface ReservationRepository : CrudRepository<Reservation, String>
interface CheckoutRepository : CrudRepository<Checkout, String>
