package com.tkachuk.kedditbysteps.features

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tkachuk.kedditbysteps.R
import com.tkachuk.kedditbysteps.commons.InfiniteScrollListener
import com.tkachuk.kedditbysteps.commons.RedditNews
import com.tkachuk.kedditbysteps.commons.adapter.NewsAdapter
import com.tkachuk.kedditbysteps.funextension.inflate
import kotlinx.android.synthetic.main.fragment_news.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class NewsFragment : RxBaseFragment() {

    private var redditNews: RedditNews? = null
    private val newsManager by lazy { NewsManager() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_news)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        news_list.setHasFixedSize(true)
        val linearLayout = LinearLayoutManager(context)
        news_list.layoutManager = linearLayout
        news_list.clearOnScrollListeners()
        news_list.addOnScrollListener(InfiniteScrollListener({requestNews()}, linearLayout))
        initAdapter()

        if (savedInstanceState == null) {
            requestNews()
        }
    }

    private fun initAdapter() {
        if (news_list.adapter == null) {
            news_list.adapter = NewsAdapter()
        }
    }

    private fun requestNews() {
        /**
         * first time will send empty string for after parameter.
         * Next time we will have redditNews set with the next page to
         * navigate with the after param.
         */
        val subscription = newsManager.getNews(redditNews?.after ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { retrievedNews ->
                            redditNews = retrievedNews
                            (news_list.adapter as NewsAdapter).addNews(retrievedNews.news)
                        },
                        { e ->
                            Toast.makeText(this@NewsFragment.context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                )
        subscriptions.add(subscription) // add the subscription
    }
}