package com.impl.recoder

import android.app.Application

/**
 *
 * @Author： Dong Lindong
 * @Time： 2021/8/31 0031 10:53
 * @description:
 *
 */
open class App: Application() {
	companion object {
		lateinit var instance: App

		fun get(): App {
			return instance
		}
	}
	override fun onCreate() {
		super.onCreate()
		instance = this
	}
}