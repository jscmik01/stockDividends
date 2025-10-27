package hoho.stock.dividends.ui.stockdividends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.R // android.R을 사용하기 위해 필요
import hoho.stock.dividends.data.model.CorpInfo

class CorpInfoAdapter(private val corpList: MutableList<CorpInfo>) :
    RecyclerView.Adapter<CorpInfoAdapter.CorpInfoViewHolder>() {


    // ⚠️ 이제 R.layout.list_item_company_info는 필요 없습니다. ⚠️

    inner class CorpInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // android.R.id.text1과 android.R.id.text2를 사용합니다.
        val text1: TextView = itemView.findViewById(R.id.text1) // 종목명 표시
        val text2: TextView = itemView.findViewById(R.id.text2) // 종목코드 표시

        init {
            itemView.setOnClickListener {
                // TODO: 항목 클릭 시 다음 액션 (예: 해당 종목의 배당금 조회)을 처리할 수 있습니다.
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CorpInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            // ⭐⭐ 수정된 부분: android.R.layout.simple_list_item_2 사용 ⭐⭐
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return CorpInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CorpInfoViewHolder, position: Int) {
        val corpInfo = corpList[position]
    }

    override fun getItemCount(): Int = corpList.size

    // 리스트 데이터를 갱신하고 RecyclerView를 새로고침합니다.
    fun updateList(newList: List<CorpInfo>) {
        corpList.clear()
        corpList.addAll(newList)
        notifyDataSetChanged()
    }
}