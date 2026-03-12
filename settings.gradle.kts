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
    }
}

rootProject.name = "My Applicationkotlin"
include(":app")
include(":imagesmodul")
include(":authactadap")
include(":arrayadapterproject")
include(":pz4")
include(":pz4nointernet")
include(":zachet2")
include(":fragmentlessons")
include(":fragmentlesson1")
include(":botnavmenu")
include(":permissonlessons")
include(":lessondialogs")
include(":mybdsqlite")
include(":spprojectsqlitetask")
