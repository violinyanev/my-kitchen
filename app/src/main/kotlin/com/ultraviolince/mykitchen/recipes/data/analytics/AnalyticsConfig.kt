package com.ultraviolince.mykitchen.recipes.data.analytics

/**
 * Configuration for Countly analytics integration.
 * Contains default values and configuration constants.
 */
object AnalyticsConfig {

    /**
     * Default Countly server URL for development/testing.
     * In production, this should be overridden with your own Countly server.
     */
    const val DEFAULT_SERVER_URL = "https://countly.example.com"

    /**
     * Default app key for development/testing.
     * In production, this should be replaced with your actual app key.
     */
    const val DEFAULT_APP_KEY = "your-app-key"

    /**
     * Whether analytics is enabled. Can be controlled via build config or runtime.
     */
    var isEnabled: Boolean = true

    /**
     * Event names for consistent tracking across the app.
     */
    object Events {
        const val APP_STARTED = "app_started"
        const val RECIPE_OPERATION = "recipe_operation"
        const val AUTH_OPERATION = "auth_operation"
        const val NAVIGATION = "navigation"
    }

    /**
     * Common segmentation keys.
     */
    object Segmentation {
        const val ACTION = "action"
        const val SCREEN = "screen"
        const val SUCCESS = "success"
        const val RECIPE_ID = "recipe_id"
    }

    /**
     * Recipe operation actions.
     */
    object RecipeActions {
        const val CREATE = "create"
        const val READ = "read"
        const val UPDATE = "update"
        const val DELETE = "delete"
        const val LIST = "list"
    }

    /**
     * Authentication actions.
     */
    object AuthActions {
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val LOGIN_ATTEMPT = "login_attempt"
    }

    /**
     * Screen names for navigation tracking.
     */
    object Screens {
        const val RECIPE_LIST = "recipe_list"
        const val RECIPE_DETAIL = "recipe_detail"
        const val RECIPE_EDIT = "recipe_edit"
        const val LOGIN = "login"
    }
}
