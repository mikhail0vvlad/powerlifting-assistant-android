# CODEMAP — карта кода Powerlifting Assistant

> **Назначение этого файла.** Это индекс для быстрого ответа на любой вопрос по коду
> (включая «что делает эта строчка» + скриншот). Прочитав его, ассистент за секунды
> определяет: к какому слою относится фрагмент, в каком файле он лежит, какие у него
> зависимости и какие конвенции действуют. Файл отражает состояние ветки на момент
> написания — при расхождении источник истины всегда сам код.

---

## 0. Как пользоваться (рецепт «что делает эта строчка»)

1. **Определи слой по сигнатуре/импортам:**
   - `@Composable`, `Modifier`, `hiltViewModel()` → **экран** (`presentation/screens`).
   - `@HiltViewModel`, `UiState`, `_state.update`, `viewModelScope` → **ViewModel** (`presentation/viewmodel`).
   - `class …UseCase @Inject`, `operator fun invoke` → **use case** (`domain/usecase/...`).
   - `class …RepositoryImpl`, `tokenProvider.bearerToken()`, `cache.…`, `api.…` → **репозиторий** (`data/repo`).
   - `@Serializable data class …Dto / …Request / …Response` → **DTO** (`data/api/ApiModels.kt`).
   - `fun XxxDto.toDomain()` / `fun Xxx.toRequest()` → **маппер** (`data/mapper`).
   - `interface …Api`, аннотации `@GET/@POST/@PUT/@DELETE` → **API-контракт** (`data/api/PowerliftingApi.kt`).
2. **Найди фичу** в таблице §4 → получишь весь вертикальный срез (экран → VM → use case → repo → endpoint).
3. **Сверь конвенцию** в §3 — почти весь код повторяет 5–6 шаблонов; зная шаблон, строку
   объясняешь без чтения всего файла.
4. **Проверь нюансы** в §8 — там собрано всё неочевидное (почему nullable, почему задержка и т.п.).

---

## 1. Что это за проект

Android-клиент для пауэрлифтеров. Общается с REST API сервером (`api/v1/...`), аутентификация
через Firebase. Программы тренировок, дневник питания, восстановление, история, достижения,
калькуляторы (блины, ИМТ), профиль с 1ПМ.

**Технологии:** Kotlin (JVM 17), Jetpack Compose + Material 3, Navigation Compose, Dagger Hilt,
Retrofit 2 + OkHttp + kotlinx.serialization, Firebase Auth/Storage, DataStore Preferences,
WorkManager, Coil, Coroutines/StateFlow. minSdk 26, compile/target SDK 34.

**Пакет:** `com.powerlifting.assistant` (applicationId — `com.powerlifting_assistant`).

---

## 2. Структура пакетов (слои Clean Architecture)

```
com.powerlifting.assistant
├── MainActivity.kt          Activity: запрос разрешения на уведомления, планирование WorkManager,
│                            установка темы (SettingsViewModel) и RootNav.
├── PowerliftingApp.kt       @HiltAndroidApp Application (пустой).
│
├── data/
│   ├── api/
│   │   ├── PowerliftingApi.kt   Retrofit-интерфейс, ~20 эндпоинтов (см. §6).
│   │   └── ApiModels.kt        Все DTO/Request/Response (@Serializable).
│   ├── repo/                   Реализации репозиториев (паттерн §3.4).
│   ├── mapper/                 DTO ↔ domain: расширения toDomain()/toRequest()/toDto().
│   ├── cache/
│   │   ├── AppCache.kt         Набор именованных MemoryCache + правила инвалидации.
│   │   └── MemoryCache.kt      Потокобезопасный TTL-кэш на ConcurrentHashMap.
│   ├── auth/
│   │   └── FirebaseTokenProvider.kt   Выдаёт "Bearer <firebase id token>" (таймаут 15с).
│   └── local/
│       └── AppPreferences.kt   DataStore: тёмная тема + история поиска продуктов (max 10).
│
├── domain/
│   ├── model/                  Доменные модели + enum/sealed (см. §5).
│   ├── repository/             Интерфейсы репозиториев.
│   └── usecase/                Use case по доменам: profile, program, workout, nutrition, achievements.
│
├── presentation/
│   ├── screens/                Compose-экраны (см. §4, §7).
│   ├── viewmodel/              ViewModel + UiState + ErrorMapper (см. §3.2).
│   ├── navigation/
│   │   ├── RootNav.kt          auth → main (по FirebaseAuth.currentUser).
│   │   └── MainScaffold.kt     NavHost main: BottomBar (5 вкладок) + вложенные маршруты.
│   └── theme/Theme.kt          PowerliftingTheme(darkTheme).
│
├── notifications/
│   ├── ReminderWorker.kt       CoroutineWorker — суточное напоминание (MVP-текст).
│   └── NotificationUtils.kt    Канал + showReminder().
│
└── di/
    ├── NetworkModule.kt        Json, OkHttp (таймауты), Retrofit, PowerliftingApi.
    └── RepositoryModule.kt     @Binds всех 7 репозиториев.
```

