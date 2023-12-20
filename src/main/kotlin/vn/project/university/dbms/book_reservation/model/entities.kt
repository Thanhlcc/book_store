package vn.project.university.dbms.book_reservation.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import vn.project.university.dbms.book_reservation.constant.AccountRole
import vn.project.university.dbms.book_reservation.constant.AccountStatus
import vn.project.university.dbms.book_reservation.constant.BookStatus
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long,
    val name: String
) {
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    val books: MutableList<Book> = mutableListOf()
}

@Entity class Publisher(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long,
    @Column(nullable = false, unique = true) val name: String,
)
@Entity class Book(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long,
    @Column(nullable = false, length = 50)var title: String,
    @ManyToOne(optional = false) var category: Category,
    @Column(columnDefinition = "text not null") var description: String,
    @Column(columnDefinition = "json not null") var authors: String,
){
    @OneToMany(mappedBy = "bookData", cascade = [CascadeType.ALL])
    val versions : MutableList<BookCopy> = mutableListOf()
    @CreationTimestamp var createdAt: LocalDateTime = LocalDateTime.now()
    @UpdateTimestamp var updatedAt: LocalDateTime = LocalDateTime.now()
}
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["version", "book_data"])]
)
@Entity class BookCopy(
    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)val id: Long,
    @Column(nullable = false) val yearPublish: LocalDate,
    @ManyToOne(optional = false) val publisher: Publisher,
    var quantity: Int,
    @Column(unique = true) val version: Int? = 1,
){
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_data")
    lateinit var bookData: Book
    @OneToMany(mappedBy = "book")
    val reservations: MutableList<Reservation> = mutableListOf()
}

@Entity class Reservation(
    @ManyToOne(optional = false) val book: BookCopy,
    @ManyToOne(optional = false) val borrower: Account,
    @Id val id: String? = null,
){
    @CreationTimestamp val startTime: LocalDateTime = LocalDateTime.now()
    @Column(nullable = false) val endTime: LocalDateTime = startTime.plusDays(7)
    @UpdateTimestamp val pickupAt: LocalDateTime? = null
    val isDeleted: Boolean = false
}

@Entity class Account(
    @Column(nullable = false, unique = true, name="username") val _username: String,
    @Column(nullable = false, name = "password") val _password: String,
    @Column(nullable = false, unique = true) val email: String,
    @Id @UuidGenerator val id: UUID? = null,
) : UserDetails {
    @Column(columnDefinition = "CHAR(15)")
    val phoneNumber: String? =  null
    @Column(columnDefinition = "CHAR(12)")
    val employeeId: String? = null
    @CreationTimestamp val createdAt: LocalDateTime? = null
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status = AccountStatus.ACTIVE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role = AccountRole.BORROWER

    @OneToMany(mappedBy = "borrower", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reservations: MutableList<Reservation> = mutableListOf()

    @OneToMany(mappedBy = "borrower", cascade = [CascadeType.ALL], orphanRemoval = true)
    val borrowedBook: MutableList<Checkout> = mutableListOf()
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(GrantedAuthority { "ROLE_${role}" })
    }

    override fun getPassword(): String = this._password

    override fun getUsername(): String = _username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

@Entity class Checkout(
    @Id val id: String,
@CreationTimestamp val startTime: LocalDateTime?,
@Column(nullable = false) val endTime: LocalDateTime,
@UpdateTimestamp val pickupAt: LocalDateTime,
val isDeleted: Boolean? = false,
@ManyToOne(optional = false) val book: BookCopy,
@ManyToOne(optional = false) val borrower: Account
)





