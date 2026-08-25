package com.remebrit.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class SearchViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<RemebritItem>> = _query
        .debounce(150)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(text: String) { _query.value = text }
}