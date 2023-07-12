package com.darkndev.evernews.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.LayoutSearchItemBinding
import com.darkndev.evernews.models.Article

class ArticlePagingAdapter(val onArticleClick: (Article) -> Unit) :
    PagingDataAdapter<Article, ArticlePagingAdapter.ArticleViewHolder>(ArticleDiffUtil) {

    object ArticleDiffUtil : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article) =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
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

    inner class ArticleViewHolder(
        val binding: LayoutSearchItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if (bindingAdapterPosition != RecyclerView.NO_POSITION && item != null)
                    onArticleClick(item)
            }
        }

        fun bind(item: Article) {
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