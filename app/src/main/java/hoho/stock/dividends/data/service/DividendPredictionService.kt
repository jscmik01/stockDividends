package hoho.stock.dividends.data.service

import android.content.Context
import android.util.Log
import hoho.stock.dividends.data.model.DividendPrediction
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class DividendPredictionService {

    private val TAG = "DivPredService"

    /**
     * Assets에서 JSON 파일을 읽어 org.json을 사용하여 DividendPrediction 리스트로 파싱합니다.
     */
    fun readJsonFromAssets(context: Context, fileName: String): List<DividendPrediction>? {
        val predictionList = mutableListOf<DividendPrediction>()

        try {
            // 1. Assets 파일 읽기
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }

            // 2. JSON 배열 파싱
            val jsonArray = JSONArray(jsonString)

            // 3. 배열 순회 및 데이터 추출
            for (i in 0 until jsonArray.length()) {
                val item: JSONObject = jsonArray.getJSONObject(i)

                predictionList.add(
                    DividendPrediction(
                        year = item.optString("year", ""),
                        code = item.optString("code", ""),
                        name = item.optString("name", ""),
                        kind = item.optString("kind", ""),
                        quarter = item.optString("quarter", ""),
                        // 정수나 실수는 optInt, optDouble을 사용하여 안전하게 추출합니다.
                        actual_dividend = item.optInt("actual_dividend", 0),
                        reported_yield = item.optDouble("reported_yield", 0.0)
                    )
                )
            }
            return predictionList

        } catch (e: Exception) {
            Log.e(TAG, "Failed to read or parse JSON from assets: $fileName", e)
            return null
        }
    }
}