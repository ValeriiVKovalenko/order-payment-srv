# PROJECT CONTEXT — order-payment-srv

**Дата сверки:** 2026-11-27  
**Репозиторий:** https://github.com/ValeriiVKovalenko/order-payment-srv  
**Ветка для проверки фактической реализации:** `develop`  
**Состояние:** после TASK-18

> Документ фиксирует состояние, подтверждённое по коду и конфигурации `develop` на дату сверки. При последующей работе сначала проверяй актуальный код. Наличие merged PR или тестовых классов не означает, что тесты повторно запускались в рамках аудита.

## 1. Назначение проекта

`order-payment-srv` — учебный backend-проект, состоящий из двух отдельных Spring Boot приложений:

- `order-srv` — создание и управление заказами;
- `payment-srv` — создание, хранение и получение платежей.

Каждый сервис использует собственную базу данных. Проект предназначен для практики Java Backend разработки: REST API, бизнес-логика, PostgreSQL, JDBC, транзакции, тестирование и инженерные практики.

## 2. Технологический стек

| Область | Технология |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Build | Gradle Wrapper 9.5.1, multi-module |
| Database | PostgreSQL 16 |
| Data access | Spring JDBC |
| SQL templates | `JdbcTemplate`, `NamedParameterJdbcTemplate` |
| Migrations | Flyway |
| Web | Spring MVC |
| Validation | Jakarta Bean Validation |
| Mapping | MapStruct 1.6.3, где используется |
| Boilerplate | Lombok |
| Tests | JUnit 5, Spring Boot Test, MockMvc, Mockito, Testcontainers |
| Infrastructure | Docker Compose |
| CI | GitHub Actions |

Корневая Gradle group: `com.valerii`.

Основные пакеты:

- `com.valerii.order`
- `com.valerii.payment`

Доступ к данным реализован через явный SQL с Spring JDBC. JPA/Hibernate и Spring Data repositories не используются; их введение требует отдельного согласования.

Денежные значения представляются через `BigDecimal`, не `double` и не `float`.

## 3. Модули и локальное окружение

Корневой Gradle build включает два модуля:

- `order-srv`;
- `payment-srv`.

Локальная инфраструктура описана в `docker-compose.yml`:

- PostgreSQL 16;
- контейнер `order-payment-postgres`;
- опубликованный порт `5432`;
- named volume `postgres_data`;
- init script `docker/postgres/init/01-create-databases.sql`.

Init script создаёт две базы данных:

- `order_db`;
- `payment_db`.

| Сервис | Порт | База данных |
|---|---:|---|
| `order-srv` | 8081 | `order_db` |
| `payment-srv` | 8082 | `payment_db` |

Локальная конфигурация использует development credentials `postgres/postgres`. Это не production-конфигурация. Реальные секреты нельзя добавлять в Git.

Оба сервиса используют Flyway через `application.yml`.

## 4. Архитектура `order-srv`

Сервис использует слоистое разделение ответственности:

- `controller` — REST endpoints;
- `service` — use cases, бизнес-правила и транзакционные границы;
- `repository` — SQL и доступ к PostgreSQL через Spring JDBC;
- `model` — доменные объекты и команды;
- `model/entity` — persistence-модели;
- `model/request`, `model/response` — API DTO;
- `mapper` — преобразование между DTO, domain и entity;
- `exception` / `exceptionhandler` — доменные ошибки и HTTP error mapping;
- `resources/db/migration` — Flyway migrations.

Для чтения заказа вместе с позициями используется `LEFT JOIN` и `OrderResultSetExtractor`, который собирает агрегат заказа с коллекцией позиций.

### 4.1. Модель заказа и статусы

Заказ содержит идентификатор клиента, статус, валюту, общую сумму, временные поля и позиции заказа. Позиция содержит название продукта, количество, цену единицы и сумму позиции.

Создание заказа выполняется через `OrderFactory`: суммы позиций и заказа рассчитываются с использованием `BigDecimal`. Новый заказ получает статус `CREATED`.

Поддерживаемые статусы:

- `CREATED`;
- `PROCESSING`;
- `COMPLETED`;
- `CANCELLED`.

Допустимые переходы:

- `CREATED → PROCESSING`;
- `CREATED → CANCELLED`;
- `PROCESSING → COMPLETED`;
- `PROCESSING → CANCELLED`.

