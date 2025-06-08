package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setAge(30);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testFindById() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());

        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);
        assertFalse(result.isPresent());

        verify(userDao, times(1)).findById(999L);
    }

    @Test
    void testFindAll() {
        when(userDao.findAll()).thenReturn(List.of(testUser));

        List<User> result = userService.findAll();
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));

        verify(userDao, times(1)).findAll();
    }

    @Test
    void testSave() {
        when(userDao.save(testUser)).thenReturn(testUser);

        User result = userService.save(testUser);
        assertEquals(testUser, result);

        verify(userDao, times(1)).save(testUser);
    }

    @Test
    void testUpdate() {
        when(userDao.update(testUser)).thenReturn(testUser);

        User result = userService.update(testUser);
        assertEquals(testUser, result);

        verify(userDao, times(1)).update(testUser);
    }

    @Test
    void testDelete() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDao).delete(testUser);

        userService.delete(1L);

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).delete(testUser);
    }

    @Test
    void testDeleteNotFound() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        userService.delete(999L);

        verify(userDao, times(1)).findById(999L);
        verify(userDao, never()).delete(any());
    }
}