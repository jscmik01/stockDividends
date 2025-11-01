package hoho.stock.dividends.data.service

import android.util.Log
import hoho.stock.dividends.data.model.DividendDetail
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DividendDetailService {

    private val TAG = "DividendDetailService"

    /**
     * DART API (alotMatter.json)의 JSON 응답 문자열을 파싱하여 DividendDetail 리스트로 반환합니다.
     * org.json 패키지를 사용하여 수동으로 파싱합니다.
     *
     * @param jsonString DART API로부터 받은 JSON 문자열
     * @return 파싱된 DividendDetail 객체 리스트 (파싱 실패 시 빈 리스트)
     */
    fun parseDividendDetails(jsonString: String): List<DividendDetail> {
        val dividendDetailList = mutableListOf<DividendDetail>()

        if (jsonString.isBlank()) {
            Log.e(TAG, "JSON string is blank.")
            return emptyList()
        }

        try {
            val jsonObject = JSONObject(jsonString)

            // 1. 상태 코드 확인
            val status = jsonObject.optString("status")
            if (status != "000") {
                val message = jsonObject.optString("message", "Unknown error")
                Log.e(TAG, "DART API Error: Status=$status, Message=$message")
                return emptyList()
            }

            // 2. 'list' 배열 추출
            val listArray: JSONArray? = jsonObject.optJSONArray("list")

            if (listArray != null) {
                // 3. 배열을 반복하며 각 객체 파싱
                for (i in 0 until listArray.length()) {
                    val item: JSONObject = listArray.getJSONObject(i)

                    dividendDetailList.add(
                        DividendDetail(
                            // optString을 사용하여 키가 없거나 null일 경우 기본값("-") 사용
                            rceptNo = item.optString("rcept_no", "-"),
                            corpCls = item.optString("corp_cls", "-"),
                            corpCode = item.optString("corp_code", "-"),
                            corpName = item.optString("corp_name", "-"),
                            se = item.optString("se", "-"),
                            stockKnd = item.optString("stock_knd", "-"),
                            thstrm = item.optString("thstrm", "-"),
                            frmtrm = item.optString("frmtrm", "-"),
                            lwfr = item.optString("lwfr", "-"),
                            stlmDt = item.optString("stlm_dt", "-")
                        )
                    )
                }
            } else {
                Log.w(TAG, "DART API returned status 000 but 'list' array is missing or null.")
            }

        } catch (e: JSONException) {
            // JSON 포맷이 잘못되었거나 키/타입 오류 발생 시
            Log.e(TAG, "JSON Parsing Error: Invalid format or missing key.", e)
        } catch (e: Exception) {
            // 기타 일반 예외 처리
            Log.e(TAG, "Unknown Parsing Error", e)
        }

        return dividendDetailList
    }
}