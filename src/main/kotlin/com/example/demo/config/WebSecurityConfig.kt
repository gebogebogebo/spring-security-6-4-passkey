package com.example.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {

        http
            .authorizeHttpRequests { authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/login").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { formLogin ->
                formLogin
                    .loginPage("/login").permitAll()
                    .defaultSuccessUrl("/mypage")
            }
//            .headers { headers ->
//                headers.frameOptions { it.disable() }
//            }

        return http.build()

    }

}