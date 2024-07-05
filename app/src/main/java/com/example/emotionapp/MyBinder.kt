package com.example.emotionapp

import android.os.Binder

class MyBinder(val s: WebService): Binder() {
    fun getService(): WebService {
        return s
    }
}
