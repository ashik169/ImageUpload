package com.ashik.imageupload.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {

    private const val UI_DATE_TIME_PATTERN = "dd MMM yyyy hh:mm a"
    fun getUIDateTimeFormat(lastModified: Long): String? {
        return try {
            val simpleDateFormat = SimpleDateFormat(UI_DATE_TIME_PATTERN, Locale.ENGLISH)
            simpleDateFormat.format(Date(lastModified))
        } catch (e: Exception) {
            null
        }
    }
}