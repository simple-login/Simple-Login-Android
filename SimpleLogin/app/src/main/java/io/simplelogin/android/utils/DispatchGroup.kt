package io.simplelogin.android.utils

class DispatchGroup {
    private var count = 0
    private var runnable: (() -> Unit)? = null

    init {
        count = 0
    }

    @Synchronized
    fun enter() {
        count++
    }

    @Synchronized
    fun leave() {
        count--
        notifyGroup()
    }

    fun notify(r: () -> Unit) {
        runnable = r
        notifyGroup()
    }

    private fun notifyGroup() {
        if (count <= 0 && runnable != null) {
            runnable?.let { it() }
        }
    }
}
