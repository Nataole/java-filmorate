package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        setNameIfEmpty(user);
        User createdUser = userService.create(user);
        log.info("Пользователь создан: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null || userService.getById(user.getId()) == null) {
            log.warn("Попытка обновления несуществующего пользователя с id: {}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        setNameIfEmpty(user);

        User updatedUser = userService.update(user);
        log.info("Пользователь обновлен: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен список пользователей");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        User user = userService.getById(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        log.info("Получен пользователь с id={}", id);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление пользователя id={} в друзья к пользователю id={}", friendId, id);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление пользователя id={} из друзей пользователя id={}", friendId, id);
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Получение списка друзей пользователя id={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получение общих друзей пользователей id={} и id={}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}

