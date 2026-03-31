---
name: UI writer
description: Write Jetpack Compose UI components following project patterns
tags: [compose, ui, android, jetpack]
---

# Write Compose UI

You are writing Jetpack Compose UI for the Personal Sudoku Android app. Follow these guidelines carefully.

## Architecture Pattern

This app uses **MVVM with Clean Architecture**:
- **Screens** live in `presentation/<feature>/` packages (e.g., `presentation/game/`, `presentation/main/`)
- **Reusable UI components** go in `presentation/view/` package
- **ViewModels** manage UI state and coordinate domain use cases
- **State flows** from ViewModel → Composable via `collectAsState()`

## Material 3 & Theme

**Always use Material 3 components and theme system:**

```kotlin
import androidx.compose.material3.*
import org.dsh.personal.sudoku.theme.* // Color schemes

// Access theme values
MaterialTheme.colorScheme.primary
MaterialTheme.typography.bodyLarge
MaterialTheme.shapes.medium
```

**Color Scheme:**
- `primary` - SamsungOneUI8Blue for primary actions
- `secondary` - Text/icon accents
- `error` - SamsungOneUI8Red for errors
- `surface` / `onSurface` - Card backgrounds and text
- `surfaceVariant` / `onSurfaceVariant` - Secondary surfaces

## Spacing System

**Always use `Dimens` object for spacing** (from `presentation/view/Dimens.kt`):

```kotlin
import org.dsh.personal.sudoku.presentation.view.Dimens

Dimens.VerySmall // 2.dp
Dimens.Small     // 4.dp
Dimens.BigSmall  // 6.dp
Dimens.Medium    // 8.dp
Dimens.BigMedium // 12.dp
Dimens.Large     // 16.dp
Dimens.Icon      // 24.dp
Dimens.Image     // 100.dp
```

**Never hardcode dp values** - always use `Dimens`.

## Composable Patterns

### 1. Always Add @Stable Annotation

```kotlin
@Stable
@Composable
fun MyComponent(
    data: MyData,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### 2. Accept Modifier as Last Parameter

```kotlin
@Composable
fun MyComponent(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Always last, with default
) { }
```

### 3. Extract Helper Composables as Private

```kotlin
@Stable
@Composable
private fun MyHelperComponent() {
    // Small, single-use component
}
```

### 4. Use Data Classes for Complex Props

Prefer passing data classes over many individual parameters:

```kotlin
@Immutable
data class CardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Stable
@Composable
fun MyCard(data: CardData, modifier: Modifier = Modifier) { }
```

## Common UI Patterns in This Project

### Loading State

```kotlin
import org.dsh.personal.sudoku.presentation.view.LoadingState

LoadingState(modifier = Modifier.fillMaxSize())
```

### Error State

```kotlin
import org.dsh.personal.sudoku.presentation.view.ErrorState

ErrorState(
    message = "Error message",
    modifier = Modifier.fillMaxSize()
)
```

### Layout Structure

Follow this structure for screens:

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { /* TopAppBar */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.Large)
        ) {
            // Content
        }
    }
}
```

## ViewModels

**ViewModels use Koin for injection:**

```kotlin
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// In presentation DI module
viewModel { MyViewModel(get(), get()) }

// In Composable
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
}
```

**ViewModel pattern:**

```kotlin
class MyViewModel(
    private val someUseCase: SomeUseCase,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(MyUiState())
    val state: StateFlow<MyUiState> = _state.asStateFlow()

    fun onAction(action: MyAction) {
        viewModelScope.launch(ioDispatcher) {
            // Handle action
        }
    }
}

@Immutable
data class MyUiState(
    val isLoading: Boolean = false,
    val data: List<String> = emptyList(),
    val error: String? = null
)
```

## File Organization

- **Screens**: `presentation/<feature>/MyScreen.kt`
- **ViewModels**: `presentation/<feature>/MyViewModel.kt`
- **Reusable components**: `presentation/view/MyComponent.kt`
- **UI state classes**: Define in same file as ViewModel or screen

## Common Imports

```kotlin
// Material 3
import androidx.compose.material3.*

// Layout
import androidx.compose.foundation.layout.*

// Modifiers
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

// State
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Koin
import org.koin.androidx.compose.koinViewModel

// Project
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.theme.*
```

## Best Practices

1. **Use `collectAsStateWithLifecycle()`** for ViewModel StateFlows
2. **Mark UI state classes as `@Immutable`** or `@Stable`
3. **Avoid composable lambdas in parameters** - use function types instead
4. **Extract repetitive UI** into reusable components in `view/` package
5. **Use remember { }** for expensive calculations
6. **Use LaunchedEffect** for side effects tied to composition
7. **Keep Composables focused** - break down complex screens
8. **Follow Material 3 guidelines** for component usage

## Navigation

Navigation uses custom Navigation3 implementation:

```kotlin
// In screen
val navigator = koinInject<Navigator>()

// Navigate
Button(onClick = { navigator.navigate(SudokuRoutes.Settings) }) {
    Text("Settings")
}

// Go back
navigator.goBack()
```

## DO NOT

- ❌ Hardcode dp values - use `Dimens`
- ❌ Hardcode colors - use `MaterialTheme.colorScheme`
- ❌ Use old Material (androidx.compose.material) - use Material 3
- ❌ Forget @Stable annotation on Composables
- ❌ Create massive single-file screens - extract components
- ❌ Use `mutableStateOf` in ViewModels - use StateFlow

## When Creating New UI

1. Determine if it's a **screen** (new feature) or **reusable component**
2. Create in appropriate package (`presentation/<feature>/` or `presentation/view/`)
3. Use existing patterns from similar components
4. Register ViewModel in appropriate DI module if needed
5. Follow the spacing, color, and Material 3 patterns
6. Add @Stable annotation
7. Make it testable - separate UI logic into ViewModel

Now write the UI component as requested.