---

## 3. Конвенции и идиомы (знать их = понимать любой файл)

### 3.1 Навигация
- Корень: `RootNav` — 2 маршрута `auth`/`main`; стартовый выбирается по `FirebaseAuth.getInstance().currentUser` ([RootNav.kt](app/src/main/java/com/powerlifting/assistant/presentation/navigation/RootNav.kt)).
- Внутри `main` — собственный `NavHost` в [MainScaffold.kt](app/src/main/java/com/powerlifting/assistant/presentation/navigation/MainScaffold.kt).
- **5 нижних вкладок** (порядок слева направо): `calculator` (Калькул.), `history` (История), `home` (Главная), `help` (Помощь), `profile` (Профиль).
- Вложенные маршруты (без вкладок): `calories`, `foodSearch`, `bmi`, `program`, `recovery`, `achievements`, `workout/{sessionId}`.
- Полноэкранные (прячут BottomBar): `workout/{sessionId}` и `recovery` — см. `fullScreenRoutes`.

### 3.2 ViewModel — стандартный шаблон
Почти каждый VM выглядит так:
```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(/* use cases */) : ViewModel() {
    data class UiState(val loading: Boolean = false, val error: String? = null, /* данные */)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try { /* useCase() */ _state.update { it.copy(loading = false, /* data */) } }
        catch (t: Throwable) { _state.update { it.copy(loading = false, error = errorMessage(t)) } }
    }
}
```
- Ошибки прогоняются через `errorMessage(t)` из [ErrorMapper.kt](app/src/main/java/com/powerlifting/assistant/presentation/viewmodel/ErrorMapper.kt) — переводит 503 / «нет хоста» / timeout в русские сообщения.
- Экран читает состояние через `val state by vm.state.collectAsState()` и вызывает `vm.load()/refresh()` в `LaunchedEffect(Unit)`.
- Исключения из шаблона:
  - `WorkoutViewModel` — не `UiState`, а `WorkoutUiState` + enum `WorkoutPhase` + два таймера-`Job` (упражнение/отдых + общий), вычисляемые свойства прогресса.
  - `FoodSearchViewModel` — `sealed interface SearchState` (Idle/Loading/Results/Empty/Error), debounce 400мс, запрос хранится в `SavedStateHandle`.
  - `SettingsViewModel` — без UiState, просто `darkTheme: StateFlow<Boolean>` из репозитория.
  - `WorkoutHistoryViewModel` — отдельно обрабатывает `HttpException` 404 при удалении.

### 3.3 UseCase — тонкие обёртки
Класс с `@Inject constructor(repo)` и `operator fun invoke(...)`, который просто делегирует в репозиторий.
Исключение с логикой — `GroupExercisesUseCase` (см. §8).

