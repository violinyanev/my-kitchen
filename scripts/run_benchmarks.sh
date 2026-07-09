#!/bin/bash

# Script to run macrobenchmarks locally
# Usage: ./scripts/run_benchmarks.sh [benchmark_type]
# benchmark_type can be: startup, scrolling, interaction, baseline, all

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

echo "Setting up device for benchmarking..."

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

echo "Device setup complete"

run_benchmark() {
    local benchmark_class="$1"
    local benchmark_name="$2"

    echo "Running $benchmark_name benchmarks..."

    ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
        -Pandroid.testInstrumentationRunnerArguments.class="com.ultraviolince.mykitchen.macrobenchmark.$benchmark_class" \
        --continue

    echo "$benchmark_name benchmarks complete"
}

BENCHMARK_TYPE="${1:-all}"

case "$BENCHMARK_TYPE" in
    "startup")
        run_benchmark "StartupBenchmark" "Startup"
        ;;
    "scrolling")
        run_benchmark "ScrollingBenchmark" "Scrolling"
        ;;
    "interaction")
        run_benchmark "InteractionBenchmark" "Interaction"
        ;;
    "baseline")
        echo "Generating baseline profile..."
        run_benchmark "BaselineProfileGenerator" "Baseline Profile"
        echo "Baseline profile generated at androidApp/src/main/baseline-prof.txt"
        ;;
    "all")
        echo "Running all benchmarks..."
        run_benchmark "StartupBenchmark" "Startup"
        run_benchmark "ScrollingBenchmark" "Scrolling"
        run_benchmark "InteractionBenchmark" "Interaction"
        run_benchmark "BaselineProfileGenerator" "Baseline Profile"
        ;;
    *)
        echo "Unknown benchmark type: $BENCHMARK_TYPE"
        echo "Available options: startup, scrolling, interaction, baseline, all"
        exit 1
        ;;
esac

echo "Benchmark execution complete!"
echo "Results available in: macrobenchmark/build/reports/androidTests/"

echo "Re-enabling animations..."
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1

echo "Device restored to normal state"
