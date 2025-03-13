package com.easylists.presentation.ui.listoflists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.use_cases.AddListFlowUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.ListOfListsState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.haan.resultat.Resultat
import fr.haan.resultat.onFailure
import fr.haan.resultat.onLoading
import fr.haan.resultat.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListOfListsViewModel @Inject constructor(
    private val getListListFlowUseCase: GetListFlowUseCase,
    private val addListUseCase: AddListFlowUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    val lists = listOf("Groceries", "Travel", "Travel Packing")

    private var listListFlowJob: Job? = null

    var state by mutableStateOf( ListOfListsState() )


    init {
        initListList()
    }


    //region addList() :: Add a list to the database
    fun addList() {
        //
        // TODO: validate the new list name is not already in use
        //

        viewModelScope.launch {
            addListUseCase(
                list = EasyListsList(name = lists.random())
            )
        }
    }
    //endregion


    //region initListList() :: initialize list of lists from the database
    fun initListList() {
        cancelListFlowCollection()

        listListFlowJob = getListListFlowUseCase()
            .onEach {
                handleGetBrokerState(Resultat.success(it))
            }.catch {
                handleGetBrokerState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetBrokerState(result: Resultat<List<EasyListsList>?>) {
        result.onSuccess {
            Arbor.i("List of lists loaded successfully: $it")
            state = state.copy(
//                dataFetchStage = ListListDataFetchState.LocalBrokerSuccess,
                isPullToRefreshing = false,
                listList = it ?: emptyList(),
            )
        }.onFailure {
            state = state.copy(
                uiState = ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))
            )
        }.onLoading {
//            state = state.copy(
//                state = CoinsListUiState.Refreshing(isAutomaticRefresh = true)
//            )
        }
    }


    private fun cancelListFlowCollection() {
        listListFlowJob?.cancel()
        listListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion

}
