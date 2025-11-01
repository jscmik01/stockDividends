package hoho.stock.dividends

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import hoho.stock.dividends.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import android.content.IntentSender
import android.content.Intent
import android.app.Activity
import android.util.Log
import android.widget.Toast
import hoho.stock.dividends.data.model.CorpInfo
import androidx.navigation.NavController
import androidx.navigation.findNavController
import hoho.stock.dividends.data.service.CorpInfoService
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var appUpdateManager: AppUpdateManager // AppUpdateManager 선언
    private val REQ_CODE_APP_UPDATE = 100 // 업데이트 요청 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(this) // AppUpdateManager 초기화
        checkAndStartUpdate() // 앱 시작 시 업데이트 확인 및 시작

        // --- 여기부터 광고 코드 ---

        // AdView는 onCreate에서 초기화
        val adView: AdView = findViewById(R.id.adView)
        MobileAds.initialize(this) {}
        adView.loadAd(AdRequest.Builder().build())

        // --- 여기까지 광고 코드 ---

        //val service = hoho.stock.dividends.data.service.CorpInfoService()
        //val corpList = service.readXmlFromAssets(this, "corpcode.xml")
        // ⭐⭐ 캐시된 데이터 사용 (MainActivity에서는 이제 조회만 합니다) ⭐⭐
        val application = application as MyApplication
        val corpList: List<CorpInfo>? = application.corpListCache

        // 필요한 경우, 로드된 데이터 확인용 로그
        Log.d("MainActivity", "Using cached corpList. Size: ${corpList?.size ?: 0}")

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_stockdividends, R.id.nav_stockprice
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // ⭐⭐ 새로 추가할 함수: 배너 광고를 다시 로드합니다. ⭐⭐
    fun reloadBannerAd() {
        val adView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        Log.d("AdReload", "Banner ad reloaded from HomeFragment.")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    // 이 함수는 앱 업데이트의 가용성을 확인하고, 업데이트가 필요하면 즉시 업데이트 흐름을 시작
    private fun checkAndStartUpdate() {
        // 앱 업데이트 가용성 확인
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // 업데이트 가능하며, 즉시 업데이트 타입인 경우
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        REQ_CODE_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    // 업데이트 흐름 시작 실패 처리
                    Log.e("AppUpdate", "Failed to start update flow: ${e.message}")
                }
            }
            // DEVELOPER_TRIGGERED_UPDATE_NEEDED 대신 DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS 사용
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // 개발자가 트리거한 업데이트가 이미 진행 중인 경우 (Flex 업데이트 도중 강제 업데이트로 전환 등)
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // 또는 AppUpdateType.FLEXIBLE
                        this,
                        REQ_CODE_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    Log.e("AppUpdate", "Failed to restart developer-triggered update flow: ${e.message}")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("AppUpdate", "Failed to check for update: ${e.message}")
        }
    }

    // 업데이트 흐름의 결과를 처리하기 위해 onActivityResult를 오버라이드 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_APP_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                // 업데이트가 취소되거나 실패했을 때의 처리
                // 사용자가 업데이트를 거부하거나 다운로드에 실패했을 수 있습니다.
                Log.w("AppUpdate", "Update flow failed! Result code: $resultCode")
                Toast.makeText(this, "앱 업데이트가 취소되었거나 실패했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 앱이 백그라운드에서 다시 포그라운드로 돌아왔을 때, 즉시 업데이트가 이미 다운로드되었는지 확인하고 설치를 프롬프트할 수 있습니다. (Immediate update는 사용자가 설치 완료할 때까지 기다리므로, 보통 onActivityResult로 처리되지만, 만약을 위해 추가할 수 있습니다.)
    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // DEVELOPER_TRIGGERED_UPDATE_NEEDED 대신 DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS 사용
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // 개발자가 트리거한 업데이트가 이미 진행 중인 경우
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // 또는 AppUpdateType.FLEXIBLE
                        this,
                        REQ_CODE_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    Log.e("AppUpdate", "Failed to restart developer-triggered update flow on resume: ${e.message}")
                }
            }
        }
    }

    // ⭐⭐⭐ 3. NavController 필드를 사용하여 뒤로 가기 동작 수정 (오류 없음) ⭐⭐⭐
    override fun onBackPressed() {

        // 내비게이션 그래프에서 홈 화면으로 설정된 ID를 사용합니다.
        val homeDestinationId = R.id.nav_home
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 1. 현재 목적지가 홈 화면인지 확인
        if (navController.currentDestination?.id == homeDestinationId) {
            // 현재 홈 화면이면, 시스템 기본 동작 (앱 종료) 실행
            super.onBackPressed()
        } else {
            // 2. 백 스택을 홈 화면까지 팝(pop) 시도
            // `popBackStack(home, false)`는 백 스택에 home이 있을 때만 돌아가고, 없으면 false를 반환합니다.
            val popped = navController.popBackStack(homeDestinationId, false)

            if (!popped) {
                // 3. popBackStack이 실패했을 경우 (다른 top-level destination에 바로 진입한 경우)
                // -> 홈 화면으로 명시적으로 이동
                navController.navigate(homeDestinationId)

                // 여기서 super.onBackPressed()를 호출하지 않습니다.
                // 홈으로 이동했으므로, 다음 뒤로 가기 버튼을 눌러야 종료됩니다.
            }
        }
    }
}