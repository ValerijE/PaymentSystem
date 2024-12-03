package com.evv.mapper;

import com.evv.database.entity.*;
import com.evv.database.repository.UserRepository;
import com.evv.dto.AdminReadDto;
import com.evv.dto.ClientCreateEditDto;
import com.evv.dto.ClientReadDto;
import com.evv.dto.UserReadDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.UnknownFormatConversionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserMapperImpl userMapper;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private static final Long ADMIN_ID = 112L;

    private static final String ADMIN_EMAIL = "some_admin@test.com";

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "{noop}000", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null,
            Gender.MALE, new ArrayList<>(), new ArrayList<>());

    private final ClientReadDto clientReadDto = new ClientReadDto(CLIENT_ID, CLIENT_EMAIL, Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    private final ClientCreateEditDto clientCreateEditDto = new ClientCreateEditDto(CLIENT_EMAIL, "client_raw_password", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    private final Admin admin = new Admin(ADMIN_ID, ADMIN_EMAIL, "{noop}noop", Role.ADMIN, 0);

    @Test
    void clientToClientReadDto_NormalFlow() {
        // when
        ClientReadDto clientReadDto = userMapper.clientToClientReadDto(client);

        // then
        assertThat(clientReadDto.getId()).isEqualTo(CLIENT_ID);
        assertThat(clientReadDto.getEmail()).isEqualTo(CLIENT_EMAIL);
        assertThat(clientReadDto.getBirthDate()).isEqualTo(client.getBirthDate());
        assertThat(clientReadDto.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(clientReadDto.getRole()).isSameAs(Role.CLIENT);
        assertThat(clientReadDto.getGender()).isSameAs(Gender.MALE);
    }

    @Test
    void clientReadDtoToClient_NormalFlow() {
        // stub
        doReturn(Optional.of(client)).when(userRepository).findClientById(CLIENT_ID);

        // when
        Client client = userMapper.clientReadDtoToClient(clientReadDto);

        // then
        assertThat(client.getId()).isEqualTo(CLIENT_ID);
        assertThat(client.getEmail()).isEqualTo(CLIENT_EMAIL);
        assertThat(client.getBirthDate()).isEqualTo(this.clientReadDto.getBirthDate());
        assertThat(client.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(client.getRole()).isSameAs(Role.CLIENT);
        assertThat(client.getGender()).isSameAs(Gender.MALE);

        verify(userRepository).findClientById(CLIENT_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void userToUserReadDto_ProcessClientInstance_ReturnClientReadDto() {
        // when
        UserReadDto userReadDto = userMapper.userToUserReadDto(client);

        // then
        assertThat(userReadDto).isInstanceOf(ClientReadDto.class);

        ClientReadDto clientReadDto = (ClientReadDto) userReadDto;

        assertThat(clientReadDto.getId()).isEqualTo(CLIENT_ID);
        assertThat(clientReadDto.getEmail()).isEqualTo(CLIENT_EMAIL);
        assertThat(clientReadDto.getBirthDate()).isEqualTo(this.client.getBirthDate());
        assertThat(clientReadDto.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(clientReadDto.getRole()).isSameAs(Role.CLIENT);
        assertThat(clientReadDto.getGender()).isSameAs(Gender.MALE);
    }

    @Test
    void userToUserReadDto_ProcessAdminInstance_ReturnAdminReadDto() {
        // when
        UserReadDto userReadDto = userMapper.userToUserReadDto(admin);

        // then
        assertThat(userReadDto).isInstanceOf(AdminReadDto.class);

        AdminReadDto adminReadDto = (AdminReadDto) userReadDto;

        assertThat(adminReadDto.getId()).isEqualTo(ADMIN_ID);
        assertThat(adminReadDto.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(adminReadDto.getRole()).isSameAs(Role.ADMIN);
    }

    static class SomeUser extends User {}

    @Test
    void userToUserReadDto_ProcessUnknownInstance_ThrowUnknownFormatConversionException() {
        // given
        SomeUser someUser = new SomeUser();

        // when
        Assertions.assertThatThrownBy(() -> userMapper.userToUserReadDto(someUser))

        // then
                .isInstanceOf(UnknownFormatConversionException.class)
                .hasMessageContaining("Unknown instance of type User for id = null");
    }

    @Test
    void clientCreateEditDtoToClient_NormalFlow() {
        // stub
        String encodedPassword = UUID.randomUUID().toString();
        doReturn(encodedPassword).when(passwordEncoder).encode(anyString());

        // when
        Client client = userMapper.clientCreateEditDtoToClient(clientCreateEditDto);

        // then
        assertThat(client.getEmail()).isEqualTo(CLIENT_EMAIL);
        assertThat(client.getPassword()).isEqualTo(encodedPassword);
        assertThat(client.getBirthDate()).isEqualTo(this.clientCreateEditDto.getBirthDate());
        assertThat(client.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(client.getRole()).isSameAs(Role.CLIENT);
        assertThat(client.getGender()).isSameAs(Gender.MALE);
        verify(passwordEncoder).encode(anyString());
        verifyNoMoreInteractions(passwordEncoder);
    }

}
