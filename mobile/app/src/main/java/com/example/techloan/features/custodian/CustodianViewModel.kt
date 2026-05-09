package com.example.techloan.features.custodian

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.BorrowRequestDto
import com.example.techloan.shared.model.LoanDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class CustodianState {
    object Idle : CustodianState()
    object Loading : CustodianState()
    data class ReservationsLoaded(val items: List<BorrowRequestDto>) : CustodianState()
    data class LoansLoaded(val items: List<LoanDto>) : CustodianState()
    data class ActionSuccess(val message: String) : CustodianState()
    data class Error(val message: String) : CustodianState()
}

class CustodianViewModel : ViewModel() {

    private val _reservationState = MutableLiveData<CustodianState>(CustodianState.Idle)
    val reservationState: LiveData<CustodianState> = _reservationState

    private val _loanState = MutableLiveData<CustodianState>(CustodianState.Idle)
    val loanState: LiveData<CustodianState> = _loanState

    fun loadPendingReservations(token: String) {
        _reservationState.value = CustodianState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getMyReservations(token, status = "PENDING")
                if (res.isSuccessful) {
                    _reservationState.value = CustodianState.ReservationsLoaded(res.body() ?: emptyList())
                } else {
                    _reservationState.value = CustodianState.Error("Failed to load (${res.code()})")
                }
            } catch (e: Exception) {
                _reservationState.value = CustodianState.Error("Network error")
            }
        }
    }

    fun loadAllLoans(token: String) {
        _loanState.value = CustodianState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getMyLoans(token)
                if (res.isSuccessful) {
                    _loanState.value = CustodianState.LoansLoaded(res.body() ?: emptyList())
                } else {
                    _loanState.value = CustodianState.Error("Failed to load (${res.code()})")
                }
            } catch (e: Exception) {
                _loanState.value = CustodianState.Error("Network error")
            }
        }
    }

    fun approveReservation(token: String, id: Long) {
        _reservationState.value = CustodianState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.approveReservation(token, id)
                if (res.isSuccessful) {
                    _reservationState.value = CustodianState.ActionSuccess("Approved")
                } else {
                    _reservationState.value = CustodianState.Error("Failed to approve (${res.code()})")
                }
            } catch (e: Exception) {
                _reservationState.value = CustodianState.Error("Network error")
            }
        }
    }

    fun rejectReservation(token: String, id: Long) {
        _reservationState.value = CustodianState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.rejectReservation(token, id)
                if (res.isSuccessful) {
                    _reservationState.value = CustodianState.ActionSuccess("Rejected")
                } else {
                    _reservationState.value = CustodianState.Error("Failed to reject (${res.code()})")
                }
            } catch (e: Exception) {
                _reservationState.value = CustodianState.Error("Network error")
            }
        }
    }

    fun returnLoan(token: String, id: Long) {
        _loanState.value = CustodianState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.returnLoan(token, id)
                if (res.isSuccessful) {
                    _loanState.value = CustodianState.ActionSuccess("Marked as returned")
                } else {
                    _loanState.value = CustodianState.Error("Failed to return (${res.code()})")
                }
            } catch (e: Exception) {
                _loanState.value = CustodianState.Error("Network error")
            }
        }
    }
}