`COMPLETED` и `CANCELLED` — терминальные состояния. Повторная установка текущего статуса идемпотентна: обновление и новая запись истории не выполняются.

При изменении статуса сервис открывает транзакцию, а repository читает статус с блокировкой строки через `SELECT ... FOR UPDATE`. Обновление заказа и запись истории выполняются в одной транзакции.

### 4.2. История статусов

Таблица `order_status_history` хранит реальные изменения статуса:

- `id`;
- `order_id`;
- `old_status`;
- `new_status`;
- `changed_at`.

История сохраняется в той же транзакции, что и обновление статуса. При ошибке записи истории транзакция должна откатить изменение заказа.

Первоначальное создание заказа не создаёт запись `NULL → CREATED`: сохраняются только реальные переходы между статусами.

История возвращается в обратном хронологическом порядке: `changed_at DESC, id DESC`.

## 5. База данных `order_db`

Flyway migrations находятся в `order-srv/src/main/resources/db/migration`:

- `V1__init_order_schema.sql` — начальная таблица `health_check`;
- `V2__order_orderitem.sql` — таблицы `orders` и `order_items`, связь позиций с заказом и индексы;
- `V3__order_created_id_idx.sql` — индекс `orders(created_at DESC, id DESC)`;
- `V4__order_status_history_schema.sql` — таблица истории статусов, FK на `orders(id)` и индекс по `order_id`.

Основные поля таблицы `orders`:

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`;
- `customer_id BIGINT NOT NULL`;
- `status VARCHAR(20) NOT NULL`;
- `total_amount NUMERIC(15,2) NOT NULL`;
- `currency CHAR(3) NOT NULL`;
- `created_at TIMESTAMPTZ NOT NULL`;
- `updated_at TIMESTAMPTZ NOT NULL`.

Основные поля таблицы `order_items`:

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`;
- `order_id BIGINT NOT NULL` — FK на `orders(id)`;
- `product_name VARCHAR(50) NOT NULL`;
- `quantity INTEGER NOT NULL`;
- `unit_price NUMERIC(15,2) NOT NULL`;
- `line_total NUMERIC(15,2) NOT NULL`.

Основные поля таблицы `order_status_history`:

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`;
- `order_id BIGINT NOT NULL` — FK на `orders(id)`;
- `old_status VARCHAR(20) NOT NULL`;
- `new_status VARCHAR(20) NOT NULL`;
- `changed_at TIMESTAMPTZ NOT NULL`.

Не изменять уже применённые Flyway migrations. Изменения схемы оформлять новыми миграциями.

## 6. REST API `order-srv`

Базовый путь: `/api/v1/orders`.

| Method | Endpoint | Назначение |
|---|---|---|
| GET | `/api/v1/orders/{id}` | Получить заказ с позициями |
| GET | `/api/v1/orders` | Получить страницу заказов |
| POST | `/api/v1/orders` | Создать заказ |
| PATCH | `/api/v1/orders/{id}/status` | Изменить статус заказа |
| GET | `/api/v1/orders/{id}/status-history` | Получить историю статусов |

### 6.1. Список заказов

Параметры:

- `page` — номер страницы, по умолчанию `0`, минимальное значение `0`;
- `size` — размер страницы, по умолчанию `20`, диапазон от `1` до `100`;
- `customerId` — необязательный положительный фильтр по клиенту;
- `status` — необязательный фильтр по статусу заказа.

Сортировка: `created_at DESC, id DESC`.

Ответ содержит `size`, `page`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious` и `content`.

### 6.2. Создание заказа

`POST /api/v1/orders` принимает клиента, валюту и непустой список позиций. Успешное создание возвращает `201 Created`, заголовок `Location` с URI созданного заказа и представление заказа в теле ответа.

### 6.3. Изменение статуса

`PATCH /api/v1/orders/{id}/status` принимает новый статус. При успешном изменении возвращает `204 No Content`.

### 6.4. История статусов

`GET /api/v1/orders/{id}/status-history` возвращает список переходов с `fromStatus`, `toStatus`, `changedAt`.

- существующий заказ без истории — `200 OK` с пустым массивом;
- несуществующий заказ — `404 Not Found`;
- некорректный ID — `400 Bad Request`.

### 6.5. HTTP error mapping

По текущим обработчикам:

- неизвестный заказ — `404`;
- некорректный запрос, параметры или статус — `400`;
- запрещённый переход статуса — `409`;
- недопустимая сумма заказа — `422`;
- нарушение доменного инварианта — `500`.

