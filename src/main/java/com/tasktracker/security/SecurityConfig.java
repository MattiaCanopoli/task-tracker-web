package com.tasktracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.service.DBUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepo uRepo;

    public SecurityConfig(UserRepo uRepo) {
        this.uRepo = uRepo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/rest/**").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET,"/user").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH,"/user").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST,"/user").permitAll()
                .anyRequest().authenticated()
        );

        http.httpBasic(Customizer.withDefaults());

        http.formLogin(form -> form
                .defaultSuccessUrl("/rest/tasks", true)
                .permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new DBUserDetailsService(uRepo);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
