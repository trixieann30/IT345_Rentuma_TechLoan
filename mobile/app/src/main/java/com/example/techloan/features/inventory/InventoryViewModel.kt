package com.example.techloan.features.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.InventoryItemDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class InventoryState {
    object Loading : InventoryState()
    data class Success(val items: List<InventoryItemDto>) : InventoryState()
    data class Error(val message: String) : InventoryState()
}

class InventoryViewModel : ViewModel() {

    private val _state = MutableLiveData<InventoryState>()
    val state: LiveData<InventoryState> = _state

    fun loadItems(token: String) {
        _state.value = InventoryState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getAvailableInventory(token)
                if (res.isSuccessful) {
                    _state.value = InventoryState.Success(res.body() ?: emptyList())
                } else {
                    _state.value = InventoryState.Error("Failed to load inventory (${res.code()})")
                }
            } catch (e: Exception) {
                _state.value = InventoryState.Error("Network error: ${e.message}")
            }
        }
    }
}
