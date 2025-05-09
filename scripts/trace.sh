#!/bin/bash

# Script setup
mkdir -p traces
package_name="com.ultraviolince.mykitchen.debug"
activity="com.ultraviolince.mykitchen.recipes.presentation.MainActivity"

# Restart the app
adb shell am force-stop $package_name
adb shell am start -n $package_name/$activity

# Record perfetto trace
cat trace_config.pbtxt | adb shell perfetto -c - --txt -o /data/misc/perfetto-traces/trace.perfetto-trace

# Store the trace with unique name (the date time)
current_date_time="`date +%Y.%m.%d_%H.%M`"
adb pull /data/misc/perfetto-traces/trace.perfetto-trace "traces/trace-$current_date_time"
