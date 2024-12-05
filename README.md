<h1 style="text-align: center; font-size: 30px">
Payment system
</h1>

&nbsp; Вэб-сервис, предназначенный для совершения покупок в интернет-магазине, который включает операции по выбору товаров, выбору способа оплаты, оплате покупки. <br>
&nbsp; Приложение написано в образовательных целях в качестве итоговой работы после обучения по специальности Java-бэкенд-разработчик.

# Оглавление :bookmark_tabs:
- [Технологии](#technologies)
- [Функции](#functions)
  - [Собственный слой представления](#собственный-слой-представления)
      - [Основные страницы слоя представления](#основные-страницы-слоя-представления)
      - [Интернационализация](#интернационализация)
  - [REST контроллер](#rest-контроллер)
    - [Документация REST ендпоинтов](#документация-rest-ендпоинтов)
  - [База данных](#база-данных)
    - [Схема](#схема)
    - [Аудит](#аудит)
    - [Изоляция](#изоляция)
  - [Логирование сервисного слоя](#логирование-сервисного-слоя)
- [Реализация](#implementation)
  - [Полиморфизм ассоциаций](#полиморфизм-ассоциаций)
  - [Разделение SecurityFilterChain](#разделение-securityfilterchain)
  - [Тесты](#тесты)
    - [@Transactional и тестовые данные](#transactional-и-тестовые-данные)
    - [Покрытие тестами](#покрытие-тестами)
- [Прочее ](#other)

---

# <a id="technologies"> Технологии :helicopter: :airplane: :rocket: </a>

#### Общие компоненты

- **Spring Boot v3.2.5** - Автоконфигурация модулей, внедрение версий зависимостей
- **spring-boot-starter-validation** - Автоконфигурация hibernate-validator для валидации DTO и Payload
- **spring-boot-starter-thymeleaf** - Автоконфигурация движка шаблонов Thymeleaf

#### Данные

- **PostgreSQL v16.4** - Основная СУБД
- **Liquibase** - Контроль версий схемы БД
- **Querydsl** - Генерация вспомогательных сущностей для упрощения реализации фильтрации результатов запроса в БД
- **spring-boot-starter-data-jpa** - Автоконфигурация модулей для работы с БД, в т.ч. hibernate-core и spring-data-jpa
- **spring-data-envers** - Автоконфигурация hibernate-envers для аудита таблиц

#### Безопасность

- **spring-boot-starter-security** - Предоставляет SecurityFilterChain, UsernamePasswordAuthenticaionFilter, 
AuthorizationFilter, CsrfFilter, @EnableMethodSecurity и прочие классы и интерфейсы для защиты приложения
- **spring-security-oauth2-client** - Предоставляет зависимости для осуществления взаимодействия между AuthClient и AuthServer
- **spring-security-oauth2-jose** - Работа с токенами: JWT, Access, Refresh

#### Утилиты и вспомогательные библиотеки

- **Lombok** - Уменьшение шаблонного кода
- **Gradle** - Инструмент сборки проекта
- **Mapstruct** - Преобразование объектов между слоями
- **Jackson Databind** - Работа с JSON
- **springdoc-openapi-starter-webmvc-ui** - Автоматизированная документация REST API

#### Тестирование

- **JUnit5** - Основной фреймворк для тестов
- **spring-boot-starter-test** - Работа с ApplicationContext, функционал MockMvc, конфигурация прочих модулей
- **spring-security-test** - Внедрение тестовых пользователей в интеграционные тесты.
- **Mockito** - Создание дублеров для модульного тестирования
- **AssertJ, Hamcrest** - Удобные условия проверки
- **Testcontainers** - Запуск оригинальной БД в Docker-контейнере для обеспечения тестов данными
- **JaCoCo** - Анализ полноты покрытия кода тестами

---
# <a id="functions"> Функции :trolleybus: :mountain_cableway: :suspension_railway: </a>
## Собственный слой представления
### Основные страницы слоя представления
- **Регистрация нового клиента** на странице registration с валидацией введенных данных:  
&emsp; &emsp; ![Login](/images/markdown/00_Registration_Errors_rus.png)
- **Авторизация клиента** на странице login, на которой дополнительно реализованы возможность авторизации с 
использованием **сервисов Google и Yandex** и вывод сообщений валидации:  
&emsp; &emsp; ![Authorization](/images/markdown/01_Login_Error_rus.png)
- **Страница выбора товаров** с фильтрацией, пагинацией и валидацией на которую происходит автоматическое 
перенаправление в случае успешной авторизации пользователя с ролью CLIENT:  
&emsp; &emsp; ![Purchase_choice](/images/markdown/02_Product_choice_rus_filter.png)
<br><br>
возможные сообщения валидации: <br>
&emsp; &emsp; ![Purchase_choice](/images/markdown/02_Product_choice_String_EmptyList_rus.png)<br>
&emsp; &emsp; ![Purchase_choice](/images/markdown/02_Product_choice_String_NarrowFilter_rus.png)
- **Страница выбора способа оплаты** - кредитной картой или банковским счетом:  
&emsp; &emsp; ![Payment_choice](/images/markdown/03_Payment_choice_rus.png)
- **Страница с запросом подтверждения** введенных данных:  
&emsp; &emsp; ![Payment_choice](/images/markdown/04_Confirm_Purchase_CC_rus.png)
- **Страница с подтверждением совершенной покупки** или с сообщением о недостаточности средств:  

| Успешная оплата кредитной картой                                       | Успешная оплата банковским счетом                                           | Недостаточно средств на счете                                                                 |
|------------------------------------------------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| ![Purchase_complete](/images/markdown/05_Purshase_complete_CC_rus.png) | ![Purchase_complete](/images/markdown/05_Purshase_complete_Account_rus.png) | ![Purchase_complete](/images/markdown/05_Purshase_complete_Account_InsufficientFunds_rus.png) |

### Интернационализация
Выполнена для всех элементов оформления и ошибок валидации. <br>
Переключение локали производится через выпадающее меню, присутствующее на некоторых страницах приложения: <br>
&emsp; &emsp; ![Locale_choice](/images/markdown/01_Login_Locale_en.png)

## REST контроллер
Разработан для получения, создания и редактирования покупок
### Документация REST ендпоинтов
Подготовлена средствами **Springdoc** в формате **OpenAPI**, визуализация и тестирование выполнены в интерфейсе **Swagger UI**
- **Общая информация о проекте**:  
&emsp; &emsp; ![Swager_title](/images/markdown/Swagger_title.png)
- **Список REST ендпоинтов**:  
&emsp; &emsp; ![Swager_enpoints](/images/markdown/Swagger_Enpoints.png)
- **Описание ендпоинтов** на примере GET /api/v1/purchases/{purchaseId}:  
&emsp; &emsp; ![Swager_enpoints](/images/markdown/Swagger_findByid_1.png)  
&emsp; &emsp; ![Swager_enpoints](/images/markdown/Swagger_findByid_200.png)  
&emsp; &emsp; ![Swager_enpoints](/images/markdown/Swagger_findByid_401_401_500.png)  

## База данных
### Схема
Все изменения сущностей сохраняются в БД PostgeSQL со следующей схемой:  
&emsp; &emsp; ![Database_scheme](/images/markdown/Database_scheme_purchase_lead.png)
### Аудит
Реализован для всех таблиц БД
- **Аудит первого и последнего изменения** в таблице сущности:  

| Таблица purchase                                                  |
|-------------------------------------------------------------------|
| ![purchases_audit](/images/markdown/Database_purchases_audit.png) |

- **Аудит всех действий** в отдельных таблицах с постфиксом _aud и таблицей ревизий:  

| Таблица purchase_aud                                        | Таблица revision                                                 |
|-------------------------------------------------------------|------------------------------------------------------------------|
| ![purchase_aud](/images/markdown/Database_purchase_aud.png) | ![revision](/images/markdown/Database_purchase_aud_revision.png) |

### Изоляция
Для повышения степени изоляции транзакций предусмотрены оптимистические блокировки:
- с типом **VERSION** для сущностей CreditCard и Account
- с типом **ALL** для прочих сущностей

## Логирование сервисного слоя
Логируются события вызова и успешного возврата результата для public методов бинов, отмеченных аннотацией
@Service.  
Логирование реализовано по канонам АОП в декларативном стиле с использованием аннотаций @AspectJ.  

Пример декорирования сообщений:
![Console_Log](/images/markdown/Console_Log.png)

Пример текстов сообщений:
```
Public service layer method "loadUserByUsername" with parameter(s) [client1@gmail.com] of class com.evv.service.UserService@aba4a33 was invoked
Public service layer method "loadUserByUsername" with result "It was user with name - client1@gmail.com. All other fields have been erased for security reason" of class com.evv.service.UserService@aba4a33 was completed

Public service layer method "createPurchaseAccount" with parameter(s) [1400.0000, ClientReadDto(super=UserReadDto(id=1, email=client1@gmail.com, role=CLIENT), birthDate=1984-06-15, clientStatus=ACTIVE, image=, gender=null), [ProductReadDto(id=39, name=Удобрение щавель, cost=400.0000), ProductReadDto(id=45, name=Удочка зимняя, cost=1000.0000)], AccountDto.Read.Public(id=3, balance=130000.1000, clientId=1)] of class com.evv.service.PurchaseService@56a3da69 was invoked
Public service layer method "createPurchaseAccount" with result "Optional[PurchaseReadDto(id=20, client=ClientReadDto(super=UserReadDto(id=1, email=client1@gmail.com, role=CLIENT), birthDate=1984-06-15, clientStatus=ACTIVE, image=, gender=null), products=[ProductReadDto(id=45, name=Удочка зимняя, cost=1000.0000), ProductReadDto(id=39, name=Удобрение щавель, cost=400.0000)], payment=PaymentAccountReadDto(account=AccountDto.Read.Public(id=3, balance=128600.1000, clientId=1)))]" of class com.evv.service.PurchaseService@56a3da69 was completed

Public service layer method "createPaymentAccountWithBalanceReduce" with parameter(s) [PaymentAccountCreateEditDto(amount=1400.0000, account=AccountDto.Read.Public(id=3, balance=130000.1000, clientId=1))] of class com.evv.service.PaymentService@36b1b28 was invoked
Public service layer method "createPaymentAccountWithBalanceReduce" with result "Optional[PaymentAccountReadDto(account=AccountDto.Read.Public(id=3, balance=128600.1000, clientId=1))]" of class com.evv.service.PaymentService@36b1b28 was completed

Public service layer method "reduceBalance" with parameter(s) [3, 1400.0000] of class com.evv.service.AccountService@738a1324 was invoked
Public service layer method "reduceBalance" with result "Optional[AccountDto.Read.Public(id=3, balance=128600.1000, clientId=1)]" of class com.evv.service.AccountService@738a1324 was completed

Public service layer method "findAll" with parameter(s) [ProductFilter[name=null, minCost=null, maxCost=null], Page request [number: 0, size 12, sort: UNSORTED]] of class com.evv.service.ProductService@5fb0c234 was invoked
Public service layer method "findAll" with result "Page 1 of 4 containing com.evv.dto.ProductReadDto instances" of class com.evv.service.ProductService@5fb0c234 was completed
```

---
# <a id="implementation"> Реализация :triangular_ruler: :wrench: :nut_and_bolt: </a>
## Полиморфизм ассоциаций
#### Таблица payment
Согласно техническому заданию (которое мной же и разработано) таблицы account и credit_card являются 
независимыми друг от друга, не могут иметь общий счетчик id, не могут быть изменены для целей отличных
от их основного назначения.  
При этом записи таблицы payment должны однозначно ссылаться на записи таблиц
account или credit_card.

Разработано решение, полностью удовлетворяющее ТЗ, а также обеспечивающее целостность данных за счет 
связывания таблицы payment с таблицами account и credit_card с помощью промежуточных таблиц, содержащих внешние ключи.

На уровне БД решение показано на диаграмме таблиц:
![table_payment_diagramm](/images/markdown/Database_Payment_scheme.png)

На уровне приложения базовый класс Payment помечается @Inheritance со стратегией JOINED и далее 
реализуются его наследники PaymentCreditCard и PaymentAccount как показано на диаграмме классов:  
![class_payment_diagramm](/images/markdown/Classes_Payment_Schema.png)

У класса Purchase есть полиморфная ассоциация с суперклассом Payment.
```java
@Entity
public class Purchase extends AuditingEntity {
    ...
    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;
    ...
}
```

Экземпляр Purschase может ссылаться на экземпляр любого из подклассов Payment.  
Во время работы приложения Hibernate выполняет left join для полиморфного извлечения 
экземпляров Payment и всех его подклассов.

Следует отметить следующие сложности программной реализации:
- многочисленные instanceof для проверки наименования подкласса Payment, который вернулся в результате 
полиморфного извлечения из БД;
- наследование на уровне сущностей приводит к наследованию в соответствующих ReadDto. 

В качестве аналога предложенного решения, с учетом требований ТЗ, может выступать построение классов и таблиц на 
основе аннотации org.hibernate.annotations.Any. Однако разработчики Hibernate рекомендуют ее избегать, поскольку
она "может привести к появлению уродливых схем". К тому же в случае @Any отсутствует поддержка целостности данных
со стороны БД.

#### Таблица users
Наследование со стратегией JOINED применено также к сущности User:  
&emsp; &emsp; <img src="images\markdown\Classes_User_Schema.png" style="max-inline-size: 50em"> </img>  
Данное обстоятельство приводит к созданию трех таблиц БД:  
&emsp; &emsp; <img src="images\markdown\Database_User_scheme.png" style="max-inline-size: 50em"> </img>  
Однако полиморфизма ассоциаций в этом случае удалось избежать, т.к прочие сущности в проекте ссылаются не на 
базовый класс User, а на конкретный подкласс Client.  
В связи с этим программная обработка данного наследования оказывается менее рутинной.

## Разделение SecurityFilterChain
Применено для разделения настроек защиты контроллеров REST и Thymeleaf, а именно:
- Различные обработчики исключений: AccessDeniedException, AuthenticationException;
- Отключение CSRF только для REST контроллера;
- Настройка страниц login и logout только для контроллера Thymeleaf;
- Настройка различных стратегий защиты ендпоинтов:
  * Для Thymeleaf вся защита настраивается в SecurityFilterChain,
  * Для REST доступ к одному из ендпоинтов предоставляет AuthorizationManager, а к остальным MethodSecurity.

SecurityFilterChain для REST контроллера:
```java
    @Bean
    @Order(1)
    public SecurityFilterChain restFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(authRegistry -> authRegistry
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/purchases/{purchaseId:\\d+}")
                            .access(purchaseAuthorizationManager)
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(restControllerExceptionHandler)
                        .authenticationEntryPoint(restControllerExceptionHandler));
        return http.build();
    }
```

SecurityFilterChain для Thymeleaf контроллера:
```java
    @Bean
    @Order(2)
    public SecurityFilterChain thymeleafFilterChain(HttpSecurity http) throws Exception {
      http
              .authorizeHttpRequests(authRegistry -> authRegistry
                      .dispatcherTypeMatchers(FORWARD).permitAll()
                      .requestMatchers("/users/login", "/users/registration").permitAll()
                      .requestMatchers(HttpMethod.POST, "/users/clients").permitAll()
                      .requestMatchers("/purchase/**", "/users/logout").hasAuthority(CLIENT.getAuthority())
                      .requestMatchers("/users/**").hasAuthority(ADMIN.getAuthority())
                      .anyRequest().denyAll())
              .logout(logout -> logout
                      .logoutUrl("/users/logout")
                      .logoutSuccessUrl("/users/login")
                      .deleteCookies("JSESSIONID"))
              .formLogin(fLConfigurer -> fLConfigurer
                      .loginPage("/users/login")
                      .successHandler(customAuthSuccessHandler())
                      .permitAll())
              .exceptionHandling(exceptionHandling -> exceptionHandling
                      .accessDeniedHandler(controllerExceptionHandler))
              .oauth2Login(config -> config
                      .loginPage("/users/login")
                      .loginProcessingUrl("/users/login/oauth2/code/*")
                      .defaultSuccessUrl("/purchase/products", true)
                      .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
                      .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
              );
      return http.build();
}
```
## Тесты
### @Transactional и тестовые данные
В интеграционных тестах сервисов и контроллеров **не используется** @Transactional из TestContext.  

Это нужно для проверки правильности установки @Transactional в исходниках, т.к.
@Transactional может ошибочно не стоять там где должна или может стоять @Transactional(readOnly = true) там где происходят 
изменения БД.  
В случае использования тестовой @Transactional вышеуказанные отклонения себя не проявят, поскольку тесты так или иначе 
успешно завершатся в тестовой @Transactional.  

Однако теперь возникает проблема с тестовыми данными, поставляемыми с помощью @Sql - тестовые данные записываются
безтранзакционно и не откатываются после каждого теста. В связи с этим, для чистки БД и сброса счетчиков id, 
после каждого теста выполняется хранимая функция truncate_tables.  

Базовый безтранзакционный тестовый класс:

```java
@IT
@Sql(scripts = "classpath:sql/TestData.sql")
public abstract class NonTransactionalIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  public void resetDataAndSequences() {
    new SimpleJdbcCall(jdbcTemplate)
            .withFunctionName("truncate_tables")
            .execute();
  }
}
```
Базовая аннотация:
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@SpringBootTest
public @interface IT {}
```
Хранимая функция для чистки БД и сброса счетчиков id:
```sql
CREATE OR REPLACE FUNCTION truncate_tables()
    RETURNS void
    LANGUAGE plpgsql AS
$$
BEGIN
    EXECUTE ( 
        SELECT 'TRUNCATE '
                   || string_agg(format('%I.%I', schemaname, tablename), ', ')
                   || ' RESTART IDENTITY'
        FROM pg_catalog.pg_tables
        WHERE schemaname = 'public'
    );
END
$$;
```

В интеграционных тестах репозиториев и мапперов **используется** @Transactional из TestContext.  

Базовый транзакционный тестовый класс:

```java
@IT
@Sql(scripts = "classpath:sql/TestData.sql")
@Transactional
public abstract class TransactionalIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterTransaction
  protected void resetDataAndSequences() {
    new SimpleJdbcCall(jdbcTemplate)
            .withFunctionName("truncate_tables")
            .execute();
  }
}
```
Здесь хранимая функция truncate_tables используется только для сброса счетчиков id, т.к. данные, 
поступившие посредством @Sql откатываются после каждого теста благодаря TestContext.

Во всех случаях схема тестовой БД берется из changelog в исходниках и накатывается Liquibase в начале поднятия каждого 
ApplicationContext.  
Для удаления таблиц, оставшихся от предыдущего ApplicationContext необходим дополнительный скрипт БД:
```sql
DO
$$
  BEGIN
    EXECUTE ( 
      SELECT COALESCE (
                     (
                       SELECT 'DROP TABLE IF EXISTS '
                                || string_agg(format('%I.%I', schemaname, tablename), ', ')
                       FROM pg_catalog.pg_tables
                       WHERE schemaname = 'public'
                         AND tablename NOT IN ('databasechangelog', 'databasechangeloglock')
                     ),
                     'SELECT 0'
             )
    );
  END
$$;
```
### Покрытие тестами
Разработаны 110 тестовых методов плюс 31 параметризованный метод с различными значениями параметров 
(@ParameterizedTest + @ValueSource):  
&emsp; &emsp; <img src="images\markdown\Tests_141count.png" style="max-inline-size: 600px"> </img>  
Порядок тестов настроен на первоочередное выполнение Unit тестов с помощью класса-наследника 
org.junit.jupiter.api.ClassOrderer - SpringBootTestClassOrderer.

Отчет о покрытии кода тестами, сделанный с помощью плагина JaCoCo:
![Tests_141count](/images/markdown/Tests_Jacoco.png)

# <a id="other"> Прочее :mag_right: </a>
В проекте также разработаны следующие элементы:
- Кастомные аннотации для валидации: @Adult, @PaymentIdPresent и их валидаторы;
- Кастомный UserDetails - CustomUserDetailsImpl, содержащий кроме стандартного username дополнительные поля типа ClientReadDto или AdminReadDto;
- Кастомные реализации AccessDeniedHandler, AuthenticationEntryPoint, AuthenticationSuccessHandler.