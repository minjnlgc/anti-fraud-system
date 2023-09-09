package antifraud.security;

import antifraud.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl myUserDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl myUserDetailsService,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.myUserDetailsService = myUserDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")

                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")

                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")

                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")

                        .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard/**").hasRole("SUPPORT")

                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasRole("SUPPORT")

                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        http.httpBasic(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());
        http.headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
