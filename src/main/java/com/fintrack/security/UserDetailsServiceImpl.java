package com.fintrack.security;

import com.fintrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Связующее звено между Spring Security и нашей базой данных.
 *
 * Spring Security вызывает loadUserByUsername(email) при каждой попытке входа.
 * Возвращаем UserDetails — стандартный интерфейс Spring Security,
 * который содержит имя пользователя, пароль и роли.
 *
 * Почему используем UserRepository напрямую, а не UserService?
 * UserDetailsService — низкоуровневая инфраструктура безопасности.
 * Смешивать её с бизнес-сервисом нежелательно во избежание циклических зависимостей.
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.fintrack.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login attempt for unknown email: {}", email);
                    return new UsernameNotFoundException("Пользователь не найден: " + email);
                });

        // User.withUsername() — стандартный билдер Spring Security.
        // username = email (им и будет Authentication.getName())
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
