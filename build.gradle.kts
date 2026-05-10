// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.android.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

detekt {
    config.setFrom(files("config/detekt.yml"))
    autoCorrect = true
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(
        files(
            subprojects.map { "${it.projectDir}/src/main/kotlin" } +
                    subprojects.map { "${it.projectDir}/src/main/java" }
        )
    )
}
