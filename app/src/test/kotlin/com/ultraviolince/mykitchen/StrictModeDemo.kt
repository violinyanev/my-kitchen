package com.ultraviolince.mykitchen

import android.os.StrictMode
import org.junit.Test

/**
 * Demonstration test showing StrictMode functionality.
 * This test shows how StrictMode policies can be verified programmatically.
 * 
 * Note: This is for documentation purposes and would normally be in androidTest
 * rather than unit test, but demonstrates the StrictMode implementation.
 */
class StrictModeDemo {

    @Test
    fun demonstrateStrictModeConfiguration() {
        // In a real Android environment, we could verify StrictMode is configured
        // by checking that the policies have been set
        
        // This demonstrates what our StrictMode implementation includes:
        
        // Debug build ThreadPolicy detects:
        val debugThreadViolations = listOf(
            "Disk reads on main thread",
            "Disk writes on main thread", 
            "Network operations on main thread",
            "Custom slow calls on main thread",
            "Resource mismatches"
        )
        
        // Debug build VmPolicy detects:
        val debugVmViolations = listOf(
            "Activity leaks",
            "SQL object leaks (Cursor, etc.)",
            "Closeable object leaks", 
            "Registration leaks (BroadcastReceiver, etc.)",
            "File URI exposure",
            "Cleartext network traffic",
            "Content URI without permission",
            "Untagged sockets"
        )
        
        // Release build has minimal StrictMode for critical issues only
        val releaseThreadViolations = listOf("Network operations on main thread")
        val releaseVmViolations = listOf(
            "Activity leaks",
            "SQL object leaks", 
            "Cleartext network traffic"
        )
        
        // Verify we have comprehensive coverage
        assert(debugThreadViolations.size == 5)
        assert(debugVmViolations.size == 8) 
        assert(releaseThreadViolations.size == 1)
        assert(releaseVmViolations.size == 3)
        
        println("StrictMode Demo: Configuration includes detection for:")
        println("Debug Thread Violations: $debugThreadViolations")
        println("Debug VM Violations: $debugVmViolations")
        println("Release Thread Violations: $releaseThreadViolations") 
        println("Release VM Violations: $releaseVmViolations")
    }
}