package com.ecommerce.deliveryperson.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class DeliveryPersonSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new DeliveryPersonServiceConfig();
    }

    @Bean(name = "bCryptPasswordEncoderForDp")
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(author -> author
                .requestMatchers("/dp-login","/forgot-password","/forgotPassword", "/checkOtp", 
                "/verify-otp", "/verifyPasswordOtp","/reset-password", "/createNewPassword").permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // Allow static resources
                .requestMatchers("/delivery/**").hasAuthority("DELIVERY_PERSON") // Secure delivery paths
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/dp-login")
                .loginProcessingUrl("/delivery/do-login-delivery-person")
                .defaultSuccessUrl("/delivery-person-dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/dp/dp-login?logout")
                .permitAll()
            )
            .authenticationManager(authenticationManager)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Adjust session management
            );

        return http.build();
    }
}






























