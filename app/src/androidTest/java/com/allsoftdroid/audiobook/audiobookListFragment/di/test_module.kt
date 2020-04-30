package com.allsoftdroid.audiobook.audiobookListFragment.di

import com.allsoftdroid.feature_book.presentation.audiobookListFragment.repository.FakeAudioBookRepository
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.domain.usecase.GetSearchBookUsecase
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val bookListViewModelModule : Module = module {
    viewModel {
        AudioBookListViewModel(
            useCaseHandler = get(),
            getAlbumListUseCase = get(),
            getSearchBookUsecase = get()
        )
    }
}

val usecaseModule : Module = module {
    factory {
        UseCaseHandler.getInstance()
    }

    factory {
        GetAudioBookListUsecase(audioBookRep = get())
    }
    factory {
        GetSearchBookUsecase(audioBookRep = get())
    }
}

val repositoryModule : Module = module {
    single {
        FakeAudioBookRepository() as AudioBookRepository
    }
}

val jobModule : Module = module {

    single(named(name = SUPER_VISOR_JOB)) {
        SupervisorJob()
    }

    factory(named(name = VIEW_MODEL_SCOPE)) {
        CoroutineScope(get(named(name = SUPER_VISOR_JOB)) as CoroutineContext + Dispatchers.Main)
    }
}

const val SUPER_VISOR_JOB = "SuperVisorJob"
const val VIEW_MODEL_SCOPE = "ViewModelScope"