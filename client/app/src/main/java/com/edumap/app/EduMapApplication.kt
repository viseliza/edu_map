package com.edumap.app

import android.app.Application
import com.edumap.app.data.SessionStorage

class EduMapApplication : Application() {
    val sessionStorage: SessionStorage by lazy { SessionStorage(this) }
}
