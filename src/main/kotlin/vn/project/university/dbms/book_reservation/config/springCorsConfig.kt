package vn.project.university.dbms.book_reservation.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SpringCorsConfig {
    @Bean
    fun myCorsConfig() : WebMvcConfigurer  {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedMethods("*")
                    .allowedOriginPatterns("*")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }
        }
    }

}
