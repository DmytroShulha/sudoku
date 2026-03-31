# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug variant
./gradlew assembleDebug

# Build debug AAB (for Play Store)
./gradlew bundleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run unit tests with coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Run lint checks
./gradlew lintDebug

# Run static analysis (Detekt)
./gradlew detekt

# Full CI pipeline (lint + tests + coverage + static analysis)
./gradlew lintDebug detekt testDebugUnitTest jacocoTestReport

# Build release AAB (requires signing configuration)
./gradlew bundleRelease
```

## Project Overview

Personal Sudoku is an Android game built with modern Android development practices:
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Clean Architecture layers
- **Dependency Injection:** Koin
- **Navigation:** Custom Navigation3 implementation (androidx.navigation3)
- **Local Storage:** Room database (statistics), DataStore Preferences (settings)
- **Async:** Kotlin Coroutines & Flow

## Architecture

The codebase follows Clean Architecture with three distinct layers:

### 1. Domain Layer (`domain/`)
Pure Kotlin business logic with no Android dependencies:
- **Use Cases:** Single-responsibility classes encapsulating business operations (e.g., `GenerateGameFieldUseCase`, `ValidateBoardUseCase`)
- **Entities:** Core data models (`SudokuBoardState`, `SudokuCellState`, `SudokuGameState`)
- **Repository Interfaces:** Abstract contracts for data access

### 2. Data Layer (`data/`)
Implements domain repository interfaces:
- **SudokuGameRepository:** Manages game state persistence via DataStore
- **CurrentGameStorage:** Handles save/load of current game
- **SudokuGeneratorEasy:** Implements puzzle generation algorithm
- **Room Database:** Stores game statistics (`SudokuDatabase`, `EntryDao`, `StatisticEntry`)

### 3. Presentation Layer (`presentation/`)
Compose UI and ViewModels:
- **Screen Modules:** Each feature has its own package (e.g., `game/`, `main/`, `settings/`, `statistic/`)
- **ViewModels:** Manage UI state and coordinate use cases
- **Composables:** Reusable UI components in `view/` package

## Dependency Injection Structure

Koin modules are organized hierarchically in `PersonalApplication.kt`:
```
appModule (core dispatchers)
└── sudokuDi
    ├── sudokuPresentationDi (ViewModels, UI state managers)
    ├── sudokuDomainDi (Use Cases, business logic)
    └── sudokuDataDi (Repositories, Database, DataStore)
```

Each layer has its own DI module:
- `PersonalDi.kt` - Core coroutine dispatchers (IO, Main, Default) with qualifiers
- `domain/Domain.kt` - Domain use cases and handlers
- `data/di/SudokuDataDi.kt` - Data layer dependencies
- `presentation/SudokuPresentationDi.kt` - ViewModels and presentation dependencies

Dispatcher injection uses qualifiers from `core/di/DispatchersQualifiers.kt` to enable testing.

## Navigation System

Uses a custom **Navigation3** implementation with multi-backstack support:
- **Routes:** Defined in `Sudoku.kt` as serializable data objects using `@Serializable` and `NavKey`
- **Navigator:** Custom navigation handler in `core/Navigator.kt` manages multiple backstacks for each top-level route
- **NavigationState:** Maintains navigation state across the app

Key insight: The navigation system is custom-built, not standard Jetpack Navigation. The `Navigator` class handles route changes and backstack management manually.

## Game State Management

The Sudoku game state flows through several handlers:
1. **SudokuHandler** (`domain/SudokuHandler.kt`) - Core game logic (cell updates, validation, notes)
2. **SudokuUpdateHelper** - Manages board state updates
3. **SudokuHelper** - Static utility functions for Sudoku rules validation
4. **CurrentGameHandler** - Use case orchestrating save/load/clear operations

Game state is represented by `SudokuGameState` which contains:
- `board: SudokuBoardState` - Current puzzle state with cell values and notes
- `difficulty: String` - Puzzle difficulty level
- Metadata like start time, mistakes count, completion status

## Testing

Test structure mirrors source structure in `app/src/test/`:
- Unit tests use JUnit 4
- Mocking with MockK
- Coroutine testing with `kotlinx-coroutines-test`

Run single test file:
```bash
./gradlew test --tests "org.dsh.personal.sudoku.domain.SudokuHandlerTest"
```

Coverage reports (JaCoCo) are generated in `app/build/reports/coverage/test/debug/` and exclude UI/DI/database code (see `fileFilter` in `app/build.gradle.kts`).

## Code Quality

- **Detekt:** Configuration in `config/detekt/detekt.yml` with baseline at `config/detekt/detekt-baseline.xml`
- **Lint:** Android Lint runs on debug variant
- **Coverage Threshold:** Enforced in CI, exclusions configured in `app/build.gradle.kts`

## Version Management

App version is managed via `app/version.properties`:
- `appVersionCode` - Integer build number
- `appVersionName` - Semantic version string

Version is loaded in `app/build.gradle.kts` via `loadVersionProperties()` function.

## Signing Configuration

Release builds use either:
1. **CI Environment:** Base64-encoded keystore from `ANDROID_KEYSTORE_BASE64` env var
2. **Local Development:** Keystore path from `local.properties` with keys like `android.injected.signing.store.file`

Never commit signing credentials to the repository.
