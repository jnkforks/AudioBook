package com.allsoftdroid.feature.book_details.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeMetadataRepository(private val bookId:String) : IMetadataRepository {
    private lateinit var metadata :AudioBookMetadataDomainModel
    private val networkResponse = Variable(Event(NetworkState.LOADING))
    override suspend fun loadMetadata() {
        networkResponse.value = Event(NetworkState.LOADING)

        metadata = AudioBookMetadataDomainModel(
            bookId,
            "pravin",
            "2020-05-02",
            "The art of war",
            "None",
            "None",
            "The Art of War",
            "2000",
            "2")

        networkResponse.value = Event(NetworkState.COMPLETED)
    }

    override fun getMetadata(): Flow<AudioBookMetadataDomainModel> {
        return flow { emit(metadata) }
    }

    override fun getBookId(): String {
        return bookId
    }

    override fun cancelRequestInFlight() {

    }

    override fun networkResponse(): Variable<Event<NetworkState>> {
        return networkResponse
    }
}