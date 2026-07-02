package com.fintrack.service;

import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.User;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Service — маркер для Spring: создать бин и управлять им.
 *   По смыслу то же самое, что @Component, но явно говорит:
 *   "это сервисный слой, здесь живёт бизнес-логика".
 *
 * @Transactional(readOnly = true) на уровне класса — все методы по умолчанию
 *   работают в read-only транзакции. Это даёт:
 *   - производительность: Hibernate не отслеживает изменения объектов (dirty checking)
 *   - безопасность: запись в read-only транзакции невозможна
 *   Методы, которые пишут в БД, переопределяют это своим @Transactional.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // Spring внедряет репозиторий через конструктор (предпочтительный способ над @Autowired)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Пользователь", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, String name, String email) {
        User user = findById(id);
        user.setName(name);
        user.setEmail(email);
        // save() не нужен явно: Hibernate видит изменения управляемой сущности
        // и сам делает UPDATE при завершении транзакции (dirty checking).
        return user;
    }
}
