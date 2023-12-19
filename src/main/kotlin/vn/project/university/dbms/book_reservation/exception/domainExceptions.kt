package vn.project.university.dbms.book_reservation.exception

class BookException(msg: String) : RuntimeException(msg)
class AccountException(msg: String) : RuntimeException(msg)
class ReservationException(msg: String) : RuntimeException(msg)
class CheckoutException(msg: String) : RuntimeException(msg)
class ResourceConflict(msg: String) : RuntimeException(msg)