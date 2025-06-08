package com.example.userservice.dao;

import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.1-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static UserDao userDao;

    @BeforeAll
    static void beforeAll() {
        // Настраиваем Hibernate для использования Testcontainers
        System.setProperty("hibernate.connection.url", postgresContainer.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgresContainer.getUsername());
        System.setProperty("hibernate.connection.password", postgresContainer.getPassword());

        userDao = new UserDaoImpl();
    }

    @BeforeEach
    void setUp() {
        // Очищаем таблицу перед каждым тестом
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("delete from User").executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
    }

    @Test
    void testSaveAndFindById() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(30);

        User savedUser = userDao.save(user);
        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Test User", foundUser.get().getName());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> foundUser = userDao.findById(999L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAll() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        user1.setAge(25);
        userDao.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setAge(30);
        userDao.save(user2);

        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void testUpdate() {
        User user = new User();
        user.setName("Original Name");
        user.setEmail("original@example.com");
        user.setAge(25);
        User savedUser = userDao.save(user);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@example.com");
        savedUser.setAge(30);
        User updatedUser = userDao.update(savedUser);

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getName());
        assertEquals("updated@example.com", foundUser.get().getEmail());
        assertEquals(30, foundUser.get().getAge());
    }

    @Test
    void testDelete() {
        User user = new User();
        user.setName("To Delete");
        user.setEmail("delete@example.com");
        user.setAge(40);
        User savedUser = userDao.save(user);

        userDao.delete(savedUser);

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }
}