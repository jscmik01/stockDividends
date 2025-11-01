package hoho.stock.dividends.data.model

data class DividendDetail(
    val rceptNo: String?,        // 접수번호
    val corpCls: String?,        // 법인 구분 (Y: 유가증권, K: 코스닥, N: 코넥스)
    val corpCode: String,         // 고유번호 (API 호출 시 사용)
    val corpName: String?,       // 회사명
    val se: String?,             // 구분 (예: "주식배당수익률(%)", "현금배당수익률(%)" 등)
    val stockKnd: String?,       // 주식 종류 (예: "보통주", "우선주")
    val thstrm: String?,         // 당기 (해당 결산기의 배당 값. "-" 또는 "0.0" 등이 올 수 있음)
    val frmtrm: String?,         // 전기 (직전 결산기의 배당 값)
    val lwfr: String?,           // 전전기 (직전전 결산기의 배당 값)
    val stlmDt: String?          // 결산 기준일 (YYYY-MM-DD 형식)
)