package hoho.stock.dividends

import android.app.Application
import android.util.Log
import hoho.stock.dividends.data.model.CorpInfo
import hoho.stock.dividends.data.model.DividendPrediction
import hoho.stock.dividends.data.service.CorpInfoService
import hoho.stock.dividends.data.service.DividendPredictionService

class MyApplication : Application() {

    // 1. DART 회사 코드 캐시
    var corpListCache: List<CorpInfo>? = null
        private set

    // 2. ⭐ 배당 예측/목록 데이터 캐시 프로퍼티 추가 ⭐
    var dividendPredictionCache: List<DividendPrediction>? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // 앱이 생성될 때 데이터를 로드하고 캐시합니다.
        loadCorpInfoData()
        loadDividendPredictionData()
    }

    /**
     * corpcode.xml에서 데이터를 읽어와 corpListCache에 저장합니다.
     */
    private fun loadCorpInfoData() {
        Log.d("MyApplication", "Start loading corpcode.xml...")
        val service = CorpInfoService()
        // 'this' (Application Context)를 사용하여 asset 파일에 접근
        corpListCache = service.readXmlFromAssets(this, "corpcode.xml")
        Log.d("MyApplication", "Finished loading corpcode.xml. Cached size: ${corpListCache?.size ?: 0}")
    }

    /**
     * JSON 파일을 읽어와 dividendPredictionCache에 저장합니다.
     */
    private fun loadDividendPredictionData() {
        Log.d("MyApplication", "Start loading dividend_predictions.json...")
        val service = DividendPredictionService()
        // JSON 파일 이름은 'dividend_predictions.json'으로 가정합니다.
        dividendPredictionCache = service.readJsonFromAssets(this, "dividend_predictions.json")
        Log.d("MyApplication", "Finished loading dividend_predictions.json. Cached size: ${dividendPredictionCache?.size ?: 0}")
    }
    // ⭐⭐ 이 함수를 삭제합니다. ⭐⭐
    // fun getCorpListCache(): List<CorpInfo>? {
    //     return corpListCache
    // }
}