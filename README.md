
# Система автоматического генерирования программного кода

**Стек:** Java 21 (Spring Boot 3), PostgreSQL, JavaScript (Vanilla), HTML/CSS, Groq API (LLM).

---

## CodeGen AI — это веб-приложение, предназначенное для автоматизации написания программного кода. Система принимает требования на естественном языке, анализирует их, подбирает подходящие шаблоны и генерирует готовый код с использованием LLM (Large Language Model). Также реализованы функции валидации кода, управления проектами и библиотека шаблонов.

---
##  Демонстрация системы
#### Редактирование профиля и запись пользователя в базу данных:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 54 31" src="https://github.com/user-attachments/assets/4d79d17c-3909-41c1-bc33-a681caf98292" />

#### Поиск шаблонов кода и скачивание их в нужном для IDE формате:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 52 40" src="https://github.com/user-attachments/assets/94ea81d1-3685-48ea-9210-f48cd53e2361" />

#### Учет проектов юзера и их отслеживание:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 54 20" src="https://github.com/user-attachments/assets/3a55540a-e598-4a82-979f-d866351e64a4" />

#### Главная страинца:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 42 00" src="https://github.com/user-attachments/assets/74efdffd-ce93-4a68-ac8f-25cc5bfe4e28" />

#### Возможность выбрать язык программирования и фреймворк для генерации. Тут же и отправка запроса:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 42 55" src="https://github.com/user-attachments/assets/d591c601-4a21-4cff-877d-49a843ea7979" />

#### После отправки запроса отображается ожидание ответа и кол-во попыток "достучаться" до LLM:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 43 44" src="https://github.com/user-attachments/assets/4f94d729-2ab6-4289-9177-fad60f216f7e" />

#### Ответ LLM - полностью рабочий и правильный код на Java:
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 43 01" src="https://github.com/user-attachments/assets/406556db-a912-4577-8c57-4d9d9254a377" />
<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 43 07" src="https://github.com/user-attachments/assets/559eeb00-88ed-4de3-8326-484f12393963" />

####  Можно выбирать другие языки, например тот же запрос только для TypeScript и React:

<img width="1728" height="999" alt="Снимок экрана 2025-12-16 в 20 52 25" src="https://github.com/user-attachments/assets/e3029ee6-a7d0-4e43-ba3a-43f8ad65f870" />

---
##  Соответствие реализации объектной модели
Реализация системы выполнена в строгом соответствии с объектной моделью, разработанной на этапе проектирования (см. Диаграмму классов в ЛР 4 ).

### Слой Модели (Entity)
Классы модели данных полностью соответствуют ER-диаграмме и диаграмме классов.

* **`User`**: Реализована сущность с полями `username`, `email`, `role`, а также добавлены расширенные поля профиля (`company`, `experienceYears`), что соответствует требованиям ЛР 5.


* **`Requirement`**: Сущность связывает пользователя, проект и сгенерированный код. Поле `structuredModel` хранится в формате JSON, что позволяет гибко сохранять результат NLP-анализа.


* **`CodeTemplate`**: Реализована сущность для хранения паттернов и шаблонов. Добавлено поле `usageCount` для статистики.


* **`GeneratedCode`**: Хранит исходный код и статус валидации.

---

## Примеры кода и архитектурные решения
### Асинхронная обработка генерации
**Проблема:** Генерация кода нейросетью занимает время (5-15 секунд). Синхронный запрос заблокировал бы поток сервера и заставил пользователя ждать "зависшую" страницу.
**Решение:** Использование `CompletableFuture` и событийной модели обновления статуса.

**Пример (`CodeGenerationController.java`):**

```java
@PostMapping("/generate")
public ResponseEntity<?> generateCode(@Valid @RequestBody RequirementCreateDTO dto, ...) {
    // 1. Создаем запись со статусом PENDING
    Requirement requirement = createRequirement(dto, user);

    // 2. Запускаем процесс в отдельном потоке
    processRequirementAsync(requirement);

    // 3. Мгновенно возвращаем ID требования клиенту
    return ResponseEntity.ok(mapToDTO(requirement));
}

private void processRequirementAsync(Requirement requirement) {
    CompletableFuture.runAsync(() -> {
        try {
            // Этапы Pipeline: Анализ -> Генерация -> Валидация
            Requirement analyzed = analysisService.analyzeRequirement(requirement);
            List<GeneratedCode> codes = generationService.generateCode(analyzed);
            validationService.validateRequirementCodes(analyzed);
        } catch (Exception e) {
            handleError(requirement, e);
        }
    });
}

```

