package vn.project.university.dbms.book_reservation.controller

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import vn.project.university.dbms.book_reservation.constant.AccountRole
import vn.project.university.dbms.book_reservation.constant.AccountStatus
import vn.project.university.dbms.book_reservation.constant.ResponseState
import vn.project.university.dbms.book_reservation.exception.ResourceConflict
import vn.project.university.dbms.book_reservation.model.Account
import vn.project.university.dbms.book_reservation.model.ResponseTemplate
import vn.project.university.dbms.book_reservation.repository.AccountRepository
import java.util.*

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/register")
    fun register(@RequestBody registerReq: RegisterReq): ResponseTemplate<AccountRes> {
        // Validate email uniqueness
        with(accountRepository.validateLoginIdentity(registerReq.username, registerReq.email)){
            if (any {account -> account.email == registerReq.email }){
                throw ResourceConflict("Email ${registerReq.email} already exists")
            }
            if(any {account -> account._username == registerReq.username}){
                throw ResourceConflict("Email ${registerReq.username} already exists")
            }
        }
        // If the email or username is already unique, then transform the request and store into db
        val newAccount: Account = with(registerReq) {
            Account(
                _username = username,
                _password = passwordEncoder.encode(password),
                email = email
            )
        }
        val savedAccount = with(accountRepository.save(newAccount)) {
            AccountRes(
                username = _username,
                status = status,
                email = email,
                id = id ?: throw Error("Account creation failed")
            )
        }
        return ResponseTemplate<AccountRes>(
            status = ResponseState.SUCCESS,
            message = "Account created successfully",
            data = savedAccount
        )
    }
    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_LIBRARIAN}')")
    fun banAccount(
        @PathVariable id: UUID,
        @RequestParam(required = true, value = "status") status: AccountStatus
    ): ResponseTemplate<AccountRes> {
        val savedAccount: AccountRes = with(accountRepository.updateStatus(id, status)) {
            AccountRes(username = _username, status = status, id = id)
        }
        return ResponseTemplate<AccountRes>(
            status = ResponseState.SUCCESS,
            message = "Update successfully",
            data = savedAccount
        )
    }
    data class RegisterReq(
        @field:[NotBlank(message = "username is required")] val username: String,
        @field:NotBlank(message = "password is required") val password: String,
        @field:[NotBlank(message = "email is required") Email(message = "email address is not valid")]
        val email: String,
//        val phoneNumber: String?
    )
    class AccountRes(
        val id: UUID,
        val username: String,
        val status: AccountStatus? = null,
        val email: String? = null,
        val role: AccountRole? = null,
    )
}

