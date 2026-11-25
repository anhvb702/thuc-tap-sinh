package com.loitu.yourrefri.reminder

import android.graphics.Bitmap

internal data class ReminderCardModel(
    val badge: String,
    val hero: String,
    val subtitle: String,
)

internal const val REMINDER_ACTION_LABEL = "Xem món & nấu ngay"

internal data class ReminderBigPictureContent(
    val contentTitle: String,
    val contentText: String,
    val hero: String,
    val subtitle: String,
    val actionLabel: String = REMINDER_ACTION_LABEL,
)

private const val URGENT_PHRASE = "nên dùng trước"
private const val DEFAULT_HERO = "Tối nay nhà mình ăn gì?"
private const val DEFAULT_SUBTITLE = "Mở xem gợi ý từ tủ lạnh nhà mình"
private const val URGENT_SUBTITLE = "Nên dùng trước · mở xem gợi ý"
private val whitespace = Regex("\\s+")
private val otherItems = Regex(" và (\\d+) món khác\\b", RegexOption.IGNORE_CASE)
private val extraItems = Regex("(?:\\b(\\d+)\\s+món\\s+khác\\b|\\+(\\d+)\\s+món\\b)", RegexOption.IGNORE_CASE)
private val bannedWords = Regex("hết hạn|lãng phí|bỏ lỡ|bạn quên|streak|tồn kho", RegexOption.IGNORE_CASE) // lint-copy: rule
private val dishTitle = Regex("^(?:Tối nay nấu |Gợi ý bữa cơm: )(.+)$", RegexOption.IGNORE_CASE)

internal fun buildReminderBigPictureContent(title: String, body: String): ReminderBigPictureContent {
    val card = buildReminderCardModel(title, body)
    val titleSuffix = if (card.badge.contains("Nên dùng sớm")) "Nên dùng sớm" else "Gợi ý hôm nay"
    val extraCount = extraItems.find(body)?.destructured?.let { (count, plusCount) ->
        count.ifEmpty { plusCount }.toIntOrNull()
    } ?: 0
    val contentText = if (extraCount > 0) "+$extraCount món nữa nên dùng sớm"
    else "Mở xem gợi ý món ngon hôm nay"
    return ReminderBigPictureContent("🍲 ${card.hero} · $titleSuffix", contentText, card.hero, card.subtitle)
}

internal fun resolveBigPictureStyle(renderBanner: () -> Bitmap?): Bitmap? =
    runCatching { renderBanner() }.getOrNull()

internal fun buildReminderCardModel(title: String, body: String): ReminderCardModel {
    val cleanTitle = title.replace(whitespace, " ").trim()
    val cleanBody = body.replace(whitespace, " ").trim()
    val urgentStart = cleanBody.indexOf(URGENT_PHRASE, ignoreCase = true)
    val badge = if (urgentStart >= 0) "🍲 Nên dùng sớm" else "🍲 Gợi ý hôm nay"
    val dish = dishTitle.matchEntire(cleanTitle)?.groupValues?.get(1)
    if (dish != null) {
        val name = dish.trimEnd('.', '!', '?')
        val hero = if (name.endsWith(" nhé", ignoreCase = true)) name.dropLast(4) else name
        return reminderCard(badge, hero, 28, "Dễ nấu, ấm bụng cho bữa cơm nhà")
    }

    if (cleanTitle.isEmpty() || cleanTitle.equals("Gợi ý từ tủ nhà mình", ignoreCase = true)) {
        val items = if (urgentStart >= 0) cleanBody.substring(0, urgentStart) else cleanBody
        val more = otherItems.find(items)
        if (more != null) {
            val subtitle = "+${more.groupValues[1]} món nên dùng trước"
            return reminderCard(badge, items.substring(0, more.range.first), 26, subtitle)
        }
        if (urgentStart >= 0) return reminderCard(badge, items, 26, URGENT_SUBTITLE)
        return reminderCard(badge, cleanBody, 28, DEFAULT_SUBTITLE)
    }

    return reminderCard(badge, cleanTitle, 28, DEFAULT_SUBTITLE)
}

private fun reminderCard(badge: String, candidate: String, heroLimit: Int, subtitle: String): ReminderCardModel {
    val hero = candidate.takeUnless(bannedWords::containsMatchIn)
        ?.let { text ->
            if (text.length <= heroLimit || text[heroLimit] == ' ') text.take(heroLimit)
            else text.take(heroLimit).substringBeforeLast(' ', missingDelimiterValue = "")
        }
        ?.trim(' ', ',', ':', '-', '–', '—')
        .orEmpty()
        .ifEmpty { DEFAULT_HERO }
    val safeSubtitle = when {
        subtitle.length > 46 -> URGENT_SUBTITLE
        subtitle.equals(hero, ignoreCase = true) -> DEFAULT_SUBTITLE
        else -> subtitle
    }
    return ReminderCardModel(badge, hero, safeSubtitle)
}
