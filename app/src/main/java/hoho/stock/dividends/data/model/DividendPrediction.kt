package hoho.stock.dividends.data.model

data class DividendPrediction(
    val year: String,
    val code: String,
    val name: String,
    val kind: String,
    val quarter: String,
    val actual_dividend: Int,
    val reported_yield: Double
)