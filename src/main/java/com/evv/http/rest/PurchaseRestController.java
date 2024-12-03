package com.evv.http.rest;

import com.evv.dto.PageResponse;
import com.evv.dto.PurchaseReadDto;
import com.evv.exception.PurchaseForClientCantBeSavedException;
import com.evv.exception.PurchaseNotFoundException;
import com.evv.http.rest.payload.PurchaseCreatePayload;
import com.evv.http.rest.payload.PurchaseUpdatePayload;
import com.evv.dto.CustomUserDetails;
import com.evv.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static com.evv.util.SwaggerConstants.*;
import static com.evv.util.Util.DEFAULT_PAGE_SIZE;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Покупки клиентов", description = "API для управления покупками клиентов")
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = "UNAUTHORIZED: пользователь не авторизован",
                content = @Content(
                        mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        schema = @Schema(example = SCHEMA_EXAMPLE_401)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "INTERNAL SERVER ERROR: Ошибка сервера при обработке запроса",
                content = @Content(
                        mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        schema = @Schema(example = SCHEMA_EXAMPLE_500)
                )
        )
})
public class PurchaseRestController {

    private final PurchaseService purchaseService;

    /**
     * Если clientId не указан:
     * <ol>
     * <li> пользователям с ролью CLIENT возвращаются собственные данные;
     * <li> пользователям с ролью ADMIN возвращаются данные всех клиентов.
     * </ol>
     * <p>Если clientId указан:
     * <ol>
     * <li> пользователям с ролью CLIENT:
     * <ul>
     * <li>если clientId совпадает с собственным id, то возвращаются собственные данные;
     * <li>иначе ответ со статусом 403.
     * </ul>
     * <li> пользователям с ролью ADMIN возвращаются данные клиента с id=clientId.
     * </ol>
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Получение всех покупок с пагинацией",
            description = """
                    Если clientId не указан: <br>
                    - пользователям с ролью CLIENT возвращаются собственные данные; <br>
                    - пользователям с ролью ADMIN возвращаются данные всех клиентов. <br>
                    Если clientId указан: <br>
                    - пользователям с ролью CLIENT: <br>
                        * если clientId совпадает с собственным id, то возвращаются собственные данные, <br>
                        * иначе ответ со статусом 403; <br>
                    - пользователям с ролью ADMIN возвращаются данные клиента с id=clientId.
                    """,
            parameters = {
                    @Parameter(name = "clientId", description = "Идентификационный номер клиента"),
                    @Parameter(name = "pageable", description = "Стандартный объект пагинации. При отсутствии значений действуют параметры по умолчанию: page=0, size=12, без сортировки")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: возвращается PageResponse с найденными покупками",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponseOfPurchaseReadDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: клиент запросил данные другого клиента",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(example = SCHEMA_EXAMPLE_403)
                            )
                    )
            }
    )
    public PageResponse<PurchaseReadDto> findAll(@RequestParam(required = false) Long clientId,
                                                 @PageableDefault(size = DEFAULT_PAGE_SIZE ) Pageable pageable,
                                                 @AuthenticationPrincipal CustomUserDetails client) {
        Page<PurchaseReadDto> purchases;
        if (client.getAdmin() != null) { // значит авторизован ADMIN
            if (clientId != null) { // значит запрашивает покупки конкретного клиента
                purchases = purchaseService.findAllByClientId(clientId, pageable);
            } else { // значит запрашивает покупки всех клиента
                purchases = purchaseService.findAll(pageable);
            }
        } else { // значит авторизован CLIENT
            Long authClientId = client.getClient().getId();
            if (clientId != null) { // значит возможно запрашивает покупки другого клиента
                if (!clientId.equals(authClientId)) {
                    throw new AccessDeniedException("Client id=%d attempted to get purchases data of client id=%d"
                            .formatted(authClientId, clientId));
                } else { // вернуть собственные покупки
                    purchases = purchaseService.findAllByClientId(authClientId, pageable);
                }
            } else { // вернуть собственные покупки
                purchases = purchaseService.findAllByClientId(authClientId, pageable);
            }
        }
        return PageResponse.of(purchases);
    }

    /**
     * Класс-адаптер для использования в Open API по причине невозможности указания параметризованного класса.
     */
    public static class PageResponseOfPurchaseReadDto extends PageResponse<PurchaseReadDto> {
        public PageResponseOfPurchaseReadDto(List<PurchaseReadDto> content, Metadata metadata) {
            super(content, metadata);
        }
    }

