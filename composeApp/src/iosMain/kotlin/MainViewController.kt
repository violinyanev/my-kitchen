import androidx.compose.ui.window.ComposeUIViewController

fun mainViewController() =
    ComposeUIViewController(
        configure = {
            KoinInitializer().init()
            Logger.init()
        },
    ) {
        App()
    }
