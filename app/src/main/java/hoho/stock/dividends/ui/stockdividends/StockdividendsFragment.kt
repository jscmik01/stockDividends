package hoho.stock.dividends.ui.stockdividends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hoho.stock.dividends.databinding.FragmentStockdividendsBinding
import hoho.stock.dividends.ui.stockdividends.StockdividendsViewModel
import hoho.stock.dividends.data.model.CorpInfo
import hoho.stock.dividends.MyApplication
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.SearchView
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.widget.CursorAdapter
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import hoho.stock.dividends.MainActivity // MainActivity import
import hoho.stock.dividends.data.service.CorpInfoService

class StockdividendsFragment : Fragment() {
    // ... (기존 변수 및 바인딩) ...
    private var _binding: FragmentStockdividendsBinding? = null
    private val binding get() = _binding!!

    // 캐시된 전체 종목 리스트를 corporations로 사용
    private var corporations: List<CorpInfo> = emptyList()
    private val displayList: MutableList<CorpInfo> = mutableListOf()

    // lateinit 변수
    private lateinit var corpInfoService: CorpInfoService
    private lateinit var corpInfoAdapter: CorpInfoAdapter // <--- 초기화되지 않은 변수

    private var lastSearchedCorpName: String? = null
    private var lastSearchedJurirNo: String? = null // fetchCompanyInfo에 필요한 경우

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(StockdividendsViewModel::class.java)

        _binding = FragmentStockdividendsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 1. 캐시된 전체 데이터 로드
        corporations = (requireActivity().application as MyApplication).corpListCache ?: emptyList()
        displayList.addAll(corporations)

        // ⭐⭐ 2. CorpInfoAdapter와 RecyclerView 초기화 (setupSearchView() 호출보다 먼저) ⭐⭐
        // RecyclerView를 사용할 준비가 되어야 Adapter를 초기화할 수 있습니다.
        corpInfoAdapter = CorpInfoAdapter(displayList)
        /*binding.companyInfoRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = corpInfoAdapter
        }*/

        // 3. SearchView 리스너 설정
        setupSearchView() // 이 함수 내에서 corpInfoAdapter를 사용합니다

        return root
    }

    // ⭐⭐ SearchView 설정 함수: 드롭다운 자동 완성 구현 ⭐⭐
    private fun setupSearchView() {

        // 1. CursorAdapter 설정
        val from = arrayOf("companyName")
        val to = intArrayOf(android.R.id.text1) // Android 기본 Simple List Item 레이아웃 사용

        val cursorAdapter = SimpleCursorAdapter(
            context,
            android.R.layout.simple_list_item_1, // 단일 텍스트 항목 레이아웃
            null, // 초기 커서
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        // SearchView에 Suggestions Adapter 연결
        binding.stockSearchView.suggestionsAdapter = cursorAdapter

        // 2. Query Text Listener 설정
        binding.stockSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // 검색 버튼을 눌렀을 때
            override fun onQueryTextSubmit(query: String?): Boolean {

                // 광고 새로고침
                (activity as? MainActivity)?.reloadBannerAd()

                if (query != null) {
                    // 국문명 또는 영문명이 정확히 일치하는 종목 찾기
                    val foundCorp = corporations.find {
                        it.corpName.equals(query, ignoreCase = true) ||
                                it.corpEngName.equals(query, ignoreCase = true)
                    }

                    if (foundCorp != null) {
                        lastSearchedJurirNo = null
                        // lastSearchedCorpName은 fetchCompanyInfo 성공 시 업데이트
                        //etchCompanyInfo(foundCorp.corpCode) // 이 안에서 ProgressBar 제어

                        // 정확히 검색된 후, RecyclerView는 비우거나 숨기는 것이 일반적
                        corpInfoAdapter.updateList(emptyList())
                    } else {
                        Toast.makeText(context, "'${query}'에 대한 검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                        lastSearchedCorpName = null // 검색 실패 시 초기화
                        //updateFavoriteButtonIcon() // 아이콘 업데이트
                        corpInfoAdapter.updateList(emptyList()) // RecyclerView 비우기
                    }
                } else {
                    lastSearchedCorpName = null
                    //updateFavoriteButtonIcon()
                }
                binding.stockSearchView.clearFocus()
                return true
            }

            // 텍스트가 변경될 때마다 호출 (자동 완성 목록 업데이트)
            override fun onQueryTextChange(newText: String?): Boolean {

                // 광고 새로고침은 빈도가 높으므로 onQueryTextChange에서는 주석 처리 유지

                val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "companyName"))

                if (!newText.isNullOrBlank()) {
                    val lowerCaseQuery = newText.lowercase()

                    // 국문명(corp_name) 또는 영문명(corp_eng_name)으로 필터링
                    corporations.filter {
                        it.corpName.lowercase().contains(lowerCaseQuery) ||
                                it.corpEngName.lowercase().contains(lowerCaseQuery)
                    }
                        .take(10) // 너무 많은 결과를 표시하지 않도록 최대 10개만 가져오기
                        .forEachIndexed { index, corporation ->
                            // 커서에는 표시할 이름(companyName)과 고유 ID가 필요
                            // 드롭다운 목록에는 국문명만 표시
                            cursor.addRow(arrayOf(index, corporation.corpName))
                        }
                }

                cursorAdapter.changeCursor(cursor)
                lastSearchedCorpName = null // 텍스트 변경 시 검색 상태 초기화
                //updateFavoriteButtonIcon() // 아이콘 업데이트

                // 검색 중에는 하단의 RecyclerView 내용을 비웁니다. (선택 사항)
                corpInfoAdapter.updateList(emptyList())

                return true
            }
        })

        // 3. Suggestion Click Listener 설정
        binding.stockSearchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = false

            // 드롭다운 목록의 항목을 클릭했을 때
            override fun onSuggestionClick(position: Int): Boolean {

                // 광고 새로고침
                (activity as? MainActivity)?.reloadBannerAd()

                val cursor: Cursor? = binding.stockSearchView.suggestionsAdapter.cursor
                if (cursor?.moveToPosition(position) == true) {
                    val colIndex = cursor.getColumnIndex("companyName")
                    if (colIndex != -1) {
                        val selection = cursor.getString(colIndex)
                        // 선택된 항목으로 SearchView 텍스트를 설정하고, onQueryTextSubmit을 호출하여 검색을 실행
                        binding.stockSearchView.setQuery(selection, true)
                    }
                }
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}