При изменении exception handlers сверять фактические HTTP-коды с кодом и тестами.

## 7. Архитектура `payment-srv`

`payment-srv` — отдельное Spring Boot приложение с собственной базой `payment_db`. После TASK-14 — TASK-18 реализованы persistence и REST API создания и чтения платежей.

Основные компоненты:

- `controller/PaymentController` — REST endpoints;
- `service/PaymentService`, `PaymentServiceImpl` — use cases создания и получения платежей;
- `repository/PaymentRepository`, `PaymentRepositoryImpl` — persistence через `NamedParameterJdbcTemplate`;
- `model/domain/Payment` — доменная модель;
- `model/entity/PaymentEntity` — persistence-модель;
- `model/enums/PaymentStatus` — статусы платежа;
- `model/command/CreatePaymentCommand` — команда создания;
- `model/request`, `model/response` — REST DTO;
- `mapper` — преобразование request DTO, command, domain и response DTO;
- `exception` / `exceptionhandler` — ошибки и HTTP error mapping;
- `resources/db/migration` — Flyway migrations.

### 7.1. Модель платежа

`Payment` содержит:

- `id`;
- `orderId`;
- `amount`;
- `currency`;
- `status`;
- `createdAt`;
- `updatedAt`.

Статусы `PaymentStatus`:

- `PENDING`;
- `SUCCEEDED`;
- `FAILED`.

Фабричный метод `Payment.create(...)` создаёт платёж со статусом `PENDING`.

`PaymentEntity` представляет persistence-запись; поле статуса в entity хранится как строка. Преобразование между entity и domain выполняет `PaymentDomainMapper`.

### 7.2. Persistence

`PaymentRepository` предоставляет операции:

- `save(Payment payment)` — сохраняет платёж и возвращает `PaymentEntity`;
- `findById(Long id)` — возвращает `Optional<PaymentEntity>`;
- `findAllByOrderId(Long orderId)` — возвращает список платежей заказа.

Реализация использует `NamedParameterJdbcTemplate` и PostgreSQL SQL. Для вставки используется `INSERT ... RETURNING`, чтобы получить запись с generated ID и временными полями.

Список платежей сортируется по `created_at DESC, id DESC`.

### 7.3. REST API `payment-srv`

Базовый путь: `/api/v1/payments`.

| Method | Endpoint | Назначение |
|---|---|---|
| POST | `/api/v1/payments` | Создать платёж |
| GET | `/api/v1/payments/{id}` | Получить платёж по ID |
| GET | `/api/v1/payments?orderId={orderId}` | Получить платежи по ID заказа |

**Создание платежа**

`POST /api/v1/payments` принимает:

- `orderId` — обязательный положительный `Long`;
- `amount` — обязательный положительный `BigDecimal`, до 13 целых и 2 дробных знаков;
- `currency` — обязательная строка из трёх заглавных букв.

Успешное создание возвращает `201 Created`, заголовок `Location` с URI созданного платежа и `PaymentResponseDto` в теле. Новый платёж получает статус `PENDING`.

**Получение платежа по ID**

`GET /api/v1/payments/{id}` принимает положительный ID. Успех возвращает `200 OK` с `PaymentResponseDto`; отсутствие платежа обрабатывается как `404 Not Found`. Некорректный ID возвращает `400 Bad Request`.

**Получение платежей по Order ID**

`GET /api/v1/payments?orderId={orderId}` требует положительный `orderId`. Возвращает `200 OK` со списком платежей. Если платежей для заказа нет, возвращается пустой список. Некорректный или отсутствующий параметр обрабатывается как `400 Bad Request`.

Поля `PaymentResponseDto`: `id`, `orderId`, `amount`, `currency`, `status`, `createdAt`, `updatedAt`.

На состоянии после TASK-18 не описаны endpoint изменения статуса платежа или отдельный payment workflow. Не считать их реализованными.

### 7.4. HTTP error mapping

- некорректные параметры и request validation — `400 Bad Request`;
- платёж не найден — `404 Not Found`.

Ответ ошибки формируется через `ApiErrorResponseDto` с полями `timestamp`, `status`, `error`, `message`, `path`.

## 8. База данных `payment_db`

Flyway migrations находятся в `payment-srv/src/main/resources/db/migration`:

