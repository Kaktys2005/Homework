package com.example.userservice.dao;

import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
        // Проверяем, что контейнер запущен
        assertTrue(postgresContainer.isRunning());

        // Настраиваем Hibernate для работы с Testcontainers
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.url", postgresContainer.getJdbcUrl())
                .setProperty("hibernate.connection.username", postgresContainer.getUsername())
                .setProperty("hibernate.connection.password", postgresContainer.getPassword())
                .setProperty("hibernate.hbm2ddl.auto", "update") // Автоматическое создание таблиц
                .setProperty("hibernate.show_sql", "true") // Логирование SQL
                .setProperty("hibernate.format_sql", "true")
                .addAnnotatedClass(User.class);

        HibernateUtil.setConfiguration(configuration);
        userDao = new UserDaoImpl();
    }

    @BeforeEach
    void setUp() {
        // Очищаем таблицу перед каждым тестом
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            try {
                // Более надежный способ очистки таблицы
                session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
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