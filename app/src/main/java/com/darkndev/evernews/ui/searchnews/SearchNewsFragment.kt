package com.darkndev.evernews.ui.searchnews

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.FragmentSearchNewsBinding
import com.darkndev.evernews.recyclerview.adapters.ArticleLoadStateAdapter
import com.darkndev.evernews.recyclerview.adapters.ArticlePagingAdapter
import com.darkndev.evernews.recyclerview.decoration.ItemOffsetDecoration
import com.darkndev.evernews.utils.errorMessage
import com.darkndev.evernews.utils.onQueryTextSubmit
import com.darkndev.evernews.utils.showIfOrInvisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news), MenuProvider {

    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var articleAdapter: ArticlePagingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSearchNewsBinding.bind(view)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            this@SearchNewsFragment,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        articleAdapter = ArticlePagingAdapter {
            val action = SearchNewsFragmentDirections.actionSearchNewsFragmentToArticleFragment(it)
            findNavController().navigate(action)
        }

        binding.apply {
            recyclerView.apply {
                adapter = articleAdapter.withLoadStateFooter(
                    ArticleLoadStateAdapter(articleAdapter::retry)
                )
                addItemDecoration(ItemOffsetDecoration(4))
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.searchResults.collectLatest { data ->
                    articleAdapter.submitData(data)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                    if (!hasCurrentQuery) {
                        recyclerView.isVisible = false
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                articleAdapter.loadStateFlow
                    .distinctUntilChangedBy { it.source.refresh }
                    .filter { it.source.refresh is LoadState.NotLoading }
                    .collect {
                        if (viewModel.pendingScrollToTopAfterNewQuery) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterNewQuery = false
                        }
                        if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                articleAdapter.loadStateFlow
                    .collect { loadState ->
                        when (val refresh = loadState.mediator?.refresh!!) {
                            is LoadState.Loading -> {
                                retry.isVisible = false
                                progressBar.show()
                                textViewEmpty.isVisible = false

                                recyclerView.showIfOrInvisible {
                                    !viewModel.newQueryInProgress && articleAdapter.itemCount > 0
                                }

                                viewModel.refreshInProgress = true
                                viewModel.pendingScrollToTopAfterRefresh = true
                            }

                            is LoadState.NotLoading -> {
                                retry.isVisible = false
                                progressBar.hide()
                                recyclerView.isVisible = articleAdapter.itemCount > 0

                                val noResults =
                                    articleAdapter.itemCount < 1 && loadState.append.endOfPaginationReached
                                            && loadState.source.append.endOfPaginationReached

                                textViewEmpty.isVisible = noResults

                                viewModel.refreshInProgress = false
                                viewModel.newQueryInProgress = false
                            }

                            is LoadState.Error -> {
                                progressBar.hide()
                                textViewEmpty.isVisible = false
                                recyclerView.isVisible = articleAdapter.itemCount > 0

                                val noCachedResults =
                                    articleAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                                retry.isVisible = noCachedResults

                                Toast.makeText(
                                    context,
                                    errorMessage { refresh.error },
                                    Toast.LENGTH_SHORT
                                ).show()


                                viewModel.refreshInProgress = false
                                viewModel.newQueryInProgress = false
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                    }
            }

            retry.setOnClickListener {
                articleAdapter.retry()
            }

            progressBar.hide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextSubmit {
            viewModel.onSearchQuerySubmit(it)
            searchView.clearFocus()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.action_refresh -> {
            articleAdapter.refresh()
            true
        }

        else -> false
    }
}