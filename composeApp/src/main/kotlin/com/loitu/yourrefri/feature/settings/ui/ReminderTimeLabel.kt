package com.loitu.yourrefri.feature.settings.ui

/** Giờ nhắc dạng HH:mm, hai chữ số cho cả giờ và phút. */
internal fun reminderTimeLabel(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
