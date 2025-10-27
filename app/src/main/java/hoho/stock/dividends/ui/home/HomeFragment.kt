package hoho.stock.dividends.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import hoho.stock.dividends.databinding.FragmentHomeBinding
import hoho.stock.dividends.R // R 파일 경로 확인 필요

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.mobile_navigation, true) // 내비게이션 그래프의 시작점까지 모든 스택을 제거합니다.
            .build()

        val warningTextView = binding.textWarningInfo // 가정: textWarningInfo ID가 바인딩에 있음
        val htmlText = getString(R.string.price_info_warning)

        warningTextView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24 (Nougat) 이상: HtmlCompat.fromHtml 사용
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            // API 23 (Marshmallow) 이하: Deprecated된 Html.fromHtml 사용
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(htmlText)
        }

        warningTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // 배당금조회
        binding.btnStockDividends.setOnClickListener {
            findNavController().navigate(R.id.nav_stockdividends, null, navOptions)
        }

        // 일일시세조회
        binding.btnStockPrice.setOnClickListener {
            findNavController().navigate(R.id.nav_stockprice, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}