    /**
     * Доступ предоставляется пользователям с ролью CLIENT если производится запрос на получение собственных данных,
     * а также пользователям с ролью ADMIN.
     * <p>В образовательных целях защита осуществляется классом PurchaseAuthorizationManager после его регистрации
     * в SecurityFilterChain. Больше никогда так делать не буду, т.к. сложно разделить статусы 403 и 404. Для
     * этих целей если запрашиваемой purchase нет, то запрос findById в базу данных происходит как в
     * PurchaseAuthorizationManager, так и дополнительно в методе контроллера.
     */
    @GetMapping("/{purchaseId:\\d+}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение покупки по id",
            description = """
                    Доступ предоставляется пользователям с ролью CLIENT если производится запрос на получение 
                    собственных данных, \n а также пользователям с ролью ADMIN.
                    """,
            parameters = {
                    @Parameter(name = "purchaseId", description = "Идентификационный номер покупки")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: возвращается PurchaseReadDto",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PurchaseReadDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: клиент запросил данные другого клиента",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(example = SCHEMA_EXAMPLE_403)
                            )
                    )
            }
    )
    public PurchaseReadDto findById(@PathVariable Long purchaseId) {
        return purchaseService.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
    }

    /**
     * Доступ предоставляется пользователям с ролью CLIENT.
     * <p> Владельцем покупки назначается авторизованный в настоящий момент клиент.
     * <p> В ходе создания покупки происходит списание денежных средств согласно указанным реквизитам.
     *
     * @param payload - данные, необходимые для совершения покупки, в том числе способ оплаты: кредитной картой
     *                или банковским счетом, для выбранного способа оплаты должен быть указан id (creditCardId или
     *                accountId), id другого способа оплаты должен быть "null"
     */

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT')")
    @GetMapping("/{purchaseId:\\d+}")
    @Operation(
            summary = "Создание покупки",
            description = """
                    Доступ предоставляется пользователям с ролью CLIENT. <br>
                    Владельцем покупки назначается авторизованный в настоящий момент клиент. <br>
                    В ходе создания покупки происходит списание денежных средств согласно указанным реквизитам.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Содержит данные, необходимые для совершения покупки, в том числе способ оплаты:
                            кредитной картой или банковским счетом. Для выбранного способа оплаты должен быть указан
                            id (creditCardId или accountId), id другого способа оплаты должен быть "null"
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PurchaseCreatePayload.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = """
                                    CREATED: возвращается ResponseEntity содержащий созданную покупку. <br>
                                    Показаны примеры возвращаемого JSON для обоих способов оплаты.
                                    """,
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Оплата кредитной картой",
                                                    value = SCHEMA_EXAMPLE_PURCHASE_CREDIT_CARD,
                                                    description = "В поле payment объект PaymentCreditCard"),
                                            @ExampleObject(
                                                    name = "Оплата банковским счетом",
                                                    value = SCHEMA_EXAMPLE_PURCHASE_ACCOUNT,
                                                    description = "В поле payment объект PaymentAccount")
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: попытка создания покупки пользователем с ролью ADMIN",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(example = SCHEMA_EXAMPLE_403)
                            )
                    )
            }
    )
    public ResponseEntity<PurchaseReadDto> create(@Validated @RequestBody PurchaseCreatePayload payload,
                                                  BindingResult bindingResult,
                                                  UriComponentsBuilder uriComponentsBuilder,
                                                  @AuthenticationPrincipal CustomUserDetails client) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            Long clientId = client.getClient().getId();
            PurchaseReadDto purchaseReadDto;
            if (payload.getCreditCardId() != null) { // Значит выбран способ оплаты кредитной картой
                purchaseReadDto = purchaseService.createPurchaseCreditCardByIds(payload.getTotalCost(), clientId,
                                payload.getChosenProductIds(), payload.getCreditCardId())
                        .orElseThrow(() -> new PurchaseForClientCantBeSavedException(clientId));
            } else { // Значит payload.getAccountId() != null, а значит выбран способ оплаты банковским счетом
                purchaseReadDto = purchaseService.createPurchaseAccountByIds(payload.getTotalCost(), clientId,
                                payload.getChosenProductIds(), payload.getAccountId())
                        .orElseThrow(() -> new PurchaseForClientCantBeSavedException(clientId));
            }
            return ResponseEntity
                    .created(uriComponentsBuilder
                            .replacePath("/api/v1/purchase/{purchaseId}")
                            .build(Map.of("purchaseId", purchaseReadDto.getId())))
                    .body(purchaseReadDto);
        }
    }

    /**
     * Доступ предоставляется только пользователям с ролью ADMIN.
     * <p>В ходе удаления покупки производится возврат денежных средств и удаление платежа.
     */
    @DeleteMapping("/{purchaseId:\\d+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Удаление покупки по id",
            description = """
                    Доступ предоставляется только пользователям с ролью ADMIN.
                    <p>В ходе удаления покупки производится возврат денежных средств и удаление платежа.
                    """,
            parameters = {
                    @Parameter(name = "purchaseId", description = "Идентификационный номер покупки")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "NO CONTENT: возвращается пустой ResponseEntity"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: попытка удаления покупки пользователем с ролью CLIENT",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(example = SCHEMA_EXAMPLE_403)
                            )
                    )
            }
    )
    public ResponseEntity<?> delete(@PathVariable Long purchaseId) {
        purchaseService.delete(purchaseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Доступ предоставляется только пользователям с ролью ADMIN.
     * <p>Изменение владельца покупки не предусмотрено.
     * <p>В ходе обновления покупки старый платеж удаляется с возвратом средств.
     * Далее создается новый платеж на новую сумму с новым способом оплаты.
     *
     * @param payload - данные, необходимые для обновления покупки, в том числе способ оплаты: кредитной картой
     *                или банковским счетом, для выбранного способа оплаты должен быть указан id (creditCardId или
     *                accountId), id другого способа оплаты должен быть "null"
     */
    @PutMapping("/{purchaseId:\\d+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Обновление покупки по id",
            description = """
                    Доступ предоставляется только пользователям с ролью ADMIN. <br>
                    Изменение владельца покупки не предусмотрено. <br>
                    В ходе обновления покупки старый платеж удаляется с возвратом средств. Далее создается новый платеж на новую сумму с новым способом оплаты.
                    """,
            parameters = {
                    @Parameter(name = "purchaseId", description = "Идентификационный номер покупки")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Содержит данные, необходимые для обновления покупки, в том числе способ оплаты:
                            кредитной картой или банковским счетом. Для выбранного способа оплаты должен быть указан
                            id (creditCardId или accountId), id другого способа оплаты должен быть "null".
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PurchaseUpdatePayload.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = """
                                    ОК: возвращается пустой ResponseEntity
                                    """
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: попытка обновления покупки пользователем с ролью CLIENT",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(example = SCHEMA_EXAMPLE_403)
                            )
                    )
            }
    )
    public ResponseEntity<?> update(@PathVariable Long purchaseId,
                                    @Validated @RequestBody PurchaseUpdatePayload payload,
                                    BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            PurchaseReadDto purchaseReadDto;
            if (payload.getCreditCardId() != null) { // Значит выбран способ оплаты кредитной картой
                purchaseReadDto = purchaseService.updatePurchaseCreditCardByIds(purchaseId, payload.getTotalCost(),
                                payload.getChosenProductIds(), payload.getCreditCardId())
                        .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
            } else { // Значит payload.getAccountId() != null, а значит выбран способ оплаты банковским счетом
                purchaseReadDto = purchaseService.updatePurchaseAccountByIds(purchaseId, payload.getTotalCost(),
                                payload.getChosenProductIds(), payload.getAccountId())
                        .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
            }
            return ResponseEntity
                    .ok()
                    .body(purchaseReadDto);
        }
    }
}
