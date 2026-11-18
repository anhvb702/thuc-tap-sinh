package com.loitu.yourrefri.feature.recipe.ui

/** Dòng tiến độ đi chợ theo Figma 06: "1/5 món đã mua · Chạm để tick hoặc bỏ tick." */
internal fun shoppingProgressLabel(bought: Int, total: Int): String =
    "$bought/$total món đã mua · Chạm để tick hoặc bỏ tick."
