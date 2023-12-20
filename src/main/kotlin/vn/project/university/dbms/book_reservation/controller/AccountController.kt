package vn.project.university.dbms.book_reservation.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import vn.project.university.dbms.book_reservation.constant.AccountRole
import vn.project.university.dbms.book_reservation.constant.AccountStatus
import vn.project.university.dbms.book_reservation.constant.ResponseState
import vn.project.university.dbms.book_reservation.exception.AccountException
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
    fun register(@Valid @RequestBody registerReq: RegisterReq): ResponseTemplate<AccountRes> {
        // Validate email uniqueness
        with(accountRepository.validateLoginIdentity(registerReq.username, registerReq.email)){
            if (any {account -> account.email == registerReq.email }){
                throw ResourceConflict("Email ${registerReq.email} already exists")
            }
            if(any {account -> account._username == registerReq.username}){
                throw ResourceConflict("Username ${registerReq.username} already exists")
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
        newAccount.role = AccountRole.valueOf(registerReq.role)
        val savedAccount = with(accountRepository.save(newAccount)) {
            AccountRes(
                username = _username,
                status = status,
                email = email,
                id = id ?: throw Error("Account creation failed")
            )
        }
        return ResponseTemplate<AccountRes>(
            success = true,
            message = "Account created successfully",
            payload = savedAccount,
            status_code = HttpStatus.OK
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
            success = true,
            message = "Update successfully",
            payload = savedAccount,
            status_code = HttpStatus.OK
        )
    }
    data class RegisterReq(
        @field:[NotBlank(message = "username is required")] val username: String,
        @field:NotBlank(message = "password is required") val password: String,
        @field:[NotBlank(message = "email is required") Email(message = "email address is not valid")]
        val email: String,
        val role: String = AccountRole.BORROWER.value
//        val phoneNumber: String?
    ){
        init {
            if(role !in AccountRole.values().map { it.name }){
                throw AccountException("Unknown role name $role, allowed roles = ['BORROWER', 'LIBRARIAN']")
            }
        }
    }
    class AccountRes(
        val id: UUID,
        val username: String,
        val status: AccountStatus? = null,
        val email: String? = null,
        val role: AccountRole? = null,
    )
}

