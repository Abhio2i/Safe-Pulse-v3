// Security Configuration file for each API 

package com.maarco.config;

import com.maarco.security.JwtAuthenticationEntryPoint;
import com.maarco.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class securityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint point;
    @Autowired
    private JwtAuthenticationFilter filter;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/home/**")
                                .authenticated()
                                .requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/auth/refresh-token").permitAll()
                                .requestMatchers("/auth/health-trends/**").authenticated()
                                .requestMatchers("/register/**").permitAll()
                                .requestMatchers("/v3/api-docs").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/api/sleep-logs").permitAll()
                                .requestMatchers("/v3/api-docs/creatSleepLog").permitAll()
                                .requestMatchers("/logs").permitAll()
                                .requestMatchers("/ingredient/uploadImages").permitAll()
                                .requestMatchers("/api").permitAll()
                                .requestMatchers("/api/relationships/**").permitAll()
                                .requestMatchers("/api/location/**").permitAll()
                                .requestMatchers("/v3/api-docs").permitAll()
                                .requestMatchers("/api/sleep-logs").permitAll()
                                .requestMatchers("/api/rest/**").permitAll()
                                .requestMatchers("/notification/**").permitAll()
                                .requestMatchers("/api/zones/**").permitAll()
                                .requestMatchers("/api/emergency-contacts/**").permitAll()
                                .requestMatchers("/v3/api-docs/creatSleepLog").permitAll()
                                .requestMatchers(HttpMethod.GET).permitAll()
                                .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
