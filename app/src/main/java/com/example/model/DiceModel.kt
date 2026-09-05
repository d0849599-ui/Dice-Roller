package com.example.model

enum class DieType(val label: String, val sides: Int, val isCoin: Boolean = false) {
    D6("D6", 6),
    D4("D4", 4),
    D8("D8", 8),
    D10("D10", 10),
    D12("D12", 12),
    D20("D20", 20),
    COIN("Coin", 2, isCoin = true);

    fun roll(): Int {
        return (1..sides).random()
    }
}

data class SingleDie(
    val id: Int,
    val value: Int,
    val isHeld: Boolean = false
)

data class RollHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val total: Int,
    val values: List<Int>,
    val dieType: DieType,
    val timestamp: Long = System.currentTimeMillis()
)
