package io.simplelogin.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SimpleLoginApp : Application()

const val PAGE_SIZE = 20