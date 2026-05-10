package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateCorrectUser() {
        User user = new User(
                null,
                "test@mail.ru",
                "login",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithBlankEmail() {
        User user = new User(
                null,
                "",
                "login",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithInvalidEmail() {
        User user = new User(
                null,
                "mail.ru",
                "login",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithBlankLogin() {
        User user = new User(
                null,
                "test@mail.ru",
                "",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithBlankSpacesLogin() {
        User user = new User(
                null,
                "test@mail.ru",
                "   ",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithSpacesInLogin() {
        User user = new User(
                null,
                "test@mail.ru",
                "bad login",
                "Name",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateUserWithBlankName() {
        User user = new User(
                null,
                "test@mail.ru",
                "login",
                "",
                LocalDate.of(2000, 1, 1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateUserWithBirthdayInFuture() {
        User user = new User(
                null,
                "test@mail.ru",
                "login",
                "Name",
                LocalDate.now().plusDays(1)
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateUserWithBirthdayToday() {
        User user = new User(
                null,
                "test@mail.ru",
                "login",
                "Name",
                LocalDate.now()
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

}
