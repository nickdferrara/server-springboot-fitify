package com.nickdferrara.fitify.identity.internal.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
internal class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.requestMatchers("/api/v1/webhooks/**").permitAll()
                it.requestMatchers("/actuator/health/**").permitAll()
                it.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "LOCATION_ADMIN")
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access") ?: return@setJwtGrantedAuthoritiesConverter emptyList()
            @Suppress("UNCHECKED_CAST")
            val roles = realmAccess["roles"] as? List<String> ?: return@setJwtGrantedAuthoritiesConverter emptyList()
            roles.map { SimpleGrantedAuthority("ROLE_$it") }
        }
        return converter
    }
}