### 3.4 Repository Impl — паттерн «кэш → токен → api → маппер → инвалидация»
Чтения:
```kotlin
cache.syncUser()                      // сбрасывает кэш при смене Firebase-пользователя
cache.xxx.get(key)?.let { return it } // отдать из TTL-кэша, если свежее
val auth = tokenProvider.bearerToken()
val fresh = api.getXxx(auth).toDomain()
cache.xxx.put(key, fresh)
return fresh
```
Записи: `bearerToken(true)` (форс-рефреш токена) → `api.…` → `cache.afterXxxChange()`.
Образец — [ProfileRepositoryImpl.kt](app/src/main/java/com/powerlifting/assistant/data/repo/ProfileRepositoryImpl.kt).

### 3.5 Сеть и сериализация
- Авторизация передаётся **аргументом** `@Header("Authorization") auth: String` в каждый метод API (interceptor’а нет).
- `Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }` ([NetworkModule.kt](app/src/main/java/com/powerlifting/assistant/di/NetworkModule.kt)).
- OkHttp-таймауты: connect 15с, read/write 20с, call 25с.
- `baseUrl = BuildConfig.SERVER_BASE_URL` (см. §8 про сборку).

### 3.6 Мапперы
В `data/mapper` — функции-расширения `XxxDto.toDomain()`, `XxxDomain.toRequest()/toDto()`.
Имя файла = домен (`ProfileMapper`, `ProgramMapper`, `WorkoutMapper`, `NutritionMapper`, `AchievementMapper`).

### 3.7 DI
- `NetworkModule` (object, `@Provides`) — сетевой граф.
- `RepositoryModule` (abstract, `@Binds`) — связывает `XxxRepository` ← `XxxRepositoryImpl`.
- Всё в `SingletonComponent`, репозитории `@Singleton`.

### 3.8 Прочее
- Весь видимый текст — **на русском**; в коде есть осмысленные комментарии (часто про «сервер ещё не передеплоен»).
- Даты на проводе — ISO-строки (`LocalDate.toString()` = `yyyy-MM-dd`); часть домена хранит даты строкой.
- Статусы тренировок парсятся через `WorkoutStatus.parse()` (неизвестное → `PLANNED`).

---

## 4. Индекс по фичам (вертикальные срезы)

| Фича | Экран | ViewModel (ключ. поля state) | Use cases | Repo | Endpoints |
|------|-------|------------------------------|-----------|------|-----------|
| **Авторизация** | `AuthScreen` | — (напрямую FirebaseAuth) | — | — | Firebase SDK |
| **Главная** | `HomeScreen` | `HomeViewModel` (profile, calendar) | GetProfile, GetCalendar (параллельно через `async`) | Profile, Program | `GET profile`, `GET calendar` |
| **Программа** | `ProgramScreen` (537 стр.) | `ProgramViewModel` (active, profileMissingMaxes, mutating) | GetActiveProgram, GenerateProgram, GetProfile, RescheduleWorkout, SkipWorkout, MarkTrainingCompleted | Program, Profile | `programs/active`, `programs/generate`, `workouts/{id}/reschedule`, `workouts/{id}/skip` |
| **Восстановление** | `RecoveryScreen` | `RecoveryViewModel` (plannedWorkout, recommendation, startedSessionId) | GetActiveProgram, StartWorkoutSession | Program, Workout | `programs/active`, `workouts/sessions/start` |
| **Тренировка** | `WorkoutScreen` (549 стр.) | `WorkoutViewModel` (`WorkoutUiState`+`WorkoutPhase`) | GetWorkoutSessionDetail, GetProfile, GroupExercises, AddWorkoutSets, FinishWorkoutSession | Workout, Profile | `sessions/{id}` (GET), `sessions/{id}/sets`, `sessions/{id}/finish` |
| **История** | `WorkoutHistoryScreen` | `WorkoutHistoryViewModel` (sessions, deletingId, deleteError) | GetWorkoutHistory, DeleteWorkoutSession | Workout | `workouts/history`, `DELETE sessions/{id}` |
| **Питание** | `CaloriesTrackerScreen` | `CaloriesViewModel` (data: NutritionDay) | GetNutritionToday, UpdateNutritionGoals, AddNutritionEntry, DeleteNutritionEntry | Nutrition | `nutrition/today`, `nutrition/goals`, `nutrition/entries` |
| **Поиск продуктов** | `FoodSearchScreen` | `FoodSearchViewModel` (`SearchState`, query, history) | AddNutritionEntry (+ FoodRepository напрямую) | Food (локальный), Nutrition | нет (локальный CATALOG) + `nutrition/entries` |
| **Достижения** | `AchievementsScreen` | `AchievementsViewModel` (items) | GetAchievements, CreateAchievement, DeleteAchievement | Achievements | `achievements` GET/POST/DELETE |
| **Калькулятор блинов** | `CalculatorScreen` | — (логика в самом Composable) | — | — | нет |
| **ИМТ** | `BmiScreen` | `BmiViewModel` (heightCm, weightKg) | GetProfile | Profile | `GET profile` |
| **Профиль** | `ProfileScreen` | `ProfileViewModel` (profile, saved) | GetProfile, UpdateProfile | Profile | `GET/PUT profile` |
| **Тема** | (MainActivity) | `SettingsViewModel` (darkTheme) | — (SettingsRepository) | Settings (DataStore) | нет |
| **Подсказки/Помощь** | `NutritionTipsScreen`, `HelpScreen` | — | — | — | статический контент |

