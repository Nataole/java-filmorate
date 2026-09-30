package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, Date.valueOf(user.getBirthday()));

            return statement;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());

        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs));
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                WHERE id = ?
                """;

        List<User> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToUser(rs),
                id
        );

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.getFirst();
        user.setFriends(getFriendIds(id));

        return Optional.of(user);
    }

    private User mapRowToUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashSet<>());

        return user;
    }

    private HashSet<Long> getFriendIds(Long userId) {
        String sql = """
                SELECT friend_id
                FROM friendships
                WHERE user_id = ?
                """;

        return new HashSet<>(
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> rs.getLong("friend_id"),
                        userId
                )
        );

    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = """
                INSERT INTO friendships (user_id, friend_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = """
                DELETE FROM friendships
                WHERE user_id = ? AND friend_id = ?
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
            SELECT u.id,
                   u.email,
                   u.login,
                   u.name,
                   u.birthday
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToUser(rs),
                userId
        );
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
            SELECT u.id,
                   u.email,
                   u.login,
                   u.name,
                   u.birthday
            FROM users u
            JOIN friendships f1 ON u.id = f1.friend_id
            JOIN friendships f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToUser(rs),
                userId,
                otherId
        );
    }
}

