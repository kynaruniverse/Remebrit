package com.remebrit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import com.remebrit.domain.lifecycle.Section
import com.remebrit.domain.lifecycle.Sectioner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeSections(
    val now: List<RemebritItem> = emptyList(),
    val today: List<RemebritItem> = emptyList(),
    val inbox: List<RemebritItem> = emptyList()
)

class HomeViewModel(private val repository: ItemRepository) : ViewModel() {

    val sections: StateFlow<HomeSections> = repository.activeItems()
        .map { items ->
            val now = System.currentTimeMillis()
            val grouped = items.groupBy { Sectioner.sectionFor(it.relevantAt, now) }
            HomeSections(
                now = grouped[Section.NOW].orEmpty(),
                today = grouped[Section.TODAY].orEmpty(),
                inbox = grouped[Section.INBOX].orEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeSections())

    fun capture(text: String) {
        viewModelScope.launch { repository.capture(text) }
    }
}