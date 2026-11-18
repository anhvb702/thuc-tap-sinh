package com.loitu.yourrefri.reminder

/**
 * Ảnh món cho popup nhắc ăn uống. Mỗi ảnh gắn với một công thức thật trong recipes.json,
 * để bấm vào popup mở đúng món. Thông báo chưa gắn với một món theo nguyên liệu, nên món luân phiên theo ngày.
 */
object ReminderPicture {
    data class Entry(val recipeId: String, val assetPath: String)

    val ENTRIES: List<Entry> = listOf(
        Entry("thit-kho-trung", "notify/thit-kho-trung.jpg"),
        Entry("canh-rau-ngot-thit-bam", "notify/canh-rau-ngot-thit-bam.jpg"),
        Entry("dau-phu-sot-ca-chua", "notify/dau-phu-sot-ca-chua.jpg"),
    )

    /** day: số ngày trong năm (1..366); cùng ngày luôn ra cùng món. */
    fun entryFor(day: Int): Entry = ENTRIES[Math.floorMod(day - 1, ENTRIES.size)]

    fun assetFor(day: Int): String = entryFor(day).assetPath
}
