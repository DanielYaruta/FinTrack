package com.fintrack.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Главный конфиг Spring Security.
 *
 * В Spring Security 6 (Spring Boot 3) нет наследования от WebSecurityConfigurerAdapter —
 * вместо этого объявляем бины SecurityFilterChain и PasswordEncoder.
 *
 * Цепочка фильтров (FilterChain):
 *   каждый HTTP-запрос проходит через цепочку фильтров Spring Security.
 *   Если запрос не аутентифицирован и URL не разрешён анонимно — редирект на /login.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Статика и страница входа — доступны без авторизации
                .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/error").permitAll()
                // Всё остальное — только для авторизованных пользователей
                .anyRequest().authenticated()
            )

            // Form Login — стандартная форма логина с нашим шаблоном
            .formLogin(form -> form
                .loginPage("/login")                      // наш кастомный шаблон login.html
                .defaultSuccessUrl("/dashboard", true)    // true = всегда на /dashboard после входа
                .permitAll()
            )

            // Выход из системы
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")        // после выхода — на страницу входа с параметром
                .permitAll()
            )

            // CSRF: оставляем для форм (Thymeleaf добавляет токен автоматически).
            // Для REST API (/api/**) отключаем: AJAX-запросы с того же Origin защищены
            // браузерной политикой Same-Origin Policy.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }

    /**
     * BCryptPasswordEncoder — стандарт хеширования паролей.
     * Bcrypt намеренно медленный (cost=10 → ~100ms на проверку), что защищает
     * от брутфорс-атак, даже если база данных утечёт.
     *
     * Никогда не используй MD5/SHA1/SHA256 для паролей — они слишком быстрые.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
