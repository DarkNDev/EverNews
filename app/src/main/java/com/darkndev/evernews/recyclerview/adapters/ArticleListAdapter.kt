package com.darkndev.evernews.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.LayoutArticleItemBinding
import com.darkndev.evernews.models.Article

class ArticleListAdapter(val onArticleClick: (Article) -> Unit) :
    ListAdapter<Article, ArticleListAdapter.ArticleViewHolder>(TopArticleDiffUtil), Filterable {

    private lateinit var articlesFull: List<Article>

    object TopArticleDiffUtil : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article) =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Article, newItem: Article) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutArticleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null)
            holder.bind(item)
    }

    fun submitFilterableList(articles: List<Article>, afterSubmit: () -> Runnable) {
        submitList(articles, afterSubmit())
        articlesFull = articles
    }

    inner class ArticleViewHolder(val binding: LayoutArticleItemBinding) :
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

    override fun getFilter(): Filter {
        return FilterArticles()
    }

    inner class FilterArticles : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<Article>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(articlesFull)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()

                articlesFull.forEach {
                    if (it.title != null && it.title.lowercase().contains(filterPattern)) {
                        filteredList.add(it)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filteredList = results?.values as List<Article>
            submitList(filteredList)
            return
        }
    }
}