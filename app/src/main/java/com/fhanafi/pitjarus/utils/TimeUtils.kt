package com.fhanafi.pitjarus.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isoTimestampNow(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())
}
