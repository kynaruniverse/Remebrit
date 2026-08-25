package com.remebrit.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ItemDetailViewModel(
    private val repository: ItemRepository,
    itemId: Long
) : ViewModel() {

    val item: StateFlow<RemebritItem?> = repository.observeItem(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun complete(item: RemebritItem) = viewModelScope.launch { repository.complete(item) }
    fun keep(item: RemebritItem) = viewModelScope.launch { repository.keep(item) }
    fun archive(item: RemebritItem) = viewModelScope.launch { repository.archive(item) }
    fun delete(item: RemebritItem) = viewModelScope.launch { repository.delete(item) }
    fun snoozeUntilTomorrow(item: RemebritItem) = viewModelScope.launch {
        repository.snooze(item, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))
    }
}