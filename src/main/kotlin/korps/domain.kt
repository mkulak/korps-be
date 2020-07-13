package korps


enum class Choice {
    Rock,
    Paper,
    Scissors
}

data class RoundResult(
    val winnerId: String?,
    val scores: Map<String, Int>
)
