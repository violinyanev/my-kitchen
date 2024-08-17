import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Logger {
    actual fun init() {
        Napier.base(DebugAntilog())
    }
}