---

## 5. Доменный словарь (модели)

**Профиль/пользователь** ([User.kt](app/src/main/java/com/powerlifting/assistant/domain/model/User.kt)):
`User(userId, firebaseUid, email?, displayName?)`, `UserProfile(heightCm?, weightKg?, bench1rm?, squat1rm?, deadlift1rm?)` + `hasAllMaxes`, `NutritionGoals(caloriesGoal, proteinGoalG)`, `UserStats(achievementsCount, caloriesToday, proteinToday)`, `ProfileSummary(user, profile, nutritionGoals, stats)`, `ProfileUpdate(...)`.

**Программа** ([Program.kt](app/src/main/java/com/powerlifting/assistant/domain/model/Program.kt)):
`enum WorkoutStatus(wire)` = PLANNED/COMPLETED/MISSED/RESCHEDULED + `parse()`;
`sealed ProgramSchedule` = `Weekdays(Set<DayOfWeek>)` | `Dates(List<LocalDate>)`;
`TrainingProgram`, `ProgramExercise(…, percent1rm?, liftType)`, `ProgramWorkout(… status, originalWorkoutId?)` + `statusEnum`, `ActiveProgram(program?, upcomingWorkouts)`, `CalendarDay(… workoutId?)` + `statusEnum`, `TrainingCalendar(from, to, days)`.

**Тренировка** ([Workout.kt](app/src/main/java/com/powerlifting/assistant/domain/model/Workout.kt)):
`WorkoutSet(exerciseName, setNumber, weightKg, reps, rpe?)`, `StartSessionParams(programWorkoutId?, sleepHours?, wellbeing?, fatigue?, soreness?)`, `WorkoutSessionStart(sessionId, recommendation?)`, `WorkoutSessionDetail(…)`, `WorkoutHistoryItem(…)`, `ExerciseGroup(name, liftType, setGroups, isMain)`, `SetGroupInfo(percent1rm?, targetReps, targetSets, weightKg?, completedSets)` + `totalSets`/`allCompleted`.

**Питание** ([Nutrition.kt](app/src/main/java/com/powerlifting/assistant/domain/model/Nutrition.kt)):
`NutritionEntry`, `NutritionTotals`, `NutritionDay(date, totals, goals, entries)`.

**Прочее:** `Achievement(id, createdAtIso, note, photoUrl?)`, `FoodProduct(id, name, calories, proteinG, fatG, carbsG)` — `@Serializable`, КБЖУ на 100 г.

> DTO в `ApiModels.kt` почти зеркалят домен; основные различия: schedule приходит как `ScheduleDto(type, weekdays?, dates?)` и конвертируется в sealed `ProgramSchedule` маппером.

---

## 6. API-эндпоинты ([PowerliftingApi.kt](app/src/main/java/com/powerlifting/assistant/data/api/PowerliftingApi.kt))

Все принимают `@Header("Authorization") auth`. База — `api/v1/`.

