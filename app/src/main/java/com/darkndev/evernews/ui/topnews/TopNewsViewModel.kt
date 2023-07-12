package com.darkndev.evernews.ui.topnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkndev.evernews.data.EverNewsRepository
import com.darkndev.evernews.utils.Category
import com.darkndev.evernews.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopNewsViewModel @Inject constructor(
    private val repository: EverNewsRepository
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Refresh>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    val category = MutableStateFlow(Category.GENERAL.category.lowercase())

    //var scrollToTopAfterRefresh = false

    val topNews = combine(category, refreshTrigger) { category, refreshTrigger ->
        Pair(category, refreshTrigger)
    }.flatMapLatest { (category, refresh) ->
        repository.getTopNews(category, refresh == Refresh.FORCE,
            onFetchFailed = { throwable ->
                viewModelScope.launch { eventChannel.send(Event.ShowErrorMessage(throwable)) }
            },
            onFetchSuccess = {
                //println("TAG: FETCHING FROM INTERNET SUCCESS")
                //scrollToTopAfterRefresh = true
                viewModelScope.launch { refreshTriggerChannel.send(Refresh.NORMAL) }
            })
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onStart() {
        if (topNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (topNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}