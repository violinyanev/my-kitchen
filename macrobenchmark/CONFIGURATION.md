# Macrobenchmark Configuration

This file contains configuration options for customizing the behavior of macrobenchmarks.

## Configuration Options

### Iterations
- **Default**: 5 iterations per benchmark
- **Recommendation**: Use 5-10 iterations for reliable results
- **CI/CD**: Consider reducing to 3 iterations for faster builds

### Compilation Modes
1. **None**: No ahead-of-time compilation (worst performance)
2. **Partial**: Limited compilation (realistic performance)  
3. **SpeedProfile**: Full profile-guided optimization (best performance)

### Metrics Collected

#### Startup Timing Metrics
- **Time to Initial Display (TTID)**: First frame rendered
- **Time to Full Display (TTFD)**: App fully interactive
- **Process initialization time**
- **Activity creation time**

#### Frame Timing Metrics  
- **Frame render time percentiles** (P50, P90, P95, P99)
- **Jank detection** (frames >16.67ms for 60fps)
- **Frame rate consistency**
- **GPU utilization** (when available)

### Device Requirements

#### Supported Devices
- Physical Android devices only (emulators not supported)
- Android 7.0 (API 24) or higher
- At least 2GB RAM recommended
- Stable thermal conditions

#### Device Preparation
```bash
# Disable animations
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0  
adb shell settings put global animator_duration_scale 0

# Ensure stable performance
# - Disconnect from charger
# - Close other applications
# - Set device to performance mode if available
# - Ensure consistent temperature
```

### Environment Variables

The following environment variables can be used to customize benchmark behavior:

```bash
# Number of iterations (default: 5)
export BENCHMARK_ITERATIONS=5

# Compilation mode (none|partial|speed)
export COMPILATION_MODE=speed

# Package name for debug builds
export APP_PACKAGE_DEBUG=com.ultraviolince.mykitchen.debug

# Package name for release builds  
export APP_PACKAGE_RELEASE=com.ultraviolince.mykitchen

# Timeout for UI operations (milliseconds)
export UI_TIMEOUT=5000
```

### Benchmark Profiles

#### Quick Profile (for CI/CD)
- 3 iterations
- Essential benchmarks only
- Reduced timeouts

#### Full Profile (for detailed analysis)
- 10 iterations
- All benchmark types
- Extended UI interactions

#### Regression Profile (for release testing)
- 5 iterations
- Focus on critical user journeys
- Comparison with baseline measurements

### Troubleshooting

#### Common Issues

1. **Package not found**
   - Ensure app is installed: `adb install app/build/outputs/apk/benchmark/app-benchmark.apk`
   - Check package name matches in manifest

2. **UI elements not found**
   - App may not have loaded completely
   - UI structure may have changed
   - Add explicit waits: `device.wait(Until.hasObject(...), timeout)`

3. **Inconsistent results**
   - Device thermal throttling
   - Background processes interfering
   - Inconsistent device state between runs

4. **Build failures**
   - Android Gradle Plugin version compatibility
   - Missing test dependencies
   - Compilation target mismatch

#### Debugging Tips

```kotlin
// Dump UI hierarchy for debugging
device.dumpWindowHierarchy(System.out)

// Wait for specific elements
device.wait(Until.hasObject(By.desc("Recipe List")), 5000)

// Add explicit delays
device.waitForIdle(1000)

// Log benchmark progress
Log.d("Benchmark", "Starting interaction phase")
```

### Performance Baselines

Establish performance baselines for regression detection:

#### Startup Performance Targets
- Cold start TTID: < 1.5 seconds
- Cold start TTFD: < 2.5 seconds  
- Warm start TTID: < 800ms
- Hot start TTID: < 400ms

#### UI Performance Targets
- P95 frame time: < 16.67ms (60fps)
- P99 frame time: < 20ms
- Jank percentage: < 5%
- Scroll response: < 100ms

### Continuous Integration

#### GitHub Actions Setup
```yaml
- name: Run Critical Benchmarks
  run: |
    ./scripts/run_benchmarks.sh startup
    ./scripts/run_benchmarks.sh scrolling
  env:
    BENCHMARK_ITERATIONS: 3
    COMPILATION_MODE: speed
```

#### Regression Detection
- Compare results with previous releases
- Set performance thresholds for build failures
- Generate performance reports for PRs
- Archive benchmark results for trend analysis
