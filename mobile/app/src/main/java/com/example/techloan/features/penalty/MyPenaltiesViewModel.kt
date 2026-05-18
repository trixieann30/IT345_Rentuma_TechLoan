package com.example.techloan.features.penalty

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.InitiatePaymentRequest
import com.example.techloan.shared.model.PenaltyDto
import com.example.techloan.shared.model.PenaltySummaryDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class PenaltyScreenState {
    object Loading : PenaltyScreenState()
    data class Success(val summary: PenaltySummaryDto) : PenaltyScreenState()
    data class Error(val message: String) : PenaltyScreenState()
}

sealed class PaymentInitState {
    object Idle : PaymentInitState()
    object Loading : PaymentInitState()
    data class Ready(val paymentId: Long, val checkoutUrl: String, val penalty: PenaltyDto) : PaymentInitState()
    data class Error(val message: String) : PaymentInitState()
}

class MyPenaltiesViewModel : ViewModel() {

    val state = MutableLiveData<PenaltyScreenState>()
    val paymentState = MutableLiveData<PaymentInitState>(PaymentInitState.Idle)

    fun load(token: String, userId: Long) {
        state.value = PenaltyScreenState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getUserPenalties(token, userId)
                if (res.isSuccessful && res.body() != null) {
                    state.value = PenaltyScreenState.Success(res.body()!!)
                } else {
                    state.value = PenaltyScreenState.Error("Failed to load penalties")
                }
            } catch (e: Exception) {
                state.value = PenaltyScreenState.Error(e.message ?: "Network error")
            }
        }
    }

    fun initiatePayment(token: String, penalty: PenaltyDto) {
        if (paymentState.value is PaymentInitState.Loading) return
        paymentState.value = PaymentInitState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.initiatePayment(token, InitiatePaymentRequest(penalty.id))
                if (res.isSuccessful && res.body() != null) {
                    val payment = res.body()!!
                    paymentState.value = PaymentInitState.Ready(
                        paymentId = payment.id,
                        checkoutUrl = payment.checkoutUrl ?: "",
                        penalty = penalty
                    )
                } else {
                    val msg = res.errorBody()?.string()?.let {
                        if (it.contains("pending")) "A payment is already in progress for this item. Complete it or try again later." else "Failed to initiate payment"
                    } ?: "Failed to initiate payment"
                    paymentState.value = PaymentInitState.Error(msg)
                }
            } catch (e: Exception) {
                paymentState.value = PaymentInitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetPaymentState() {
        paymentState.value = PaymentInitState.Idle
    }
}
