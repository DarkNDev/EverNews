package com.darkndev.evernews.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.evernews.data.EverNewsRepository
import com.darkndev.evernews.models.BookmarkedArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: EverNewsRepository
) : ViewModel() {

    fun unBookmarkAll() = viewModelScope.launch {
        repository.bookmarkDao.unBookmarkAllArticles()
    }

    fun unBookmark(article: BookmarkedArticle) = viewModelScope.launch {
        repository.bookmarkDao.unBookmarkArticle(article.url)
        channel.send(Event.ShowUndoMessage(article))
    }

    fun undoClicked(article: BookmarkedArticle) = viewModelScope.launch {
        repository.bookmarkDao.bookmarkArticle(article)
    }

    val items = repository.getBookmarkedItems().asLiveData()

    private val channel = Channel<Event>()
    val event = channel.receiveAsFlow()

    sealed class Event {
        data class ShowUndoMessage(val article: BookmarkedArticle) : Event()
    }
}