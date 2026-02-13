package io.simplelogin.android.ui.nav

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry)

    override val content: @Composable (() -> Unit) = {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val firstEntryWeight = if (isLandscape) 0.4f else 0.5f
        val secondEntryWeight = 1.0f - firstEntryWeight

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(firstEntryWeight)) {
                firstEntry.Content()
            }

            VerticalDivider()

            CompositionLocalProvider(LocalBackButtonVisible provides false) {
                Column(modifier = Modifier.weight(secondEntryWeight)) {
                    AnimatedContent(
                        targetState = secondEntry,
                        contentKey = { entry -> entry.contentKey },
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { entry ->
                        entry.Content()
                    }
                }
            }
        }
    }

    companion object {
        const val PERMANENT_PANE_KEY = "PermanentPane"
        const val DETAIL_PANE_KEY = "DetailPane"

        fun permanentPane() = mapOf(PERMANENT_PANE_KEY to true)
        fun detailPane() = mapOf(DETAIL_PANE_KEY to true)
    }
}

val LocalBackButtonVisible = compositionLocalOf { true }

@Composable
fun <T : Any> rememberTwoPaneSceneStrategy(): TwoPaneSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass) {
        TwoPaneSceneStrategy(windowSizeClass)
    }
}

class TwoPaneSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val permanentEntry =
            entries.firstOrNull { it.metadata.containsKey(TwoPaneScene.PERMANENT_PANE_KEY) }
        val lastDetailEntry =
            entries.lastOrNull { it.metadata.containsKey(TwoPaneScene.DETAIL_PANE_KEY) }
        return if (permanentEntry != null && lastDetailEntry != null) {
            TwoPaneScene(
                key = permanentEntry.contentKey,
                previousEntries = entries.dropLast(1),
                firstEntry = permanentEntry,
                secondEntry = lastDetailEntry
            )
        } else {
            null
        }
    }
}