@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.cooklog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.cooklog.data.CookLogRepository
import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeek
import com.loitu.yourrefri.feature.cooklog.logic.groupCookHistory
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Lịch sử nấu đã gom theo tuần; [loaded] false cho tới khi đọc xong lần đầu. */
data class CookHistoryState(
    val weeks: List<CookHistoryWeek> = emptyList(),
    val loaded: Boolean = false,
)

sealed interface CookHistoryAction {
    data class Delete(val id: Long) : CookHistoryAction
}

sealed interface CookHistoryEffect {
    data object SaveFailed : CookHistoryEffect
    data object LoadFailed : CookHistoryEffect
}

class CookHistoryViewModel(
    private val repository: CookLogRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val workerScope = scope ?: viewModelScope
    private val mutableState = MutableStateFlow(CookHistoryState())
    private val mutableEffects = Channel<CookHistoryEffect>(Channel.BUFFERED)
    private var collectJob: Job? = null

    val state: StateFlow<CookHistoryState> = mutableState.asStateFlow()
    val effects = mutableEffects.receiveAsFlow()

    init {
        startCollecting()
    }

    fun dispatch(action: CookHistoryAction) {
        when (action) {
            is CookHistoryAction.Delete -> workerScope.launch {
                try {
                    repository.delete(action.id)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    mutableEffects.send(CookHistoryEffect.SaveFailed)
                }
            }
        }
    }

    /** Đọc lại từ DB, dùng sau khi khôi phục sao lưu. */
    fun reload() {
        startCollecting()
    }

    /**
     * Ghi một lần nấu xong, kể cả khi không có nguyên liệu nào được trừ. [onResult] nhận id bản ghi
     * (để Hoàn tác xóa đúng bản ghi này) hoặc null khi không ghi được.
     * Ruling: dùng callback thay vì Effect vì Hoàn tác cần id ngay sau khi ghi.
     */
    fun record(recipeId: String, savedIngredientIds: List<String>, meal: MealSlot? = null, onResult: (Long?) -> Unit) {
        if (recipeId.isBlank()) {
            onResult(null)
            return
        }
        workerScope.launch {
            val id = try {
                val today = clock.now().toLocalDateTime(timeZone).date
                repository.add(recipeId, today, savedIngredientIds, meal)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableEffects.send(CookHistoryEffect.SaveFailed)
                null
            }
            onResult(id)
        }
    }

    private fun startCollecting() {
        collectJob?.cancel()
        collectJob = workerScope.launch {
            try {
                repository.observeAll().collect { logs ->
                    // Ngày "hôm nay" tính lúc dữ liệu đổi; mở lại màn là tính lại nên đủ cho danh sách.
                    val today = clock.now().toLocalDateTime(timeZone).date
                    mutableState.value = CookHistoryState(weeks = groupCookHistory(logs, today), loaded = true)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(loaded = true)
                mutableEffects.send(CookHistoryEffect.LoadFailed)
            }
        }
    }
}
