# Macrobenchmark Setup

This module contains Google Macrobenchmarks for the My Kitchen app to identify and measure performance issues.

## What is Measured

### Startup Performance
- **Cold Startup**: App startup from a completely fresh state
- **Warm Startup**: App startup when the app process is already in memory
- **Hot Startup**: App startup when the app is already in memory and visible

### UI Performance (Jank Detection)
- **Scrolling Performance**: Frame timing during list scrolling
- **Navigation Performance**: Frame timing during screen transitions
- **Interaction Performance**: Response time for button taps and form interactions

### Compilation Modes
- **None**: No ahead-of-time compilation (worst case)
- **Partial**: Some compilation (realistic case)
- **SpeedProfile**: Full compilation with profile-guided optimization (best case)

## Available Benchmarks

### StartupBenchmark
- `startup()`: Basic cold startup measurement
- `startupWithCompilation()`: Cold startup with speed profile compilation
- `startupPartial()`: Cold startup with partial compilation

### ScrollingBenchmark
- `scrollRecipeList()`: Measures frame timing during scrolling
- `scrollRecipeListWithoutCompilation()`: Scrolling performance without compilation
- `navigateAndScroll()`: Combined navigation and scrolling performance

### InteractionBenchmark
- `tapInteractions()`: Measures response time for UI taps
- `navigationBenchmark()`: Screen transition performance
- `formInteractionBenchmark()`: Form input and submission performance

### BaselineProfileGenerator
- `generate()`: Creates a baseline profile for the app to optimize performance

## Running Benchmarks

### Prerequisites
1. Use a physical device (emulators are not supported for macrobenchmarks)
2. Ensure the device is not plugged into a charger during benchmarking
3. Close other apps and disable animations for consistent results:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

### Running from Command Line
```bash
# Run all benchmarks
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest

# Run specific benchmark class
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ultraviolince.mykitchen.macrobenchmark.StartupBenchmark

# Generate baseline profile
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ultraviolince.mykitchen.macrobenchmark.BaselineProfileGenerator
```

### Running from Android Studio
1. Switch to the `benchmark` build variant
2. Right-click on the benchmark test class
3. Select "Run" to execute the benchmark

## Understanding Results

### Startup Timing Metrics
- **Time to Initial Display (TTID)**: Time until the first frame is displayed
- **Time to Full Display (TTFD)**: Time until the app is fully interactive

### Frame Timing Metrics
- **P50, P90, P95, P99**: Percentiles of frame rendering times
- **Jank**: Frames that take longer than 16.67ms (60fps) or 11.11ms (90fps)

### Compilation Impact
- Compare results between `None`, `Partial`, and `SpeedProfile` to understand the impact of compilation
- Use baseline profiles to improve startup and interaction performance

## Best Practices

1. **Run benchmarks on consistent hardware** - Use the same device for all benchmark runs
2. **Minimize external factors** - Close other apps, disable animations, ensure stable device temperature
3. **Use multiple iterations** - The default iterations (5) provide statistically significant results
4. **Monitor regression** - Set up CI to run benchmarks on each release to catch performance regressions
5. **Focus on user journeys** - Measure the most common user interactions and workflows

## Troubleshooting

### Common Issues
- **App not found**: Ensure the app is installed and the package name matches
- **No scrollable content**: The app may not have loaded or the UI structure may have changed
- **Test timeouts**: Increase timeout values if the app takes longer to load

### Debugging
- Add `device.dumpWindowHierarchy(System.out)` to see the current UI structure
- Use `device.wait(Until.hasObject(By.desc("specific element")), timeout)` to wait for specific elements
- Check logcat output for any app crashes or errors during benchmark execution

## Integration with CI/CD

Add this to your GitHub Actions or other CI pipeline:
```yaml
- name: Run Macrobenchmarks
  run: ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
  env:
    # Ensure emulator is not used
    ANDROID_DEVICE: physical
```

The benchmark results will be saved in `macrobenchmark/build/reports/androidTests/` and can be analyzed to identify performance issues and track improvements over time.