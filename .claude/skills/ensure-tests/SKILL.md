---
name: ensure-tests
description: Ensure unit tests and UI tests are added or updated when implementing new features, fixing bugs, or changing behavior in the my-kitchen KMP project. Use this skill whenever new ViewModels, use cases, repository methods, or Compose screens are added or modified — even if the user doesn't explicitly ask for tests. Also use when the user asks to "add tests", "write tests for this", or "make sure this is tested".
---

# Ensure Tests — my-kitchen

When a feature lands, two questions must be answered before the task is done:
1. Is the **logic** tested? (ViewModel state transitions, use-case outcomes, repository behavior)
2. Is the **UI** tested? (screen renders correctly for each meaningful state)

Work through both, write the missing tests, and confirm they pass locally.

---

## Where tests live

| What changed | Test location | Runner |
|---|---|---|
| Domain use case (`shared/domain/`) | `shared/domain/src/commonTest/` | `kotlin.test` + `runTest` |
| Data repository (`shared/data/`) | `shared/data/src/commonTest/` | `kotlin.test` + `runTest` |
| ViewModel (`shared/ui/`) | `androidApp/src/test/` | Robolectric (`ScreenshotTestRunner`) |
| Compose screen (`shared/ui/`) | `androidApp/src/test/` | Robolectric (`ScreenshotTestRunner`) |

`shared/ui` has no `commonTest` source set — ViewModel and Compose tests both go in `androidApp/src/test/`.

---

## Test patterns

### Domain use-case tests (`shared/domain/src/commonTest/`)

```kotlin
class MyUseCaseTest {
    @Test
    fun `returns success when repo succeeds`() = runTest {
        val repo = FakeRecipeRepository()
        repo.syncResult = Result.success(Unit)
        val result = MyUseCase(repo)()
        assertTrue(result.isSuccess)
    }
}
```

- Import `kotlin.test.*` for assertions (`assertTrue`, `assertEquals`, `assertFails`)
- Use `FakeRecipeRepository` from the same `commonTest` package (already exists)
- Use `kotlinx.coroutines.test.runTest` for suspend functions
- Collect flows with `.first()` (from `kotlinx.coroutines.flow`)

### ViewModel unit tests (`androidApp/src/test/`)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(ScreenshotTestRunner::class)
class MyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state change after action`() = runTest {
        val repo = FakeRepo()
        val vm = MyViewModel(MyUseCase(repo))
        vm.doSomething()
        advanceUntilIdle()
        assertEquals(expectedState, vm.state.value)
    }
}
```

Key points:
- Always `setMain(UnconfinedTestDispatcher())` — `viewModelScope` needs a Main dispatcher
- Always `resetMain()` in `@After`
- `advanceUntilIdle()` after any ViewModel action that launches coroutines
- Create local `private class FakeXxxRepo : XxxRepository` inline — test fakes from other modules aren't accessible
- `kotlinx.coroutines.test` is declared in `libs.versions.toml`; add `testImplementation(libs.kotlinx.coroutines.test)` to `androidApp/build.gradle.kts` if not present

### Screenshot (golden) tests — `@Preview` functions

Every meaningful **visual state** of a screen needs a `@Preview` in `shared/ui/src/androidMain/.../screens/<screen>/<Screen>Previews.kt`. Roborazzi auto-converts these into golden screenshot tests via `generateComposePreviewRobolectricTests`.

```kotlin
@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "My Screen — Error State")
@Composable
internal fun MyScreenErrorPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        MyScreenContent(
            state = MyState(isError = true),
            onAction = {},
        )
    }
}
```

**How goldens are recorded:** The `verify-screenshots` CI job runs `verifyAndRecordRoborazziDebug`. When a new `@Preview` has no golden yet, the job records it, then auto-commits the new image file to the PR branch with the message "Updated Roborazzi screenshots after verification" and posts a PR comment. No manual step needed — just push the preview and let CI handle it.

**When goldens must be re-recorded:** If existing UI changes cause a visual diff, the `verify-screenshots` job fails and the same auto-commit mechanism updates the affected goldens. Review the committed diff to confirm the change was intentional.

Goldens live in `androidApp/src/test/screenshots/` and are committed to the repo (tracked by git).

**Rule of thumb:** For every new state you add to `RecipeListState`, `AddEditState`, or any other screen state, add a `@Preview` for it. Behavioral tests (`createComposeRule`) check logic; `@Preview` + roborazzi checks visual appearance.

---

### Compose UI tests (`androidApp/src/test/`)

Test the `*Content` composable (the stateless one), not the screen that wires the ViewModel:

```kotlin
@OptIn(ExperimentalResourceApi::class)
@RunWith(ScreenshotTestRunner::class)
class MyScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `element is shown in correct state`() {
        composeTestRule.setContent {
            val ctx = LocalContext.current
            remember(ctx) { setResourceReaderAndroidContext(ctx) }
            AppTheme {
                MyScreenContent(
                    state = MyState(/* meaningful fields */),
                    onAction = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Expected text").assertIsDisplayed()
    }
}
```

Use these finders/matchers:
- `onNodeWithText("…")` / `onNodeWithContentDescription("…")`
- `.assertIsDisplayed()`, `.assertIsNotDisplayed()`, `.assertDoesNotExist()`
- `.performClick()` — then assert a callback was or wasn't called via a `var clicked = false` flag

---

## Checklist when a feature is added

For each changed component, ask:

**Use case?**
- [ ] Happy path (success result)
- [ ] Failure path (error result / exception)
- [ ] Edge cases mentioned in the task

**ViewModel?**
- [ ] Initial state is correct
- [ ] State after each public action (`fun doX()`) is correct
- [ ] Failure in the use case produces the right error state
- [ ] Side effects (navigation triggers, logout, etc.) fire when expected

**Compose screen?**
- [ ] Default / happy-path state renders key elements
- [ ] Error / unreachable / loading states show the right UI
- [ ] User actions (button clicks) call the right callback — and blocked actions don't
- [ ] A `@Preview` exists in `*Previews.kt` for every new visual state (roborazzi turns these into golden screenshot tests automatically)

---

## What to run locally after writing tests

```bash
# Detekt + domain/data unit tests + server compile — always runnable
./scripts/validate-pr.sh

# ViewModel and Compose tests need CI (AGP/Robolectric require Google Maven)
# Push and let the `build` and `verify-screenshots` CI jobs validate them
```

Changes to `androidApp/build.gradle.kts` (e.g. adding a test dependency) are CI-only — see CLAUDE.md.

---

## Common pitfalls

- **Don't share test fakes across modules.** `FakeRecipeRepository` in `shared/domain/src/commonTest/` is invisible to `androidApp/src/test/`. Duplicate only what the test needs, keeping the fake minimal.
- **Don't forget `resetMain()`.** Leaving a test dispatcher set leaks into other tests.
- **String resources in Compose tests** require `setResourceReaderAndroidContext(ctx)` inside `setContent {}` — without it, string lookups throw at test time.
- **Screenshot tests** (`verify-screenshots` CI job) record golden images; if a screen's appearance changes, the job will fail until goldens are re-recorded with `./gradlew :androidApp:recordRoborazziDebug` (CI-only). If your test is not a screenshot test, use `createComposeRule()` not `createAndroidComposeRule()` to avoid auto-capture.
