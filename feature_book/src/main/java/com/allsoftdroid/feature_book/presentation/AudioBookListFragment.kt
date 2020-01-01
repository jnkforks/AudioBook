package com.allsoftdroid.feature_book.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.data.network.Utils
import com.allsoftdroid.feature_book.databinding.FragmentAudiobookListBinding
import com.allsoftdroid.feature_book.di.FeatureBookModule
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookAdapter
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookItemClickedListener
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.PaginationListener
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import com.allsoftdroid.feature_book.utils.NetworkState
import org.koin.android.ext.android.inject
import timber.log.Timber


class AudioBookListFragment : BaseContainerFragment(){

    /**
    Lazily initialize the view model
     */
    private val booksViewModel: AudioBookListViewModel by inject()
    @VisibleForTesting var bundleShared: Bundle = Bundle.EMPTY

    private lateinit var callback:OnBackPressedCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding:FragmentAudiobookListBinding = inflateLayout(inflater,R.layout.fragment_audiobook_list,container)

        FeatureBookModule.injectFeature()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.audioBookListViewModel = booksViewModel

        //val audio book adapter
        val bookAdapter = AudioBookAdapter(AudioBookItemClickedListener {
            booksViewModel.onBookItemClicked(it)
        })

        //attach adapter to recycler view
        binding.recyclerViewBooks.adapter = bookAdapter

        //recycler view layout manager
        binding.recyclerViewBooks.apply {
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object : PaginationListener(
                layoutManager = layoutManager as LinearLayoutManager,
                pageSize = Utils.Books.DEFAULT_ROW_COUNT){

                override fun loadNext() {
                    booksViewModel.loadNextData()
                }

                override fun isLoading(): Boolean {
                    return booksViewModel.networkResponse.value?.peekContent() == NetworkState.LOADING
                }
            })
        }

        //Observe the books list and update the list as soon as we get the update
        booksViewModel.audioBooks.observe(viewLifecycleOwner, Observer {
            it?.let {
                bookAdapter.submitList(it)
            }
        })

        booksViewModel.itemClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { bookId ->
                //Navigate to display page
                val bundle = bundleOf("bookId" to bookId)
                bundleShared = bundle

                this.findNavController()
                    .navigate(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle)
            }
        })

        booksViewModel.networkResponse.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){
                    NetworkState.LOADING -> {
                        Toast.makeText(context,"Loading",Toast.LENGTH_SHORT).show()
                        binding.loadingProgressbar.visibility = View.VISIBLE
                    }

                    NetworkState.COMPLETED -> {
                        Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
                        binding.loadingProgressbar.visibility = View.GONE
                    }

                    NetworkState.ERROR -> {
                        binding.loadingProgressbar.visibility = View.GONE
                        Toast.makeText(context,"Network Error",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        binding.toolbarBookSearch.setOnClickListener{
            binding.etToolbarSearch.text.clear()
            booksViewModel.onSearchItemPressed()
        }

        binding.ivSearch.setOnClickListener {
            Toast.makeText(activity,"Search in progress",Toast.LENGTH_SHORT).show()
            val searchText = binding.etToolbarSearch.text.trim().toString()

            if(searchText.length>3){
                booksViewModel.search(query = searchText)
            }
        }

        booksViewModel.searchBooks.observe(this, Observer {
            it.map {book ->
                Timber.d("Fetched: ${book.mId}")
            }

            booksViewModel.onSearchFinished()
            bookAdapter.submitList(null)
            bookAdapter.submitList(it)
        })

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            handleBackPressEvent()
        }

        callback.isEnabled = true
    }

    private fun handleBackPressEvent(){

        Timber.d("backPress:Back pressed")
        if (booksViewModel.isSearching) {

            booksViewModel.apply {
                cancelSearchRequest()
                onSearchFinished()
                loadRecentBookList()
            }
        }else{
            callback.isEnabled = false
            requireActivity().onBackPressed()
        }
    }
}