| Метод | Путь | Тело/параметры → ответ |
|------|------|------------------------|
| GET | `profile` | → `ProfileResponse` |
| PUT | `profile` | `UpdateProfileRequest` → `UserProfileDto` |
| PUT | `nutrition/goals` | `UpdateNutritionGoalsRequest` → `NutritionGoalsDto` |
| GET | `nutrition/today` | `?date=` → `NutritionTodayResponse` |
| POST | `nutrition/entries` | `CreateNutritionEntryRequest` → `NutritionEntryDto` |
| DELETE | `nutrition/entries/{id}` | — |
| POST | `programs/generate` | `GenerateProgramRequest` → `TrainingProgramDto` |
| GET | `programs/active` | → `ActiveProgramResponse` |
| GET | `calendar` | `?from=&to=` → `CalendarResponse` |
| POST | `programs/workouts/{id}/reschedule` | `RescheduleWorkoutRequest` → `ProgramWorkoutDto` |
| POST | `programs/workouts/{id}/skip` | — |
| POST | `workouts/sessions/start` | `StartWorkoutSessionRequest` → `WorkoutSessionResponse` |
| GET | `workouts/sessions/{id}` | → `WorkoutSessionDetailResponse` |
| DELETE | `workouts/sessions/{id}` | — |
| POST | `workouts/sessions/{id}/sets` | `AddWorkoutSetsRequest` |
| POST | `workouts/sessions/{id}/finish` | `FinishWorkoutWithRatingRequest` |
| GET | `workouts/history` | `?limit=` → `WorkoutHistoryResponse` |
| GET | `achievements` | → `List<AchievementDto>` |
| POST | `achievements` | `CreateAchievementRequest` → `AchievementDto` |
| DELETE | `achievements/{id}` | — |

> «MarkTrainingCompleted» на клиенте есть как use case/VM-метод; на сервере отдельного эндпоинта в API-интерфейсе нет — проверять реализацию `ProgramRepositoryImpl`/`MarkTrainingCompletedUseCase` при вопросах о завершении дня из календаря.

---

## 7. Размеры экранов (ориентир «где много логики»)

Самые крупные и сложные UI-файлы:
- `WorkoutScreen.kt` — 549 стр. (фазы тренировки, таймеры, ввод подходов).
- `ProgramScreen.kt` — 537 стр. (генерация программы, выбор дней/дат, перенос/пропуск).
- `HomeScreen.kt` — 301, `FoodSearchScreen.kt` — 268, `CaloriesTrackerScreen.kt` — 233, `WorkoutHistoryScreen.kt` — 219.
- Средние: `ProfileScreen` 168, `AuthScreen` 161, `RecoveryScreen` 156, `CalculatorScreen` 132, `AchievementsScreen` 115.
- Мелкие/статические: `BmiScreen` 76, `HelpScreen` 60, `NutritionTipsScreen` 55.

Образец структуры экрана: `Surface { Column(verticalScroll) { … } }`, состояние через `collectAsState()`,
загрузка в `LaunchedEffect(Unit)` (см. [HomeScreen.kt](app/src/main/java/com/powerlifting/assistant/presentation/screens/HomeScreen.kt)).

---

## 8. Нюансы и неочевидные места (частые источники вопросов)

