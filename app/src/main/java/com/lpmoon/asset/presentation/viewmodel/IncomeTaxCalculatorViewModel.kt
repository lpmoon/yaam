package com.lpmoon.asset.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpmoon.asset.domain.model.tax.IncomeTaxHistory
import com.lpmoon.asset.domain.model.tax.IncomeTaxResult
import com.lpmoon.asset.domain.usecase.tax.ClearIncomeTaxHistoriesUseCase
import com.lpmoon.asset.domain.usecase.tax.DeleteIncomeTaxHistoryUseCase
import com.lpmoon.asset.domain.usecase.tax.GetIncomeTaxHistoriesUseCase
import com.lpmoon.asset.domain.usecase.tax.SaveIncomeTaxHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "IncomeTaxCalcViewModel"

/**
 * 普通收入计算器 ViewModel
 * 管理历史记录状态
 */
class IncomeTaxCalculatorViewModel(
    getHistoriesUseCase: GetIncomeTaxHistoriesUseCase,
    private val saveHistoryUseCase: SaveIncomeTaxHistoryUseCase,
    private val deleteHistoryUseCase: DeleteIncomeTaxHistoryUseCase,
    private val clearHistoriesUseCase: ClearIncomeTaxHistoriesUseCase
) : ViewModel() {

    val histories: StateFlow<List<IncomeTaxHistory>> = getHistoriesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 保存计算历史
     */
    fun saveHistory(
        calculationMode: Int,
        inputSalary: Double,
        socialSecurityRate: Double,
        housingFundRate: Double,
        medicalInsuranceRate: Double,
        unemploymentInsuranceRate: Double,
        specialDeduction: Double,
        result: IncomeTaxResult
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveHistory called: calculationMode=$calculationMode, inputSalary=$inputSalary")
            val history = IncomeTaxHistory(
                calculationMode = calculationMode,
                inputSalary = inputSalary,
                socialSecurityRate = socialSecurityRate,
                housingFundRate = housingFundRate,
                medicalInsuranceRate = medicalInsuranceRate,
                unemploymentInsuranceRate = unemploymentInsuranceRate,
                specialDeduction = specialDeduction,
                result = result
            )
            Log.d(TAG, "Saving history: ${history.getSalaryDisplay()}")
            saveHistoryUseCase(history)
            Log.d(TAG, "History saved successfully")
        }
    }

    /**
     * 删除指定历史记录
     */
    fun deleteHistory(historyId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting history: $historyId")
            deleteHistoryUseCase(historyId)
        }
    }

    /**
     * 清除所有历史记录
     */
    fun clearAllHistories() {
        viewModelScope.launch {
            Log.d(TAG, "Clearing all histories")
            clearHistoriesUseCase()
        }
    }
}
