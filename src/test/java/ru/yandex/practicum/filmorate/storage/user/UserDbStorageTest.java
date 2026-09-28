package ru.yandex.practicum.filmorate.storage.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateAndFindUserById() {
        User user = new User();

        user.setEmail("test@test.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(user);

        assertNotNull(created.getId());

        User found = userStorage.getById(created.getId()).orElseThrow();

        assertEquals(created.getId(), found.getId());
        assertEquals("test@test.ru", found.getEmail());
        assertEquals("testLogin", found.getLogin());
        assertEquals("Test User", found.getName());
        assertEquals(LocalDate.of(2000, 1, 1), found.getBirthday());
    }

    @Test
    void shouldReturnEmptyForUnknownUser() {
        assertTrue(userStorage.getById(999L).isEmpty());
    }
}