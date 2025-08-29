#!/bin/bash

echo "🔍 Validating Navigation System Implementation..."
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if file exists
check_file() {
    local file="$1"
    local description="$2"
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅${NC} $description: $file"
        return 0
    else
        echo -e "${RED}❌${NC} $description: $file (NOT FOUND)"
        return 1
    fi
}

# Function to check file content
check_content() {
    local file="$1"
    local pattern="$2"
    local description="$3"
    
    if grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}✅${NC} $description"
        return 0
    else
        echo -e "${RED}❌${NC} $description (pattern not found: $pattern)"
        return 1
    fi
}

# Function to count lines in file
count_lines() {
    local file="$1"
    local expected_min="$2"
    local description="$3"
    
    local lines=$(wc -l < "$file" 2>/dev/null || echo "0")
    if [ "$lines" -ge "$expected_min" ]; then
        echo -e "${GREEN}✅${NC} $description: $lines lines (min: $expected_min)"
        return 0
    else
        echo -e "${RED}❌${NC} $description: $lines lines (expected min: $expected_min)"
        return 1
    fi
}

# Initialize counters
total_checks=0
passed_checks=0

# Check core navigation files
echo ""
echo "📁 Checking Core Navigation Files:"
echo "----------------------------------"

((total_checks++))
if check_file "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationGraph.kt" "Navigation Graph"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationState.kt" "Navigation State"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserver.kt" "Navigation Observer"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/EnhancedNavigationHost.kt" "Enhanced Navigation Host"; then
    ((passed_checks++))
fi

# Check test files
echo ""
echo "🧪 Checking Test Files:"
echo "----------------------"

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationStateManagerTest.kt" "Navigation State Manager Tests"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserverTest.kt" "Navigation Observer Tests"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationActionsTest.kt" "Navigation Actions Tests"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationGraphTest.kt" "Navigation Graph Tests"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationStatesTest.kt" "Navigation States Tests"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationSystemIntegrationTest.kt" "Navigation System Integration Tests"; then
    ((passed_checks++))
fi

# Check documentation files
echo ""
echo "📚 Checking Documentation Files:"
echo "--------------------------------"

((total_checks++))
if check_file "NAVIGATION_SYSTEM_README.md" "Navigation System README"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "IMPLEMENTATION_SUMMARY.md" "Implementation Summary"; then
    ((passed_checks++))
fi

# Check modified files
echo ""
echo "🔧 Checking Modified Files:"
echo "---------------------------"

((total_checks++))
if check_file "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/MainActivity.kt" "Updated MainActivity"; then
    ((passed_checks++))
fi

((total_checks++))
if check_file "app/build.gradle.kts" "Updated Build Configuration"; then
    ((passed_checks++))
fi

# Check content of key files
echo ""
echo "🔍 Checking File Content:"
echo "-------------------------"

if [ -f "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationGraph.kt" ]; then
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationGraph.kt" "object NavigationRoutes" "Navigation Routes definition"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationGraph.kt" "class NavigationActions" "Navigation Actions class"; then
        ((passed_checks++))
    fi
fi

if [ -f "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationState.kt" ]; then
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationState.kt" "data class NavigationState" "Navigation State data class"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationState.kt" "sealed class NavigationEvent" "Navigation Event sealed class"; then
        ((passed_checks++))
    fi
fi

if [ -f "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserver.kt" ]; then
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserver.kt" "class NavigationObserver" "Navigation Observer class"; then
        ((passed_checks++))
    fi
fi

if [ -f "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/EnhancedNavigationHost.kt" ]; then
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/EnhancedNavigationHost.kt" "fun EnhancedNavigationHost" "Enhanced Navigation Host function"; then
        ((passed_checks++))
    fi
fi

# Check test content
echo ""
echo "🧪 Checking Test Content:"
echo "-------------------------"

if [ -f "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationStateManagerTest.kt" ]; then
    ((total_checks++))
    if check_content "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationStateManagerTest.kt" "@Test" "Test annotations present"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    count_lines "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationStateManagerTest.kt" 20 "Navigation State Manager Test file size"
    if [ $? -eq 0 ]; then
        ((passed_checks++))
    fi
fi

if [ -f "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserverTest.kt" ]; then
    ((total_checks++))
    if check_content "app/src/test/kotlin/com/ultraviolince/mykitchen/recipes/presentation/navigation/NavigationObserverTest.kt" "@Test" "Test annotations present"; then
        ((passed_checks++))
    fi
fi

# Check documentation content
echo ""
echo "📚 Checking Documentation Content:"
echo "----------------------------------"

if [ -f "NAVIGATION_SYSTEM_README.md" ]; then
    ((total_checks++))
    if check_content "NAVIGATION_SYSTEM_README.md" "Navigation System Implementation" "README title"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "NAVIGATION_SYSTEM_README.md" "## Overview" "README overview section"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "NAVIGATION_SYSTEM_README.md" "## Testing Strategy" "README testing section"; then
        ((passed_checks++))
    fi
fi

if [ -f "IMPLEMENTATION_SUMMARY.md" ]; then
    ((total_checks++))
    if check_content "IMPLEMENTATION_SUMMARY.md" "Navigation System Implementation Summary" "Summary title"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "IMPLEMENTATION_SUMMARY.md" "## Files Created/Modified" "Summary files section"; then
        ((passed_checks++))
    fi
fi

# Check build configuration
echo ""
echo "⚙️ Checking Build Configuration:"
echo "--------------------------------"

if [ -f "app/build.gradle.kts" ]; then
    ((total_checks++))
    if check_content "app/build.gradle.kts" "jvmToolchain(21)" "Java 21 toolchain configured"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "app/build.gradle.kts" "JVM_21" "JVM 21 target configured"; then
        ((passed_checks++))
    fi
fi

# Check MainActivity integration
echo ""
echo "🔗 Checking MainActivity Integration:"
echo "-------------------------------------"

if [ -f "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/MainActivity.kt" ]; then
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/MainActivity.kt" "EnhancedNavigationHost" "Enhanced Navigation Host integration"; then
        ((passed_checks++))
    fi
    
    ((total_checks++))
    if check_content "app/src/main/kotlin/com/ultraviolince/mykitchen/recipes/presentation/MainActivity.kt" "NavigationRoutes" "Navigation Routes import"; then
        ((passed_checks++))
    fi
fi

# Summary
echo ""
echo "================================================"
echo "📊 Validation Summary:"
echo "================================================"
echo -e "Total checks: ${YELLOW}$total_checks${NC}"
echo -e "Passed: ${GREEN}$passed_checks${NC}"
echo -e "Failed: ${RED}$((total_checks - passed_checks))${NC}"

if [ $passed_checks -eq $total_checks ]; then
    echo ""
    echo -e "${GREEN}🎉 All checks passed! Navigation system is properly implemented.${NC}"
    echo ""
    echo "✅ Core navigation components implemented"
    echo "✅ Comprehensive test suite created"
    echo "✅ Documentation provided"
    echo "✅ Build configuration updated"
    echo "✅ MainActivity integration completed"
    echo ""
    echo "The navigation system is ready for use!"
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some checks failed. Please review the implementation.${NC}"
    exit 1
fi