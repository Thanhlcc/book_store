package vn.project.university.dbms.book_reservation

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookReservationApplication

fun main(args: Array<String>) {
    runApplication<BookReservationApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}
