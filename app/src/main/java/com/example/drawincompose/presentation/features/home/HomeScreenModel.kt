package com.example.drawincompose.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawincompose.database.DefaultCanvasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val repository: DefaultCanvasRepository,
) : ViewModel() {

    private val mutableState: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Loading)
    val state = mutableState
        .onStart {
            observe()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            mutableState.value,
        )

    private val mutableActions: Channel<OutputAction> = Channel()
    val actions: Flow<OutputAction> = mutableActions.receiveAsFlow()

    private var observeCanvases: Job? = null

    fun input(action: InputAction) {
        when (action) {
            is InputAction.OnCanvasClick -> navigateToCanvasScreen(action.id)
            is InputAction.SetEditMode -> setEditMode(action.isActive)
            is InputAction.SetCanvasSelected -> setCanvasSelected(action.id)
            InputAction.DisableEditMode -> disableEditMode()
            InputAction.OnDeleteIconClick -> deleteCanvases()
        }
    }

    private fun deleteCanvases() {
        val success = (state.value as? HomeScreenState.Success) ?: return

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCanvases(success.selectedCanvases.toList())
            mutableState.update {
                HomeScreenState.Success(
                    selectedCanvases = setOf(),
                    isEditModeActive = false,
                )
            }
        }
    }

    private fun disableEditMode() {
        val success = (state.value as? HomeScreenState.Success) ?: return

        mutableState.update {
            HomeScreenState.Success(
                canvases = success.canvases,
                selectedCanvases = emptySet(),
                isEditModeActive = false,
            )
        }
    }

    private fun setCanvasSelected(id: String) {
        val success = (state.value as? HomeScreenState.Success) ?: return

        val newValue = if (success.selectedCanvases.contains(id)) {
            success.selectedCanvases.minus(id)
        } else {
            success.selectedCanvases.plusElement(id)
        }
        mutableState.update {
            HomeScreenState.Success(
                canvases = success.canvases,
                selectedCanvases = newValue,
                isEditModeActive = newValue.isNotEmpty(),
            )
        }
    }

    private fun setEditMode(isActive: Boolean) {
        val success = (state.value as? HomeScreenState.Success) ?: return
        mutableState.update {
            HomeScreenState.Success(
                canvases = success.canvases,
                isEditModeActive = isActive,
                selectedCanvases = success.selectedCanvases,
            )
        }
    }

    private suspend fun observe() {
        observeCanvases?.cancel()
        runCatching {
            observeCanvases = repository
                .getCanvases()
                .onEach { canvases ->
                    mutableState.update {
                        HomeScreenState.Success(
                            canvases = canvases.map { canvas ->
                                canvas.toState()
                            },
                        )
                    }
                }.launchIn(
                    viewModelScope
                )
        }.onFailure {
            mutableState.update {
                HomeScreenState.Error
            }
        }
    }

    private fun navigateToCanvasScreen(id: String) {
        viewModelScope.launch {
            mutableActions.send(OutputAction.GoToCanvasScreen(id))
        }
    }

    sealed interface OutputAction {
        data class GoToCanvasScreen(val id: String) : OutputAction
    }

    sealed interface InputAction {
        data class OnCanvasClick(val id: String) : InputAction
        data class SetEditMode(val isActive: Boolean) : InputAction
        data class SetCanvasSelected(val id: String) : InputAction
        data object DisableEditMode : InputAction
        data object OnDeleteIconClick : InputAction
    }

}