package com.evv.database.repository;

import com.evv.TestUtils;
import com.evv.TransactionalIT;
import com.evv.database.entity.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@RequiredArgsConstructor
public class UserRepositoryIT extends TransactionalIT {

    private final UserRepository userRepository;

    private final EntityManager entityManager;

    @Test
    void findAllClients() {
        List<Client> actualResult = userRepository.findAllClients();
        assertThat(actualResult).hasSize(2);

        List<Client> onlyClients = actualResult.stream()
                .filter(client -> client.getRole() == Role.CLIENT)
                .toList();
        assertThat(onlyClients).hasSize(2);
    }

    @Test
    void findAllAdmins() {
        List<Admin> actualResult = userRepository.findAllAdmins();
        assertThat(actualResult).hasSize(2);

        List<Admin> onlyAdmins = actualResult.stream()
                .filter(client -> client.getRole() == Role.ADMIN)
                .toList();
        assertThat(onlyAdmins).hasSize(2);
    }

    @Test
    void findUserByEmail() {
        Optional<User> maybeUser = userRepository.findUserByEmail(TestUtils.SECOND_CLIENT_EMAIL);

        assertThat(maybeUser).isPresent();
        maybeUser
                .map(user -> (Client) user)
                .ifPresent
                        (client -> {
                                    assertEquals(client.getEmail(), TestUtils.SECOND_CLIENT_EMAIL);
                                    assertSame(client.getRole(), Role.CLIENT);
                                    assertEquals(client.getBirthDate(), LocalDate.of(1980, 1, 20));
                                    assertSame(client.getClientStatus(), ClientStatus.BLOCKED);
                                    assertSame(client.getGender(), Gender.MALE);
                                }
                        );
    }

    @Test
    void save() {
        Client client = new Client(null, "saveClientTest@it.com", "{noop}000", Role.CLIENT,
                LocalDate.of(2003, 1, 1), ClientStatus.ACTIVE, null,
                Gender.FEMALE, null, null);

        User actualClient = userRepository.save(client);

        assertThat(actualClient.getId()).isGreaterThan(0L);
        assertThat(actualClient).isExactlyInstanceOf(Client.class);

        Admin admin = new Admin(null, "saveAdminTest@it.com", "{noop}0d+b3h5h7x,v[",
                Role.CLIENT, 0);

        User actualAdmin = userRepository.save(admin);

        assertThat(actualAdmin.getId()).isGreaterThan(0L);
        assertThat(actualAdmin).isExactlyInstanceOf(Admin.class);
    }

    @Test
    void saveClient() {
        Client client = new Client(null, "saveClientTest@it.com", "{noop}000", Role.CLIENT,
                LocalDate.of(2003, 1, 1), ClientStatus.ACTIVE, null,
                Gender.FEMALE, null, null);

        Client actualResult = userRepository.saveClient(client);

        assertThat(actualResult.getId()).isGreaterThan(0L);

        assertThat(actualResult).isEqualTo(client);
    }

    @Test
    void saveAdmin() {
        Admin admin = new Admin(null, "saveClientTest@it.com", "{noop}0d+b3h5h7x,v[",
                Role.CLIENT, 0);

        Admin actualResult = userRepository.saveAdmin(admin);

        assertThat(actualResult.getId()).isGreaterThan(0L);

        assertThat(actualResult).isEqualTo(admin);
    }

    @Test
    void saveAndFlush() {
        Client client = new Client(null, "saveClientTest@it.com", "{noop}000", Role.CLIENT,
                LocalDate.of(2003, 1, 1), ClientStatus.ACTIVE, null,
                Gender.FEMALE, null, null);

        Long savedClientId = userRepository.saveAndFlush(client).getId();

        entityManager.clear();

        Optional<User> actualClient = userRepository.findById(savedClientId);

        assertThat(actualClient).isPresent();
        actualClient
                .map(user -> (Client) user)
                .ifPresent
                        (clnt -> {
                                    assertEquals(clnt.getEmail(), "saveClientTest@it.com");
                                    assertSame(clnt.getRole(), Role.CLIENT);
                                    assertEquals(clnt.getBirthDate(), LocalDate.of(2003, 1, 1));
                                    assertSame(clnt.getClientStatus(), ClientStatus.ACTIVE);
                                    assertSame(clnt.getGender(), Gender.FEMALE);
                                }
                        );
    }

    @Test
//    @Commit // Раскомментировать эту строку для проверки как отрабатывает Envers @Audited. Нужно смотреть логи Hibernate.
    void update() {
        User changedUser = userRepository.findUserByEmail(TestUtils.FIRST_CLIENT_EMAIL)
                .map(user -> {
                    user.setEmail("123@bk.ru");
                    return user;
                })
                .orElseThrow();

        Long savedClientId = changedUser.getId();

        userRepository.saveAndFlush(changedUser);

        entityManager.clear();

        Optional<User> actualUser = userRepository.findById(savedClientId);
        assertThat(actualUser).isPresent();
        actualUser.ifPresent(user ->
                assertEquals(user.getEmail(), "123@bk.ru")
        );
    }
}