package com.loitu.yourrefri.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Bộ icon nét một màu (24dp, nét 1.8). Màu thật do `Icon(tint = ...)` quyết định.
private fun strokeIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { data ->
            addPath(
                pathData = addPathNodes(data),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object AppIcons {
    val Home by lazy { strokeIcon("Home", "M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z") }
    val Fridge by lazy {
        strokeIcon(
            "Fridge",
            "M8.5 2.5h7A2.5 2.5 0 0 1 18 5v14a2.5 2.5 0 0 1-2.5 2.5h-7A2.5 2.5 0 0 1 6 19V5a2.5 2.5 0 0 1 2.5-2.5z",
            "M6 10h12", "M9 6v1.5", "M9 13v3",
        )
    }
    val Pot by lazy {
        strokeIcon(
            "Pot",
            "M4 10h16v6a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4z", "M2 10h20",
            "M9 6c0-1.5 1-1.5 1-3", "M14 6c0-1.5 1-1.5 1-3",
        )
    }
    val User by lazy { strokeIcon("User", "M12 4a4 4 0 1 0 0 8 4 4 0 1 0 0-8z", "M4 21c0-4 4-6 8-6s8 2 8 6") }
    val Plus by lazy { strokeIcon("Plus", "M12 5v14", "M5 12h14") }
    val Minus by lazy { strokeIcon("Minus", "M5 12h14") }
    val Clock by lazy { strokeIcon("Clock", "M12 3a9 9 0 1 0 0 18 9 9 0 1 0 0-18z", "M12 7v5l3 2") }
    val Check by lazy { strokeIcon("Check", "M5 12l5 5 9-10") }
    val Back by lazy { strokeIcon("Back", "M15 5l-7 7 7 7") }
    val Chevron by lazy { strokeIcon("Chevron", "M9 5l7 7-7 7") }
    val Bell by lazy { strokeIcon("Bell", "M6 17V11a6 6 0 0 1 12 0v6l1.5 2h-15z", "M10 21h4") }
    val Lock by lazy {
        strokeIcon("Lock", "M7 11h10a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2z", "M8 11V8a4 4 0 0 1 8 0v3")
    }
    val Save by lazy { strokeIcon("Save", "M12 4v11", "M7 10l5 5 5-5", "M5 20h14") }
    val Load by lazy { strokeIcon("Load", "M12 16V5", "M7 10l5-5 5 5", "M5 20h14") }
    val Calendar by lazy {
        strokeIcon("Calendar", "M6 5h12a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z", "M4 10h16", "M9 3v4", "M15 3v4")
    }
    val Moon by lazy { strokeIcon("Moon", "M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z") }

    // Icon cho bố cục mới (tìm kiếm, nhập món, yêu thích, nấu rảnh tay, cài đặt khẩu vị).
    val Search by lazy { strokeIcon("Search", "M11 4a7 7 0 1 0 0 14 7 7 0 1 0 0-14z", "M20 20l-4-4") }
    val Mic by lazy { strokeIcon("Mic", "M12 3a3 3 0 0 0-3 3v6a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3z", "M6 11a6 6 0 0 0 12 0", "M12 17v4") }
    val Keyboard by lazy {
        strokeIcon("Keyboard", "M4 7h16a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z", "M7 11h.01", "M10 11h.01", "M13 11h.01", "M16 11h.01", "M8 14h8")
    }
    val Heart by lazy { strokeIcon("Heart", "M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z") }
    val Snowflake by lazy { strokeIcon("Snowflake", "M12 3v18", "M4.5 7.5l15 9", "M19.5 7.5l-15 9", "M9.5 4.5L12 6.5l2.5-2", "M9.5 19.5L12 17.5l2.5 2") }
    val Cart by lazy { strokeIcon("Cart", "M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h8.4a1 1 0 0 0 1-.8L20 8H6", "M9 19.5h.01", "M17 19.5h.01") }
    val ChefHat by lazy { strokeIcon("ChefHat", "M7 14a4 4 0 1 1 1-7.9A4 4 0 0 1 16 6a4 4 0 1 1 1 8v5H7z", "M7 17h10") }
    val Info by lazy { strokeIcon("Info", "M12 3a9 9 0 1 0 0 18 9 9 0 1 0 0-18z", "M12 11v5", "M12 8h.01") }
    val Sliders by lazy { strokeIcon("Sliders", "M4 7h10", "M18 7h2", "M4 17h2", "M10 17h10", "M14 5v4", "M6 15v4") }
    val Utensils by lazy { strokeIcon("Utensils", "M7 3v8", "M5 3v5a2 2 0 0 0 4 0V3", "M7 11v10", "M17 3c-2 2-2 6 0 8v10") }
    val Close by lazy { strokeIcon("Close", "M6 6l12 12", "M18 6L6 18") }

    // Icon thực phẩm theo nhóm.
    val Tomato by lazy { strokeIcon("Tomato", "M12 6a7.5 7.5 0 1 0 0 15 7.5 7.5 0 1 0 0-15z", "M8.5 6.5c2 1 5 1 7 0", "M12 6V3.5") }
    val Egg by lazy { strokeIcon("Egg", "M12 3c4 0 7 6.5 7 11a7 7 0 0 1-14 0c0-4.5 3-11 7-11z") }
    // Miếng thịt có đốm mỡ ở giữa; tránh hình chân gà vì dễ nhầm với biểu tượng tìm kiếm.
    val Meat by lazy {
        strokeIcon("Meat", "M4 10c0-3.5 3.5-6 8-6s8 2.5 8 6-1.5 6.5-4 8.5-9 2-11-.5S4 13.5 4 10z", "M14.5 11.5h.01")
    }
    val Fish by lazy { strokeIcon("Fish", "M3 12c3-5 9-6 14-2l4-3v10l-4-3c-5 4-11 3-14-2z", "M8 11.5h.01") }
    val Milk by lazy { strokeIcon("Milk", "M8 3h8l2 4v14H6V7z", "M6 7h12") }
    val Leaf by lazy { strokeIcon("Leaf", "M5 19c0-8 5-14 15-14 0 10-6 15-14 15", "M5 19l8-8") }
    val Tofu by lazy { strokeIcon("Tofu", "M4 9l8-4 8 4v8l-8 4-8-4z", "M4 9l8 4 8-4", "M12 13v8") }
}
