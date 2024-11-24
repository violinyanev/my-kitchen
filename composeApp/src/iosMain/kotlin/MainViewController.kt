import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() =
    ComposeUIViewController(
        configure = {
            KoinInitializer().init()
            Logger.init()
        },
    ) {
        App()
    }
