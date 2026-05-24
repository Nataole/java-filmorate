package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;


@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    public User addFriend(Long id, Long friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь с id=" + id + " или id=" + friendId + " не найден");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь с id=" + id + " или id=" + friendId + " не найден");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        return user;
    }

    public List<User> getFriends(Long id) {
        User user = getById(id);

        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user.getFriends().stream()
                .map(userStorage::getById)
                .toList();
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = getById(id);
        User otherUser = getById(otherId);

        if (user == null || otherUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(userStorage::getById)
                .toList();
    }
}
