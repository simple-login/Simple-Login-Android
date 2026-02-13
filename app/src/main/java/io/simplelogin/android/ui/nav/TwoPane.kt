package io.simplelogin.android.ui.nav

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import io.simplelogin.android.ui.util.isTwoPaneEligible

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

            Column(modifier = Modifier.weight(secondEntryWeight)) {
                secondEntry.Content()
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

@Composable
fun <T : Any> rememberTwoPaneSceneStrategy(): TwoPaneSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass) {
        TwoPaneSceneStrategy(windowSizeClass)
    }
}

class TwoPaneSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isTwoPaneEligible()) {
            return null
        }

        val permanentEntry =
            entries.firstOrNull { it.metadata.containsKey(TwoPaneScene.PERMANENT_PANE_KEY) }
        val lastDetailEntry =
            entries.lastOrNull { it.metadata.containsKey(TwoPaneScene.DETAIL_PANE_KEY) }
        return if (permanentEntry != null && lastDetailEntry != null) {
            val sceneKey = Pair(permanentEntry.contentKey, lastDetailEntry.contentKey)
            TwoPaneScene(
                key = sceneKey,
                previousEntries = entries.dropLast(1),
                firstEntry = permanentEntry,
                secondEntry = lastDetailEntry
            )
        } else {
            null
        }
    }
}