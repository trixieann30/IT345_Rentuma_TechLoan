package com.example.techloan.features.custodian

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.AdminPenaltyDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class AdminPenaltyState {
    object Loading : AdminPenaltyState()
    data class Success(val items: List<AdminPenaltyDto>) : AdminPenaltyState()
    data class Error(val message: String) : AdminPenaltyState()
}

class CustodianPenaltyViewModel : ViewModel() {

    val state = MutableLiveData<AdminPenaltyState>()

    private var allItems: List<AdminPenaltyDto> = emptyList()

    fun load(token: String) {
        state.value = AdminPenaltyState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getAllPenalties(token)
                if (res.isSuccessful && res.body() != null) {
                    allItems = res.body()!!
                    state.value = AdminPenaltyState.Success(allItems)
                } else {
                    state.value = AdminPenaltyState.Error("Failed to load penalties")
                }
            } catch (e: Exception) {
                state.value = AdminPenaltyState.Error(e.message ?: "Network error")
            }
        }
    }

    fun filter(filter: String) {
        val filtered = when (filter) {
            "UNPAID" -> allItems.filter { !it.paid }
            "PAID"   -> allItems.filter { it.paid }
            else     -> allItems
        }
        state.value = AdminPenaltyState.Success(filtered)
    }
}
