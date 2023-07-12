package com.darkndev.evernews.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.LayoutSearchItemBinding
import com.darkndev.evernews.models.BookmarkedArticle

class BookmarkListAdapter(val onBookmarkArticleClick: (BookmarkedArticle) -> Unit) :
    ListAdapter<BookmarkedArticle, BookmarkListAdapter.ArticleViewHolder>(BookmarkDiffUtil) {

    object BookmarkDiffUtil : DiffUtil.ItemCallback<BookmarkedArticle>() {
        override fun areItemsTheSame(
            oldItem: BookmarkedArticle,
            newItem: BookmarkedArticle
        ) = oldItem.url == newItem.url

        override fun areContentsTheSame(
            oldItem: BookmarkedArticle,
            newItem: BookmarkedArticle
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null)
            holder.bind(item)
    }

    inner class ArticleViewHolder(val binding: LayoutSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if (bindingAdapterPosition != RecyclerView.NO_POSITION && item != null)
                    onBookmarkArticleClick(item)
            }
        }

        fun bind(item: BookmarkedArticle) {
            binding.apply {
                Glide.with(itemView)
                    .load(item.urlToImage)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.error)
                    .into(newsView)

                title.text = item.title ?: ""
            }
        }

    }
}