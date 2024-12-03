package com.evv.database.repository;

import com.evv.database.entity.Admin;
import com.evv.database.entity.Client;
import com.evv.database.entity.Role;
import com.evv.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByRole(Role role);

    Optional<User> findUserByEmail(String email);

    default Optional<Client> findClientByEmail(String email) {
        return findUserByEmail(email)
                .filter(user -> user instanceof Client)
                .map(user -> (Client) user);
    }

    default Optional<Client> findClientById(Long clientId) {
        return findById(clientId)
                .filter(user -> user instanceof Client)
                .map(user -> (Client) user);
    }

    default List<Client> findAllClients() {
        return findAllByRole(Role.CLIENT).stream()
                .map(user -> (Client) user)
                .toList();
    }
    default List<Admin> findAllAdmins() {
        return findAllByRole(Role.ADMIN).stream()
                .map(user -> (Admin) user)
                .toList();
    }

    default Client saveClient(Client client) {
        return save(client);
    }

    default Admin saveAdmin(Admin admin) {
        return save(admin);
    }

    Optional<User> findByEmail(String username);
}
