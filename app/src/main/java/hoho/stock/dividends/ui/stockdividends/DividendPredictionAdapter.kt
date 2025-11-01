package hoho.stock.dividends.ui.stockdividends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hoho.stock.dividends.R
import hoho.stock.dividends.data.model.DividendPrediction
import java.text.DecimalFormat

class DividendPredictionAdapter(
    private var predictionList: List<DividendPrediction>
) : RecyclerView.Adapter<DividendPredictionAdapter.PredictionViewHolder>() {

    // 배당금을 원화 형식으로 포맷
    private val currencyFormat = DecimalFormat("#,###원")

    class PredictionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yearQuarter: TextView = itemView.findViewById(R.id.text_year_quarter)
        val stockKind: TextView = itemView.findViewById(R.id.text_stock_kind)
        val actualDividend: TextView = itemView.findViewById(R.id.text_actual_dividend)
        val reportedYield: TextView = itemView.findViewById(R.id.text_reported_yield)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_dividend_prediction, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val prediction = predictionList[position]

        // 1. 년도/분기
        holder.yearQuarter.text = "${prediction.year}년"

        // 2. 주식 종류
        holder.stockKind.text =prediction.quarter

        // 3. 배당금 (원화 포맷 적용)
        val formattedDividend = currencyFormat.format(prediction.actual_dividend)
        holder.actualDividend.text = prediction.kind

        // 4. 배당률 (퍼센트 포맷 적용)
        holder.reportedYield.text = "${formattedDividend} (${String.format("%.2f", prediction.reported_yield)}%)"
    }

    override fun getItemCount(): Int = predictionList.size

    /**
     * Fragment에서 데이터를 업데이트할 때 호출되는 메서드
     */
    fun updateList(newList: List<DividendPrediction>) {
        this.predictionList = newList
        notifyDataSetChanged()
    }
}