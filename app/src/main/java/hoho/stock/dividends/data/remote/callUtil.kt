package hoho.stock.dividends.data.remote

import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class callUtil {

    private suspend fun callApi(urlString: String): String? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("API_CALL_ERROR", "API call failed for $urlString with response code: ${connection.responseCode}")
                //withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "API 호출 실패: ${connection.responseCode}", Toast.LENGTH_SHORT).show()
                    //Toast.makeText(context, "죄송합니다. 현재 공공 데이터 포털 복구가 되지 않아 데이터 조회가 불가능 합니다.", Toast.LENGTH_SHORT).show()
                //}
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            //withContext(Dispatchers.Main) {
                //Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            //}
        }
        return null
    }
}