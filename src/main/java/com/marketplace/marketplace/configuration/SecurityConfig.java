package com.marketplace.marketplace.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    public static final int T_30_DAYS_IN_SECONDS = 2592000;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/registration").permitAll();
                    auth.requestMatchers("/static/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(login -> {
                    login.loginPage("/login");
                    login.usernameParameter("email");
                    login.usernameParameter("username");
                })
                .cors(AbstractHttpConfigurer::disable)
                .logout(logout -> {
                    logout.logoutUrl("/logout").permitAll();
                    logout.logoutSuccessUrl("/login").permitAll();
                    logout.clearAuthentication(true);
                    logout.invalidateHttpSession(true);
                })
                .rememberMe(rememberMe -> {
                    rememberMe.tokenValiditySeconds(T_30_DAYS_IN_SECONDS);
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}