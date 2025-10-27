package hoho.stock.dividends.data.service

import android.content.Context
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import hoho.stock.dividends.data.model.CorpInfo

class CorpInfoService {

    fun readXmlFromAssets(context: Context, fileName: String): List<CorpInfo> {
        val corpList = mutableListOf<CorpInfo>()

        val inputStream = context.assets.open(fileName)
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentTag = ""
        var corpCode = ""
        var corpName = ""
        var corpEngName = ""
        var stockCode = ""
        var modifyDate = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> currentTag = parser.name
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "corp_code" -> corpCode = text
                            "corp_name" -> corpName = text
                            "corp_eng_name" -> corpEngName = text
                            "stock_code" -> stockCode = text
                            "modify_date" -> modifyDate = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "list") {
                        corpList.add(
                            CorpInfo(corpCode, corpName, corpEngName, stockCode, modifyDate)
                        )
                        // 다음 항목을 위해 초기화
                        corpCode = ""
                        corpName = ""
                        corpEngName = ""
                        stockCode = ""
                        modifyDate = ""
                    }
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return corpList
    }

}