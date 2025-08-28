pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "BeAlpha_"
include(":app")
include(":common:utils")
include(":home:home_data")
include(":create:create_data")
include(":goal:goal_data")
include(":create:create_domain")
include(":create:create_ui")
include(":goal:goal_domain")
include(":goal:goal_ui")
include(":profile:profile_domain")
include(":profile:profile_data")
include(":profile:profile_ui")
include(":home:home_domain")
include(":home:home_ui")
include(":authentication")
include(":common:ui")
include(":notification_data")
include(":notification:notification_data")
include(":notification:notification_ui")
include(":notification:notification_domain")
include(":ai_agent:ai_data")
include(":ai_agent:ai_domain")
include(":ai_agent:ai_ui")
include(":onboarding:onboarding_data")
include(":onboarding:onboarding_domain")
include(":onboarding:onboarding_ui")
