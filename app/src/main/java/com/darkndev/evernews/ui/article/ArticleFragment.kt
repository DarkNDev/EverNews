package com.darkndev.evernews.ui.article

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.darkndev.evernews.R
import com.darkndev.evernews.databinding.FragmentArticleBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class ArticleFragment : Fragment(R.layout.fragment_article) {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArticleBinding.bind(view)

        binding.apply {
            Glide.with(this@ArticleFragment)
                .load(viewModel.article.urlToImage)
                .error(R.drawable.error)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

            title.text = viewModel.article.title
            description.text = viewModel.article.description
            source.text = StringBuilder(viewModel.article.source.name)
            author.text = StringBuilder("- " + viewModel.article.author)

            val date = ZonedDateTime.parse(viewModel.article.publishedAt)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
            published.text = date

            imageView.setOnClickListener {
                viewModel.article.urlToImage?.let { url ->
                    navigate(url)
                }
            }

            readMore.setOnClickListener {
                navigate(viewModel.article.url)
            }

            bookmark.setOnClickListener {
                viewModel.bookmarkClicked()
            }

            viewModel.isBookmarked.observe(viewLifecycleOwner) {
                bookmark.isChecked = it
            }
        }
    }

    private fun navigate(url: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}