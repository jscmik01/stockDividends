package hoho.stock.dividends

import android.app.Application
import android.util.Log
import hoho.stock.dividends.data.model.CorpInfo
import hoho.stock.dividends.data.service.CorpInfoService

class MyApplication : Application() {

    // ⭐⭐ 수정 없음: 이 프로퍼티가 자동으로 getCorpListCache()를 생성합니다. ⭐⭐
    var corpListCache: List<CorpInfo>? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // 앱이 생성될 때 데이터를 로드하고 캐시합니다.
        loadCorpInfoData()
    }

    /**
     * corpcode.xml에서 데이터를 읽어와 corpListCache에 저장합니다.
     * 이 함수는 앱 실행 시 한 번만 호출됩니다.
     */
    private fun loadCorpInfoData() {
        Log.d("MyApplication", "Start loading corpcode.xml...")
        val service = CorpInfoService()
        // 'this' (Application Context)를 사용하여 asset 파일에 접근
        corpListCache = service.readXmlFromAssets(this, "corpcode.xml")
        Log.d("MyApplication", "Finished loading corpcode.xml. Cached size: ${corpListCache?.size ?: 0}")
    }

    // ⭐⭐ 이 함수를 삭제합니다. ⭐⭐
    // fun getCorpListCache(): List<CorpInfo>? {
    //     return corpListCache
    // }
}