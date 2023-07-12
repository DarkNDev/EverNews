package com.darkndev.evernews.ui.topnews

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
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
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.FragmentTopNewsBinding
import com.darkndev.evernews.recyclerview.adapters.ArticleListAdapter
import com.darkndev.evernews.recyclerview.decoration.ItemOffsetDecoration
import com.darkndev.evernews.utils.Category
import com.darkndev.evernews.utils.Resource
import com.darkndev.evernews.utils.errorMessage
import com.darkndev.evernews.utils.onQueryTextChange
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopNewsFragment : Fragment(R.layout.fragment_top_news), MenuProvider {

    private var _binding: FragmentTopNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopNewsViewModel by viewModels()

    private lateinit var articleAdapter: ArticleListAdapter
    private lateinit var allChip: Chip

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentTopNewsBinding.bind(view)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            this@TopNewsFragment,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        articleAdapter = ArticleListAdapter {
            val action = TopNewsFragmentDirections.actionTopNewsFragmentToArticleFragment(it)
            findNavController().navigate(action)
        }

        articleAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {
            chipGroup.removeAllViews()
            Category.values().forEach { category ->
                val chip = layoutInflater.inflate(
                    R.layout.layout_label_chip,
                    chipGroup,
                    false
                ) as Chip
                chip.text = category.category
                chipGroup.addView(chip)
                if (category == Category.GENERAL) {
                    allChip = chip
                }
            }

            recyclerView.apply {
                adapter = articleAdapter
                addItemDecoration(ItemOffsetDecoration(4))
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            retry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                val text = view.findViewById<Chip>(checkedIds.first()).text.toString()
                viewModel.category.value = text.lowercase()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.topNews.collectLatest {
                    val resource = it ?: return@collectLatest

                    if (resource is Resource.Loading) progressBar.show() else progressBar.hide()
                    recyclerView.isVisible = !resource.data.isNullOrEmpty()
                    retry.isVisible = resource.error != null && resource.data.isNullOrEmpty()

                    if (resource.error != null)
                        Toast.makeText(context, errorMessage { resource.error }, Toast.LENGTH_SHORT)
                            .show()

                    articleAdapter.submitFilterableList(resource.data ?: listOf()) {
                        Runnable {
                            recyclerView.scrollToPosition(0)
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.events.collectLatest {
                    when (it) {
                        is TopNewsViewModel.Event.ShowErrorMessage -> {
                            val errorMessage =
                                "Could not load News: ${it.error.localizedMessage ?: "An unknown error occurred"}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        allChip.isChecked = true
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.onQueryTextChange {
            articleAdapter.filter.filter(it)
            binding.recyclerView.scrollToPosition(0)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.action_refresh -> {
            viewModel.onManualRefresh()
            true
        }

        else -> false
    }
}