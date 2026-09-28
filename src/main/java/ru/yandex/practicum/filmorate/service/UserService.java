package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;


@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        setNameIfEmpty(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        getById(user.getId());
        setNameIfEmpty(user);
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User addFriend(Long id, Long friendId) {
        checkDifferentUsers(id, friendId);
        getById(id);
        getById(friendId);

        userStorage.addFriend(id, friendId);

        return getById(id);
    }

    public User removeFriend(Long id, Long friendId) {
        checkDifferentUsers(id, friendId);
        getById(id);
        getById(friendId);

        userStorage.removeFriend(id, friendId);

        return getById(id);
    }

    public List<User> getFriends(Long id) {
        User user = getById(id);

        return user.getFriends().stream()
                .map(this::getById)
                .toList();
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        checkDifferentUsers(id, otherId);
        User user = getById(id);
        User otherUser = getById(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::getById)
                .toList();
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkDifferentUsers(Long id, Long otherId) {
        if (id.equals(otherId)) {
            throw new ValidationException("Пользователь не может взаимодействовать сам с собой");
        }
    }
}