- `V1__init_payment_schema.sql` — начальная таблица `health_check`;
- `V2__payments_schema.sql` — таблица `payments` и индекс по `order_id`.

Таблица `payments` содержит:

- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`;
- `order_id BIGINT NOT NULL`;
- `amount NUMERIC(15,2) NOT NULL`;
- `currency CHAR(3) NOT NULL`;
- `status VARCHAR(20) NOT NULL`;
- `created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()`;
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()`.

Для `order_id` создан индекс `payments_order_id_index`.

В таблице `payments` нет FK на `orders`: сервисы используют разные базы данных. Наличие `orderId` в платеже само по себе не означает, что `payment-srv` проверяет существование заказа через базу `order_db`.

Не изменять уже применённые Flyway migrations. Изменения схемы оформлять новыми миграциями.

## 9. Тестирование

В проекте используются JUnit 5, Spring Boot Test, MockMvc, Mockito и Testcontainers с PostgreSQL 16.

Для `order-srv` тестами представлены controller, domain model, factory, mapper, service, repository и integration scenarios. В частности, существуют тесты REST API, валидации, переходов статусов, истории, concurrency и интеграции с PostgreSQL.

Для `payment-srv` существуют:

- `FlywayMigrationTest` — проверка Flyway migrations;
- `PaymentIntegrationRepositoryTest` — integration tests repository;
- `PaymentControllerTest` — REST endpoints и validation;
- `PaymentServiceTest` — service layer;
- `PaymentIntegrationTest` — интеграционные сценарии;
- `PaymentTestData` — тестовые данные;
- `TestcontainersConfiguration` — конфигурация PostgreSQL Testcontainers.

Наличие тестов в репозитории не доказывает их успешный запуск в рамках текущей сверки. Не утверждать, что `./gradlew test` или `./gradlew clean build` прошли, если запуск не был выполнен или пользователь не предоставил результат.

## 10. CI

В `.github/workflows/ci.yml` настроен GitHub Actions workflow:

- запуск при push в ветки `TASK-*` и `develop`;
- запуск при pull request в `develop`;
- runner `ubuntu-latest`;
- Temurin JDK 21;
- Gradle cache;
- запуск `./gradlew clean build`;
- Docker доступен для Testcontainers.

Документ описывает конфигурацию workflow, а не результат конкретного CI run.

## 11. Архитектурные ограничения и принципы

- `order-srv` и `payment-srv` — отдельные Spring Boot приложения.
- У каждого сервиса собственная база данных.
- Не обращаться напрямую к таблицам другой базы данных как к способу интеграции сервисов.
- Использовать Spring JDBC и явный SQL.
- Не вводить JPA/Hibernate или Spring Data без отдельного согласования.
- Денежные значения представлять через `BigDecimal`.
- Изменения схемы БД оформлять Flyway migrations.
- Не изменять уже применённые миграции.
- Бизнес-правила размещать в service/domain слоях, а не в controller.
- Транзакционные границы должны соответствовать бизнес-операциям.
- Не добавлять архитектурные паттерны и инфраструктурные технологии без обоснованной необходимости.
- Не изменять согласованные бизнес-правила, API contracts, архитектурные границы и модель данных без обсуждения.

## 12. Git workflow и источники истины

Репозиторий: `ValeriiVKovalenko/order-payment-srv`.

- `develop` — актуальная рабочая ветка проекта.
- `master` — устаревшая bootstrap-ветка; не использовать её как источник актуального состояния проекта.
- Рабочий процесс: feature branch `TASK-n` → Pull Request в `develop` → Code Review → merge.
- Изменения исходного кода и относящейся к ним документации должны проходить в одном MR, если задача требует обновления документации.

При анализе проекта:

1. Сначала проверять актуальный commit ветки `develop`.
2. Фактический код, конфигурацию и Flyway migrations использовать как источник состояния реализации.
3. `docs/PROJECT_CONTEXT.md` описывает подтверждённую архитектуру и технические ограничения.
4. `docs/ROADMAP.md` описывает историю задач и согласованное направление дальнейшей работы.
5. Последние явно согласованные пользователем решения являются источником требований, ещё не отражённых в коде.
6. При расхождении кода, документации и согласованных требований не выбирать вариант самостоятельно: описать расхождение и запросить уточнение.
7. Наличие merged PR не означает, что тесты повторно запускались в рамках аудита.

История выполненных задач и правила её ведения находятся в [ROADMAP.md](./ROADMAP.md).
