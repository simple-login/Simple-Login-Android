package io.simplelogin.android.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowWidthSizeClass

class TwoPaneScene<T: NavKey>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>
): Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry)

    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.4f)) {
                firstEntry.Content()
            }

            VerticalDivider()

            Column(modifier = Modifier.weight(0.6f)) {
                secondEntry.Content()
            }
        }
    }

    companion object {
        const val TWO_PANE_KEY = "TwoPane"

        fun twoPane() = mapOf(TWO_PANE_KEY to true)
    }
}

class TwoPaneSceneStrategy<T: NavKey>: SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val windowWidthSizeClass = windowSizeClass.windowWidthSizeClass

        if (windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
            windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            val last2Entries = entries.takeLast(2)
            // Only return a Scene if there are two entries, and both have declared
            // they can be displayed in a two pane scene.
            return if (last2Entries.size == 2 &&
                last2Entries.all { it.metadata.containsKey(TwoPaneScene.TWO_PANE_KEY) }) {
                val firstEntry = last2Entries.first()
                val secondEntry = last2Entries.last()

                // The scene key must uniquely represent the state of the scene.
                val sceneKey = Pair(firstEntry.contentKey, secondEntry.contentKey)

                TwoPaneScene(
                    key = sceneKey,
                    previousEntries = entries.dropLast(1),
                    firstEntry = firstEntry,
                    secondEntry = secondEntry
                )
            } else {
                null
            }
        }

        return null
    }
}
