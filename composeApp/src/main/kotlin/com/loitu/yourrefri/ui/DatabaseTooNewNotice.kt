package com.loitu.yourrefri.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppTheme

internal object DatabaseTooNewCopy {
    const val title = "Cần cập nhật ứng dụng"
    const val body = "Dữ liệu trên máy được lưu bởi phiên bản mới hơn. Hãy cập nhật ứng dụng để xem; dữ liệu vẫn được giữ nguyên."
}

/** Thay màn chính khi DB mới hơn app: không crash, không đụng dữ liệu. */
@Composable
internal fun DatabaseTooNewNotice() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = DatabaseTooNewCopy.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
        Text(
            text = DatabaseTooNewCopy.body,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.muted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
