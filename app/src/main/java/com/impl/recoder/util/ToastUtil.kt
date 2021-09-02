package com.impl.recoder.util

import android.widget.Toast
import com.impl.recoder.App

/**
 *
 * @Author： Dong Lindong
 * @Time： 2021/8/31 0031 11:12
 * @description:
 *
 */
fun toastShortMessage(msg: String) {
	Toast.makeText(App.instance, msg, Toast.LENGTH_SHORT).show()
}

fun toastLongMessage(msg: String) {
	Toast.makeText(App.instance, msg, Toast.LENGTH_LONG).show()
}
