package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class FetchAdditionalBookDetailsRepositoryTest{

    private lateinit var storeCachingRepository: IStoreRepository
    private lateinit var bookDetailsParser : BookDetailsParserFromHtml

    // Class Under Test
    private lateinit var bookDetailsRepository: IFetchAdditionBookDetailsRepository

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createRepository(){
        storeCachingRepository = mock(IStoreRepository::class.java)
        bookDetailsParser = BookDetailsParserFromHtml()

        bookDetailsRepository = FetchAdditionalBookDetailsRepositoryImpl(
                bookDetailsParser = bookDetailsParser,
                storeCachingRepository = storeCachingRepository)
    }

//    @ExperimentalCoroutinesApi
//    @Test
//    fun fetchBookDetails_bookURL_returnsBookDetails(){
//        mainCoroutineRule.runBlockingTest {
//            try{
//                bookDetailsRepository.registerNetworkResponse(object : NetworkResponseListener{
//                    override suspend fun onResponse(result: NetworkResult) {
//                        when(result){
//                            is Success -> assertThat(1, `is`(1))
//                            else -> assertThat(1, `is`(0))
//                        }
//                    }
//                })
//
//                bookDetailsRepository.fetchBookDetails("")
//            }catch (e:Exception){
//                assertThat(e.message, `is`("This job has not completed yet"))
//            }
//        }
//    }

}

