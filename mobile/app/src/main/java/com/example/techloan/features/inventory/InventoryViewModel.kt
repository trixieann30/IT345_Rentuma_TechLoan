package com.example.techloan.features.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.InventoryItemDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private fun networkErrorMessage(e: Exception) = when {
    e is SocketTimeoutException || e.message?.contains("timeout", true) == true ->
        "Server is starting up. Please wait a moment and try again."
    e is UnknownHostException -> "No internet connection. Please check your network."
    else -> "Connection failed. Please try again."
}

sealed class InventoryState {
    object Loading : InventoryState()
    data class Success(val items: List<InventoryItemDto>) : InventoryState()
    data class Error(val message: String) : InventoryState()
}

class InventoryViewModel : ViewModel() {

    private val _state = MutableLiveData<InventoryState>()
    val state: LiveData<InventoryState> = _state

    private val _imageUpdate = MutableLiveData<Pair<Long, String>>()
    val imageUpdate: LiveData<Pair<Long, String>> = _imageUpdate

    fun loadItems(token: String) {
        _state.value = InventoryState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getAvailableInventory(token)
                if (res.isSuccessful) {
                    val items = res.body() ?: emptyList()
                    _state.value = InventoryState.Success(items)
                    fetchAutoImages(token, items)
                } else {
                    _state.value = InventoryState.Error("Failed to load inventory (${res.code()})")
                }
            } catch (e: Exception) {
                _state.value = InventoryState.Error(networkErrorMessage(e))
            }
        }
    }

    private fun fetchAutoImages(token: String, items: List<InventoryItemDto>) {
        items.filter { it.imageUrl.isNullOrEmpty() && !it.userProvidedImage }
            .forEach { item ->
                viewModelScope.launch {
                    try {
                        val imgRes = RetrofitClient.api.getAutoImage(token, item.id)
                        if (imgRes.isSuccessful) {
                            val url = imgRes.body()?.get("imageUrl") ?: ""
                            if (url.isNotEmpty()) _imageUpdate.postValue(Pair(item.id, url))
                        }
                    } catch (_: Exception) {}
                }
            }
    }
}
