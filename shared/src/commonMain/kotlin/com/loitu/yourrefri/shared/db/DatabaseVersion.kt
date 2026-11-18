package com.loitu.yourrefri.shared.db

/**
 * DB trên máy được tạo bởi bản app mới hơn (schema cao hơn bản đang chạy).
 * Không hạ cấp và không xóa: app chỉ báo cần cập nhật, dữ liệu giữ nguyên.
 */
fun isDatabaseTooNew(dbVersion: Long, appVersion: Long): Boolean = dbVersion > appVersion
