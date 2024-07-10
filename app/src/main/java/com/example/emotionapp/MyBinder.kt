//Davide Reverberi E-MotionApp

package com.example.emotionapp

import android.os.Binder

/**
 *
 * MyBinder is a class that extends the Binder class and is used to provide access to a WebService instance.
 *
 * **/
class MyBinder(val s: WebService): Binder() {
    fun getService(): WebService {
        return s
    }
}
