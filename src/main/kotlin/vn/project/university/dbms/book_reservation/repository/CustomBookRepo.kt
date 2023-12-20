package vn.project.university.dbms.book_reservation.repository

interface CustomBookRepo {
    fun reserveBook(id: Long): Boolean
}
class CustomBookRepoImpl : CustomBookRepo {
    override fun reserveBook(id: Long): Boolean {
        return true;
    }

}
