package vn.project.university.dbms.book_reservation.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.CorsDsl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsUtils
import vn.project.university.dbms.book_reservation.constant.AccountRole
import vn.project.university.dbms.book_reservation.exception.AccountException
import vn.project.university.dbms.book_reservation.repository.AccountRepository

@Configuration
@EnableWebSecurity(debug = true)
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val roles = mapOf(
            "borrower" to "ROLE_${AccountRole.BORROWER.toString()}",
            "librarian" to "ROLE_${AccountRole.LIBRARIAN.toString()}"
        )
        http {
            authorizeRequests {
                authorize(CorsUtils::isPreFlightRequest, permitAll)
                authorize("api/v1/books/**", permitAll)
                authorize("/api/v1/accounts/register", permitAll)
                authorize("/api/v1/accounts/**", hasRole(roles["librarian"]!!))
                authorize(anyRequest, permitAll)
            }
            csrf { disable() }
            httpBasic {}
        }
        return http.build()
    }

    @Bean
    fun userDetailService(accountRepository: AccountRepository): UserDetailsService {
        return UserDetailsService { username ->
            accountRepository.findBy_username(username!!)
                ?: throw AccountException("Unknown account credentials")
        }
    }

    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setPasswordEncoder(passwordEncoder())
        provider.setUserDetailsService(userDetailsService)
        return provider
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}