*На фронтенде реализован механизм **Polling** (опрос сервера каждые 2 секунды), чтобы проверить изменение статуса требования.*

### Управление транзакциями при Lazy Loading
**Проблема:** При асинхронном выполнении теряется контекст транзакции Hibernate. Попытка обратиться к связанным сущностям (`generatedCode.getRequirement()`) вызывала `LazyInitializationException`.
**Решение:** Явное управление транзакциями через аннотацию `@Transactional` в сервисе валидации.

**Пример (`CodeValidationService.java`):**

```java
@Service
public class CodeValidationService {
    
    // Аннотация гарантирует создание транзакции и сессии Hibernate
    // даже при вызове из CompletableFuture
    @Transactional 
    public ValidationReport validateCode(GeneratedCode generatedCode) {
        // Безопасное получение связанных данных (Lazy fetch)
        String language = generatedCode.getRequirement().getLanguage(); 
        
        // ... логика валидации ...
    }
}

```

### Паттерн Strategy для шаблонов
**Решение:** Система выбирает шаблон динамически на основе языка и фреймворка, используя репозиторий как хранилище стратегий генерации.

**Пример (`CodeGenerationService.java`):**

```java
private List<CodeTemplate> findSuitableTemplates(String language, String framework) {
    // Поиск наиболее подходящего шаблона в базе знаний
    List<CodeTemplate> templates = templateRepository.findByLanguageAndFramework(language, framework);
    
    if (templates.isEmpty()) {
        // Fallback стратегия: использование базового шаблона
        return getDefaultTemplates(language); 
    }
    return templates;
}

```

---

## Отклонения от объектной модели и обоснование
В процессе реализации были внесены изменения в исходную архитектуру для повышения надежности и упрощения MVP.

### Замена внутреннего NLP Engine на внешний API* 
**Исходная модель:** Диаграмма классов  предполагала наличие класса `NLPEngine` и `AdvancedNLPEngine` внутри инфраструктуры приложения.

* **Реализация:** Создан `AIModelService`, который выступает адаптером к API Groq (модель Llama-3).
* **Обоснование:** Разработка собственного NLP-движка такой сложности нецелесообразна в рамках лабораторной работы. Использование LLM позволяет обрабатывать нечеткие запросы  гораздо эффективнее, чем жестко закодированные алгоритмы.



### Отказ от IDE Плагинов (в текущей итерации)* 
**Исходная модель:** Диаграмма "Направления развития" и Классы `VSCodePlugin`, `IntelliJPlugin`.


* **Реализация:** Реализован только веб-интерфейс (`WebInterfaceController`).
* **Обоснование:** Это заявлено как "направление развития". В текущей версии фокус сделан на полноценном веб-приложении с возможностью скачивания файлов, что является достаточным для сценариев "Быстрая разработка" и "Обучение".



### Упрощение структуры пакетов языков* 
**Исходная модель:** Интерфейс `ILanguagePackage` и классы `PythonDjangoPackage`, `JavaScriptReactPackage`.


* **Реализация:** Логика языков перенесена в **Базу Данных** (`CodeTemplate` table).
* **Обоснование:** Хранение правил генерации в коде нарушает принцип Open-Closed. Хранение шаблонов в БД позволяет добавлять новые языки и фреймворки без перекомпиляции приложения, что делает систему более гибкой.

### Язык программирования
Использавала Java, потому что питон я не люблю!!!
---

## Инструкция по запуску
1. **Клонировать репозиторий**
  
2. **База данных:**
* Установить PostgreSQL.
* Создать базу данных `postgres` и схему `codegen`.
* Настроить `application.properties` (url, username, password).


3. **API Ключ:**
* Получить ключ в Groq Cloud.
* Указать его в `application.properties` (`ai.api.key`).


4. **Сборка и запуск:**
```bash
mvn clean install
mvn spring-boot:run

```


5. **Использование:**
* Открыть браузер: `http://localhost:8080/templates/index.html`
* Тестовый пользователь создается автоматически при первом запуске (`DataInitializer`).
