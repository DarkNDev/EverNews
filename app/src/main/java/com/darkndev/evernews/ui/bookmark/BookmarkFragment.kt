package com.darkndev.evernews.ui.bookmark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.FragmentBookmarkBinding
import com.darkndev.evernews.recyclerview.adapters.BookmarkListAdapter
import com.darkndev.evernews.recyclerview.decoration.ItemOffsetDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarkFragment : Fragment(R.layout.fragment_bookmark), MenuProvider {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarkViewModel by viewModels()

    private lateinit var bookmarkAdapter: BookmarkListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookmarkBinding.bind(view)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            this@BookmarkFragment,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        bookmarkAdapter = BookmarkListAdapter {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
            context?.startActivity(intent)
        }

        binding.apply {
            recyclerView.apply {
                adapter = bookmarkAdapter
                addItemDecoration(ItemOffsetDecoration(4))
                setHasFixedSize(true)
            }

            ItemTouchHelper(callback).attachToRecyclerView(recyclerView)

            viewModel.items.observe(viewLifecycleOwner) {
                textViewEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                bookmarkAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collectLatest { event ->
                when (event) {
                    is BookmarkViewModel.Event.ShowUndoMessage -> {
                        Snackbar.make(view, "Bookmark Removed", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.undoClicked(event.article)
                            }.show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_bookmark, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.action_delete_all -> {
            viewModel.unBookmarkAll()
            true
        }

        else -> false
    }

    private val callback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = bookmarkAdapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.unBookmark(item)
            }

        }
}