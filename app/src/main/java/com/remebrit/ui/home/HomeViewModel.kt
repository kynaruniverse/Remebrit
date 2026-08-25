package com.remebrit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ItemRepository) : ViewModel() {

    val items: StateFlow<List<RemebritItem>> = repository.activeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun capture(text: String) {
        viewModelScope.launch { repository.capture(text) }
    }

    fun complete(item: RemebritItem) {
        viewModelScope.launch { repository.complete(item) }
    }
}