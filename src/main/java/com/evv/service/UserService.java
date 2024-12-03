package com.evv.service;

import com.evv.config.AppProperties;
import com.evv.database.entity.*;
import com.evv.database.repository.UserRepository;
import com.evv.dto.*;
import com.evv.dto.validation.Adult;
import com.evv.exception.ClientCantBeSaveException;
import com.evv.mapper.UserMapper;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final ImageService imageService;

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    private final AppProperties appProperties;

    public List<ClientReadDto> findAllClients() {
        return userRepository.findAllClients().stream()
                .map(userMapper::clientToClientReadDto).toList();
    }

    public Optional<ClientReadDto> findClientById(Long clientId) {
        return userRepository.findClientById(clientId)
                .map(userMapper::clientToClientReadDto);
    }

    public Optional<ClientReadDto> findClientByEmail(String clientEmail) {
        return userRepository.findClientByEmail(clientEmail)
                .map(userMapper::clientToClientReadDto);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new CustomUserDetailsImpl(
                        user.getEmail(),
                        user.getPassword(), true, true, true,
                        isClientNonLocked(user),
                        Collections.singleton(user.getRole()),
                        userMapper.userToUserReadDto(user)
                ))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Failed to retrieve user: " + email));
    }

    private boolean isClientNonLocked(User user) {
        if (user instanceof Admin) {
            return true;
        } else {
            return !((Client) user).getClientStatus().equals(ClientStatus.BLOCKED);
        }
    }

    @Transactional
    public ClientReadDto create(ClientCreateEditDto clientDto) {
        return Optional.of(clientDto)
                .map(dto -> {
                    uploadImage(dto.getImage());
                    return userMapper.clientCreateEditDtoToClient(dto);
                } )
                .map(client -> {
                    client.setRole(Role.CLIENT);
                    client.setClientStatus(ClientStatus.ACTIVE);
                    return client;
                })
                .map(userRepository::save)
                .map(client -> { // Создание новой кредитной карты и нового счета для нового клиента
                    creditCardService.save(buildCreditCardDto(client.getId()));
                    accountService.save(buildAccountDto(client.getId()));
                    return client;
                })
                .map(userMapper::clientToClientReadDto)
                .orElseThrow(() -> new ClientCantBeSaveException(clientDto.getEmail()));
    }

    @SneakyThrows
    private void uploadImage(MultipartFile image) {
        if (image != null) {
            if (!image.isEmpty()) {
                imageService.upload(image.getOriginalFilename(), image.getInputStream());
            }
        }
    }

    @Transactional
    public ClientReadDto createIfNotExistForOAuth(@Email String email,
                                                  @Adult LocalDate birthDate) {
        Optional<Client> maybeExistedClient = userRepository.findClientByEmail(email);
        ClientReadDto result = null;
        if (maybeExistedClient.isEmpty()) {
            String randomPassword = new Base64StringKeyGenerator( // Случайный сложный пароль, который никогда не будет введен пользователем, а нужен только для удовлетворения NotNull ограничения БД.
                    new Random().ints(32, 64).findFirst().getAsInt()
            ).generateKey();
            var client = new ClientCreateEditDto(email, randomPassword, Role.CLIENT,
                    birthDate, ClientStatus.ACTIVE, null, null);
            result = create(client);
        } else {
            result = userMapper.clientToClientReadDto(maybeExistedClient.get());
        }
        return result;
    }

    private CreditCardDto.CreateEdit.Public buildCreditCardDto(Long clientId) {
        return new CreditCardDto.CreateEdit.Public(
                LocalDate.now().plusYears(appProperties.getPayment().getCreditCard().getExpirationYears()),
                BigDecimal.ZERO,
                BigDecimal.valueOf(-appProperties.getPayment().getCreditCard().getInitialLimit()),
                CreditCardStatus.ACTIVE,
                clientId);
    }

    private AccountDto.CreateEdit.Public buildAccountDto(Long clientId) {
        return new AccountDto.CreateEdit.Public(
                BigDecimal.valueOf(appProperties.getPayment().getAccount().getInitialBalance()),
                clientId
        );
    }
}
