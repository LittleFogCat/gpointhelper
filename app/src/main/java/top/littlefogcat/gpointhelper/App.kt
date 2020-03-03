package top.littlefogcat.gpointhelper

import android.app.Application

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun getInstance(): App {
            return instance
        }
    }

    init {
        instance = this
    }
}