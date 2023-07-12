package com.darkndev.evernews.ui.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.evernews.data.EverNewsRepository
import com.darkndev.evernews.models.Article
import com.darkndev.evernews.utils.toBookmarkArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    state: SavedStateHandle,
    private val repository: EverNewsRepository
) : ViewModel() {

    val article = state.get<Article>("ARTICLE")!!
    val isBookmarked = repository.bookmarkDao.isBookmarked(article.url).asLiveData()

    fun bookmarkClicked() = viewModelScope.launch {
        if (isBookmarked.value!!) {
            repository.bookmarkDao.unBookmarkArticle(article.url)
        } else {
            repository.bookmarkDao.bookmarkArticle(article.toBookmarkArticle())
        }
    }
}