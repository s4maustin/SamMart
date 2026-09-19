package com.sam.sammart;

import com.sam.sammart.dao.UserDao;
import com.sam.sammart.exception.ConflictException;
import com.sam.sammart.model.User;
import com.sam.sammart.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserDao userDao;

    @Test
    void registerFailsWhenEmailTaken() throws Exception {
        when(userDao.emailExists("a@b.co")).thenReturn(true);
        AuthService auth = new AuthService(userDao);
        assertThrows(ConflictException.class,
                () -> auth.register("Ann", "a@b.co", "Password1", "BUYER"));
    }

    @Test
    void registerInsertsBuyer() throws Exception {
        when(userDao.emailExists("a@b.co")).thenReturn(false);
        when(userDao.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(9);
            return u;
        });
        AuthService auth = new AuthService(userDao);
        assertEquals(9, auth.register("Ann", "a@b.co", "Password1", "BUYER").getId());
        assertEquals("BUYER", auth.register("Ann", "c@d.co", "Password1", "BUYER").getRole());
    }

    @Test
    void loginUnknownEmail() throws Exception {
        when(userDao.findByEmail("x@y.z")).thenReturn(Optional.empty());
        AuthService auth = new AuthService(userDao);
        assertThrows(Exception.class, () -> auth.login("x@y.z", "Password1"));
    }
}
