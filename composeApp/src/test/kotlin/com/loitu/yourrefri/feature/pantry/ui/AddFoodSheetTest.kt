package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddFoodSheetTest {
    @Test
    fun luoiBaCotOCoChuThuong() {
        assertEquals(3, quickAddGridColumns(1.0f))
        assertEquals(3, quickAddGridColumns(1.29f))
    }

    @Test
    fun luoiGiamConHaiCotTuCoChu1Phay3() {
        assertEquals(2, quickAddGridColumns(1.3f))
        assertEquals(2, quickAddGridColumns(2.0f))
    }

    @Test
    fun moTaOChonNeuTenMonVaTrangThai() {
        assertEquals("Thịt bò, đã chọn", quickAddTileDescription("Thịt bò", selected = true))
        assertEquals("Thịt bò, chưa chọn", quickAddTileDescription("Thịt bò", selected = false))
    }

    @Test
    fun chuCuaSheetAmVaNgan() {
        assertEquals("Hôm nay mua gì về?", AddFoodSheetCopy.title)
        assertEquals("Chạm để chọn, hạn dùng ước tính sẽ tự điền.", AddFoodSheetCopy.subtitle)
        assertEquals("Hạn dùng ước tính", AddFoodSheetCopy.estimateHeader)
        assertEquals("Đóng", AddFoodSheetCopy.closeLabel)
        assertEquals("Thêm món", AddFoodSheetCopy.fabLabel)
        assertEquals("Món này đã có trong tủ rồi.", AddFoodSheetCopy.alreadyPresent)
    }

    @Test
    fun chuCuaSheetKhongChuaTuCam() {
        val all = listOf(
            AddFoodSheetCopy.title, AddFoodSheetCopy.subtitle, AddFoodSheetCopy.estimateHeader,
            AddFoodSheetCopy.closeLabel, AddFoodSheetCopy.fabLabel, AddFoodSheetCopy.searchPlaceholder,
        ).joinToString(" ").lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { assertFalse(all.contains(it), it) }
    }

    @Test
    fun nutChinhDungNhanTheoSoMon() {
        assertEquals("Thêm 3 món vào tủ", quickAddConfirmLabel(3))
        assertTrue(quickAddConfirmLabel(0).isNotBlank())
    }
}
