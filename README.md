# Task Manager

[![hexlet-check](https://github.com/voronovyyyyy/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/voronovyyyyy/java-project-99/actions/workflows/hexlet-check.yml)
[![Java CI](https://github.com/voronovyyyyy/java-project-99/actions/workflows/ci.yml/badge.svg)](https://github.com/voronovyyyyy/java-project-99/actions/workflows/ci.yml)

[Открыть приложение](https://task-manager-java-99.onrender.com)
· [Swagger API](https://task-manager-java-99.onrender.com/swagger-ui/index.html)
· [Ошибки в Bugsink](https://voronovtasks.bugsink.com/issues/1/)

Менеджер задач на Spring Boot 3 с REST API, JWT-аутентификацией и интерфейсом React Admin.
Позволяет управлять пользователями, задачами, статусами и метками, назначать исполнителей
и фильтровать задачи. Данные хранятся через Spring Data JPA в H2 или PostgreSQL.

## Запуск

Требуются JDK 21, Node.js 22+ и Make. Gradle 9.7.0 загружается через Wrapper.
Для запуска Gradle укажите `JAVA_HOME`, соответствующий JDK 21.

```sh
git clone https://github.com/voronovyyyyy/java-project-99.git
cd java-project-99
make setup
make start
```

Приложение откроется по адресу [localhost:8080](http://localhost:8080).
Начальный пользователь: **hexlet@example.com**, пароль **qwerty**.
Он может создавать пользователей; редактирование и удаление аккаунта разрешено
только владельцу этого аккаунта.

`make setup` устанавливает npm-зависимости, запускает официальный сборщик
`@hexlet/java-task-manager-frontend`, создаёт `build/libs/app.jar` и дистрибутив
`build/install/app`. Исходники находятся в корне, без вложенного каталога `app`.

```sh
make dev       # запуск через bootRun
make test      # интеграционные тесты и отчёт JaCoCo
make lint      # проверка форматирования Spotless
make lint-fix  # форматирование
./gradlew check
```

На Windows используйте `gradlew.bat` вместо `./gradlew`.
Без Make подготовка выполняется командами:

```sh
npm ci
npm run build
./gradlew bootJar installDist
java -jar build/libs/app.jar
```

## API

Интерактивная документация доступна по адресу
[/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html),
спецификация OpenAPI — [/v3/api-docs](http://localhost:8080/v3/api-docs).

| Метод | Путь | Действие |
| --- | --- | --- |
| GET | `/welcome` | `Welcome to Spring` |
| POST | `/api/login` | Вход и получение JWT |
| GET, POST | `/api/users` | Список и создание пользователей |
| GET, PUT, DELETE | `/api/users/{id}` | Просмотр, частичное обновление, удаление |
| GET, POST | `/api/task_statuses` | Список и создание статусов |
| GET, PUT, DELETE | `/api/task_statuses/{id}` | Просмотр, частичное обновление, удаление |
| GET, POST | `/api/tasks` | Список и создание задач |
| GET, PUT, DELETE | `/api/tasks/{id}` | Просмотр, частичное обновление, удаление |
| GET, POST | `/api/labels` | Список и создание меток |
| GET, PUT, DELETE | `/api/labels/{id}` | Просмотр, частичное обновление, удаление |

Вход:

```http
POST /api/login
Content-Type: application/json

{"username":"hexlet@example.com","password":"qwerty"}
```

Ответ — строка JWT. Для всех остальных маршрутов `/api/**` нужен заголовок
`Authorization: Bearer <token>`. JWT действует сутки, пароли хешируются BCrypt
и не возвращаются в ответах. JWT-ключ в production задаётся переменной окружения.

Пример создания задачи:

```json
{
  "title": "Create API",
  "content": "Implement task endpoints",
  "status": "draft",
  "index": 12,
  "assignee_id": 1,
  "taskLabelIds": [1, 2]
}
```

В PUT отправляются только изменяемые поля. Пропущенные поля сохраняются,
явный `null` очищает необязательное поле. Пустой массив `taskLabelIds` удаляет
все метки задачи. Пользователи, статусы и метки, связанные с задачами, не удаляются.

Создание возвращает `201`, успешное удаление — `204`, ошибки данных — `400`,
отсутствие аутентификации — `401`, запрещённая операция — `403`,
несуществующий ресурс — `404`.

### Фильтрация и списки

```text
/api/tasks?titleCont=create&assigneeId=1&status=draft&labelId=1
```

Фильтры объединяются условием AND. Поиск по названию не зависит от регистра;
`%` и `_` считаются обычными символами. Без фильтров возвращаются все задачи.

Для React Admin поддерживаются `_sort`, `_order`, `_start`, `_end`, выборка по
повторяющемуся `id` и заголовок `X-Total-Count`. Сортировка разрешена только по
известным полям соответствующего ресурса.

При старте создаются пользователь-администратор, статусы `draft`, `to_review`,
`to_be_fixed`, `to_publish`, `published` и метки `feature`, `bug`.
Повторный запуск не дублирует начальные данные.

## Конфигурация и базы данных

По умолчанию используется профиль `development` с H2 в памяти.
Консоль H2 доступна по `/h2-console`, JDBC URL — `jdbc:h2:mem:project`,
пользователь `sa`, пароль пустой. Данные H2 существуют до остановки приложения.

В production используется PostgreSQL. Имена столбцов и внешние ключи создаёт
Hibernate, схема обновляется через `ddl-auto: update`. Связь задач со статусом
и исполнителем — many-to-one, с метками — many-to-many.

| Переменная | Назначение |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` для PostgreSQL |
| `PORT` | Порт HTTP, по умолчанию `8080` |
| `JDBC_DATABASE_URL` | JDBC URL или внутренний PostgreSQL URL Render |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Доступ к БД для JDBC URL без встроенных данных |
| `DATABASE_SCHEMA` | Схема production, по умолчанию `task_manager` |
| `JWT_SECRET` | Секрет подписи JWT, не менее 32 байт |
| `SENTRY_AUTH_TOKEN` | DSN коллектора из задания |
| `SENTRY_DSN` | Альтернативное стандартное имя DSN, имеет приоритет |

В переменную `SENTRY_AUTH_TOKEN` передаётся именно DSN, выдаваемый Bugsink для
подключения SDK. Обычный API-токен Sentry/Bugsink не заменяет DSN.
Секреты задаются в окружении; `.env.example` содержит только пример имён.
Приложение не загружает `.env` автоматически.

## Деплой и сбор ошибок

Для Render подготовлены Dockerfile и `render.yaml`. Docker отдельно собирает
фронтенд и Java-приложение, затем запускает JAR под непривилегированным пользователем.
Проверка готовности — `/actuator/health`. Автодеплой ждёт успешных проверок CI.

```text
https://dashboard.render.com/blueprint/new?repo=https://github.com/voronovyyyyy/java-project-99
```

Blueprint использует существующую базу `page-analyzer-java-72-db` и отдельную
схему `task_manager`. Это позволяет разместить оба учебных приложения в пределах
ограничения одной бесплатной PostgreSQL на рабочее пространство Render.
Бесплатная база действует до **24 ноября 2026 года**; для дальнейшей работы
потребуется продление/смена тарифа или перенос данных в другую PostgreSQL.

Bugsink подключён через Sentry SDK для Spring Boot. Непредвиденные ошибки
обрабатываются общим обработчиком, отправляются в коллектор, а клиент получает
общий ответ `500` без внутренних подробностей. Содержимое запросов и персональные
данные не отправляются; сбор трассировок отключён.

Для контролируемой проверки доставки можно на один запуск передать
`--app.monitoring-check=true`. Он отправляет диагностическое исключение через
тот же обработчик ошибок. По умолчанию эта проверка выключена.

## Тесты и CI

Интеграционные тесты используют MockMvc и настоящую JPA-базу. Проверяются CRUD,
валидация, уникальность, права владельца, JWT, связи, фильтрация и контракт фронтенда.
Каждый тест работает с изолированным состоянием данных.

В CI один и тот же набор запускается с H2 и с отдельным PostgreSQL 17.
Тесты не используют production-базу и не отправляют события в Bugsink.
Отчёты тестов и JaCoCo сохраняются в артефактах GitHub Actions.
Команда `./gradlew check` также требует не менее **80% покрытия инструкций**.

Для локальной проверки с отдельной PostgreSQL задайте `TEST_DATABASE_URL`,
`TEST_DATABASE_USERNAME`, `TEST_DATABASE_PASSWORD` и выполните
`./gradlew test --rerun-tasks`. Эти переменные предназначены только для тестовой БД.

Код разделён на контроллеры, DTO, сервисы, маппер, JPA-модели, репозитории,
спецификации фильтров и конфигурацию безопасности.

<details>
<summary>Автоматические тесты Хекслета</summary>

Тесты запускаются на каждый коммит. За запуск отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте и не переименовывайте ни его, ни репозиторий.

</details>

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
