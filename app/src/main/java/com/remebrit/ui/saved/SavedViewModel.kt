package com.remebrit.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SavedViewModel(repository: ItemRepository) : ViewModel() {
    val savedItems: StateFlow<List<RemebritItem>> = repository.savedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}