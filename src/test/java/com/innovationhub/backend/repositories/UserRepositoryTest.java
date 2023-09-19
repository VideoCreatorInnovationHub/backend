/*
package com.innovationhub.backend.repositories;

import com.innovationhub.backend.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository repository;

    User user1, user2;
    @BeforeEach
    public void setup() {
        repository.deleteAll();
        user1 = User.builder().id(0).email("t@gmail.com").username("u1").password("p").build();
        user2 = User.builder().id(1).email("t2@gmail.com").username("u2").password("p").build();
        repository.save(user1); repository.save(user2);
    }
    @AfterEach
    public void teardown() {
        repository.deleteAll();
    }

    @Test
    public void whenDeleteByIdFromRepository_thenDeletingShouldBeSuccessful() {
        repository.deleteById(user1.getId());
        assertEquals(1, repository.count());
    }

    @Test
    public void whenDeleteAllFromRepository_thenRepositoryShouldBeEmpty() {
        repository.deleteAll();
        assertEquals(0, repository.count());
    }
}
*/
