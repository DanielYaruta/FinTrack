package com.fintrack.repository;

import com.fintrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JpaRepository<User, Long> — Spring Data генерирует реализацию этого интерфейса
 * в рантайме. Тебе не нужно писать никакого SQL или кода — Spring сам:
 *   - реализует save(), findById(), findAll(), delete() и ещё ~10 методов
 *   - по имени метода выводит SQL-запрос
 *
 * Метод findByEmail — Spring Data прочитает имя и сгенерирует:
 *   SELECT * FROM users WHERE email = ?
 *
 * Возвращаем Optional<User>, а не User, чтобы явно обработать случай "не найден"
 * без NullPointerException.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
