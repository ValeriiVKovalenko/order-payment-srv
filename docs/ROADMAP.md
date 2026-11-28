# ROADMAP — order-payment-srv

**Дата сверки:** 2026-11-27  
**Источник фактического состояния:** ветка `develop` и история Pull Requests репозитория.

> Для задач, выполнявшихся в рамках согласованного PR workflow, статус `Done` означает, что соответствующий PR влит в `develop`. Исторические исключения для TASK-0 и TASK-1 указаны отдельно в таблице и правилах ниже. Merge не является подтверждением успешного запуска тестов в рамках последующего аудита.

## История задач

| Task | Название / содержание | Статус | Подтверждение |
|---|---|---|---|
| TASK-0 | Initialize order-payment microservices project | Done | Initial commit |
| TASK-1 | Configure postgres infrastructure | Done | Историческое исключение: прямой commit в `develop` — `dd45a1112f354c1225a2b45372e717cfdbb7845e` от 2026-07-11 |
| TASK-2 | Design the order domain schema | Done | PR #2 |
| TASK-3 | Implement Order domain model | Done | PR #3 |
| TASK-4 | Implement Order Repository using JDBC | Done | PR #4 |
| TASK-5 | Implement validation for Order | Done | PR #5 |
| TASK-6 | Testing the saving and reading of an Order aggregate | Done | PR #6 |
| TASK-7 | Receiving an order at the service level | Done | PR #7 |
| TASK-8 | Expose Order retrieval through REST API | Done | PR #8 |
| TASK-9 | Create an order via REST API | Done | PR #9, merged 2026-11-05 |
| TASK-10 | Retrieve list of orders with pagination | Done | PR #10, merged 2026-11-07 |
| TASK-11 | Add filtering for the list of orders | Done | PR #11, merged 2026-11-09 |
| TASK-12 | Order status transitions | Done | PR #12, merged 2026-11-10 |
| TASK-13 | Order status history | Done | PR #13 |
| TASK-14 | Payment persistence | Done | PR #14, merged 2026-11-12 |
| TASK-15 | Create a payment via REST API | Done | PR #15, merged 2026-11-22 |
| TASK-16 | Add GitHub Actions CI for Gradle build | Done | PR #16, merged 2026-11-24 |
| TASK-17 | Retrieve a payment by ID via REST API | Done | PR #17, merged 2026-11-25 |
| TASK-18 | Retrieve payments by Order ID via REST API | Done | PR #18, merged 2026-11-26 |
| TASK-19 | Organize Project Documentation| Done | PR #19, merged 2026-11-27 |
| TASK-20 | Update Payment Status via REST API| Done | PR #19, merged 2026-11-28 |

### Историческое исключение: TASK-1

TASK-1 была выполнена до перехода проекта на текущий PR workflow отдельным прямым commit в ветку `develop`:

- Commit: `dd45a1112f354c1225a2b45372e717cfdbb7845e`;
- Message: `TASK-1: configure postgres infrastructure`;
- Date: 2026-07-11.

PR #1 не был влит в `develop` и не является подтверждением завершения TASK-1. Статус `Done` для TASK-1 сохраняется как явно зафиксированное историческое исключение, а не как изменение общего критерия завершения задач.

## Текущее состояние

После TASK-20 в `develop` присутствуют два Spring Boot сервиса:

- `order-srv` — API создания и управления заказами, включая получение списка, фильтрацию, пагинацию, переходы статусов и историю статусов;
- `payment-srv` — persistence платежей и REST API создания и чтения платежей по ID платежа и ID заказа.

Также настроен GitHub Actions CI workflow, выполняющий `./gradlew clean build` для соответствующих push и pull request событий.

Следующие задачи и их Task ID не назначены этим документом. Они должны быть отдельно обсуждены и согласованы.

## Правила ведения Roadmap

- Для задач, выполняемых в рамках текущего workflow, задача считается завершённой и добавляется в историю после merge соответствующего PR в `develop`.
- TASK-0 (initial commit) и TASK-1 (прямой commit `dd45a1112f354c1225a2b45372e717cfdbb7845e` в `develop` от 2026-07-11) являются историческими исключениями, относящимися к раннему этапу проекта до использования текущего PR workflow. Эти исключения не изменяют общий критерий завершения последующих задач.
- Наличие merged PR не означает, что тесты были повторно запущены в рамках аудита или что конкретный локальный запуск `./gradlew clean build` успешен.
- Не утверждать, что тесты прошли, если запуск не подтверждён результатом CI или явно предоставленным результатом.
- Не назначать новые Task ID без обсуждения с пользователем.
- Не фиксировать архитектурные решения как согласованные до явного обсуждения.
- Для описания текущей архитектуры и фактического состояния кода использовать [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md).
- Этот файл предназначен для истории задач и согласованного направления дальнейшей работы, а не для дублирования технической документации.
- При расхождении истории PR, фактического кода и ранее согласованных требований указывать расхождение и запрашивать уточнение; не менять статус или согласованное решение самостоятельно.
