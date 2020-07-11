package korps


enum class Choice {
    Rock,
    Paper,
    Scissors
}

data class RoundResult(
    val id1: String,
    val id2: String,
    val winnerId: String?,
    val score1: Int,
    val score2: Int,
    val choice1: Choice,
    val choice2: Choice
)
