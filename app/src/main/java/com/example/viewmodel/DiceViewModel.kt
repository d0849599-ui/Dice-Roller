package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DieType
import com.example.model.RollHistoryItem
import com.example.model.SingleDie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiceUiState(
    val diceCount: Int = 2,
    val dieType: DieType = DieType.D6,
    val dice: List<SingleDie> = listOf(
        SingleDie(id = 0, value = 5),
        SingleDie(id = 1, value = 3)
    ),
    val lastResult: Int = 8,
    val isRolling: Boolean = false,
    val history: List<RollHistoryItem> = emptyList(),
    val shakeSensitivity: Float = 13.0f,
    val sensitivityLabel: String = "Normal",
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)

class DiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    private var rollJob: Job? = null

    init {
        // Record the initial 8 in history for clean initial display
        val initialValues = listOf(5, 3)
        _uiState.update {
            it.copy(
                history = listOf(
                    RollHistoryItem(
                        total = 8,
                        values = initialValues,
                        dieType = DieType.D6
                    )
                )
            )
        }
    }

    fun rollDice(onHapticPulse: () -> Unit = {}, onLanded: () -> Unit = {}) {
        if (_uiState.value.isRolling) return

        rollJob?.cancel()
        rollJob = viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true) }

            val currentType = _uiState.value.dieType
            val currentDice = _uiState.value.dice

            // Tumbling animation simulation over ~600ms
            val startTime = System.currentTimeMillis()
            var step = 0
            while (System.currentTimeMillis() - startTime < 600L) {
                step++
                if (step % 2 == 0) {
                    onHapticPulse()
                }
                _uiState.update { state ->
                    val tempDice = state.dice.map { die ->
                        if (die.isHeld) die
                        else die.copy(value = currentType.roll())
                    }
                    val tempSum = tempDice.sumOf { it.value }
                    state.copy(dice = tempDice, lastResult = tempSum)
                }
                delay(70L)
            }

            // Final landed result
            val finalDice = currentDice.map { die ->
                if (die.isHeld) die
                else die.copy(value = currentType.roll())
            }
            val finalSum = finalDice.sumOf { it.value }

            val historyEntry = RollHistoryItem(
                total = finalSum,
                values = finalDice.map { it.value },
                dieType = currentType
            )

            _uiState.update { state ->
                state.copy(
                    dice = finalDice,
                    lastResult = finalSum,
                    isRolling = false,
                    history = listOf(historyEntry) + state.history.take(49)
                )
            }

            onHapticPulse()
            onLanded()
        }
    }

    fun toggleHold(dieId: Int) {
        if (_uiState.value.isRolling) return
        _uiState.update { state ->
            val updated = state.dice.map { die ->
                if (die.id == dieId) die.copy(isHeld = !die.isHeld) else die
            }
            state.copy(dice = updated)
        }
    }

    fun setDiceCount(count: Int) {
        val safeCount = count.coerceIn(1, 6)
        _uiState.update { state ->
            val currentDice = state.dice
            val newDice = if (safeCount > currentDice.size) {
                currentDice + (currentDice.size until safeCount).map { id ->
                    SingleDie(id = id, value = state.dieType.roll())
                }
            } else {
                currentDice.take(safeCount)
            }
            val sum = newDice.sumOf { it.value }
            state.copy(diceCount = safeCount, dice = newDice, lastResult = sum)
        }
    }

    fun setDieType(type: DieType) {
        _uiState.update { state ->
            val newDice = state.dice.map { die ->
                die.copy(value = type.roll(), isHeld = false)
            }
            state.copy(dieType = type, dice = newDice, lastResult = newDice.sumOf { it.value })
        }
    }

    fun setSensitivity(label: String) {
        val threshold = when (label) {
            "High" -> 10.5f
            "Low" -> 16.5f
            else -> 13.0f // Normal
        }
        _uiState.update { it.copy(shakeSensitivity = threshold, sensitivityLabel = label) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }
}