- **Сборка / SERVER_BASE_URL** ([app/build.gradle.kts](app/build.gradle.kts)): URL берётся из `BuildConfig.SERVER_BASE_URL`. Debug использует dev-fallback (`cloudpub.ru`-туннель); **release-сборка падает** (`GradleException`), если не передан `-PPOWERLIFT_SERVER_BASE_URL=...`. В debug подключён `network_security_config` ([app/src/debug](app/src/debug)).
- **Кэш per-user** ([AppCache.kt](app/src/main/java/com/powerlifting/assistant/data/cache/AppCache.kt)): TTL у каждого кэша свой (profile 60с, nutrition 30с, program/calendar 5 мин и т.д.). `syncUser()` чистит весь кэш при смене Firebase-uid. `afterXxxChange()` точечно инвалидирует связанные кэши (напр. изменение питания сбрасывает и профиль — там агрегаты).
- **Расчёт веса подходов** — `GroupExercisesUseCase`: вес = `1ПМ * percent1rm`, округление до шага блина **2.5 кг** (`roundToPlate`); reps парсятся из строки (`"5"` или `"5-8"` → 5), дефолт 8; «основные» движения = `squat/bench/deadlift` (остальное → подсобка, `isMain=false`).
- **Тренировка — фазы** (`WorkoutPhase`): LOADING → WARMUP → EXERCISE → REST → (циклы по сетам/упражнениям) → ACCESSORIES → FINISH_RATING → FINISHED. Подходы копятся локально в `completedSetsList` и отправляются **разом** в `finishWorkout()` (`addWorkoutSets` + `finishWorkoutSession`). Два таймера: `exerciseTimerSec`/`restTimerSec` (общий job) и `totalTimerSec`.
- **RPE**: модель `WorkoutSet` его поддерживает, но `completeSet()` логирует `rpe = null` (RPE вводится не на каждом шаге).
- **Удаление тренировки**: 404 трактуется как «уже удалена» — элемент убирается из списка + показывается подсказка «новый эндпоинт ещё не развёрнут» ([WorkoutHistoryViewModel.kt:60](app/src/main/java/com/powerlifting/assistant/presentation/viewmodel/WorkoutHistoryViewModel.kt)).
- **`CalendarDayDto.workoutId` nullable** намеренно — старый сервер без нового маппера; для таких дней перенос из календаря не работает (комментарий в `ApiModels.kt`).
- **Поиск продуктов локальный**: `FoodRepositoryImpl.CATALOG` — всего 3 продукта (яйца, куриная грудка, рис), `search()` имеет искусственную `delay(350)` ради видимого прогресса. История — DataStore, максимум 10, без дубликатов по `id`. Добавление в дневник: КБЖУ пересчитываются `grams/100.0`.
- **Тёмная тема по умолчанию `true`** ([AppPreferences.kt:36](app/src/main/java/com/powerlifting/assistant/data/local/AppPreferences.kt)); хранится в DataStore `app_prefs`, применяется в `MainActivity` до `RootNav`.
- **Уведомления**: `MainActivity` запрашивает `POST_NOTIFICATIONS` (API 33+) и ставит суточную `PeriodicWorkRequest` (`ReminderWorker`, unique `daily_reminder`, политика UPDATE). Текст напоминания — захардкоженный MVP.
- **Старт сессии vs восстановление**: `RecoveryScreen` собирает опросник (сон/самочувствие/усталость/болезненность) → `startWorkoutSession` возвращает `sessionId` + текстовую `recommendation`; навигация уходит на `workout/{sessionId}`.
- **Генерация программы** (`ProgramViewModel.generate`): приоритет `explicitDates` > `weekdays` > `null` (сервер сам ставит Пн/Ср/Пт). По умолчанию `weeks = 4`.

---

## 9. Файлы-якоря (куда смотреть первым делом)

| Вопрос про… | Открыть |
|-------------|---------|
| Навигацию / маршруты / вкладки | `presentation/navigation/MainScaffold.kt`, `RootNav.kt` |
| Контракт сервера | `data/api/PowerliftingApi.kt`, `data/api/ApiModels.kt` |
| Как устроен сетевой стек | `di/NetworkModule.kt` |
| Кэширование / TTL / инвалидацию | `data/cache/AppCache.kt`, `MemoryCache.kt` |
| Авторизацию / токен | `data/auth/FirebaseTokenProvider.kt`, `RootNav.kt`, `AuthScreen.kt` |
| Локальные настройки/тема/история | `data/local/AppPreferences.kt`, `SettingsViewModel.kt` |
| Бизнес-логику тренировки | `WorkoutViewModel.kt`, `domain/usecase/workout/GroupExercisesUseCase.kt` |
| Маппинг расписания программы | `data/mapper/ProgramMapper.kt` |
| Обработку ошибок | `presentation/viewmodel/ErrorMapper.kt` |
| Сборку / переменные / SDK | `app/build.gradle.kts` |
```
