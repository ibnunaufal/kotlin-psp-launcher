package id.co.psplauncher.main

import android.app.AlertDialog
import android.app.UiModeManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.Item
import id.co.psplauncher.Menu
import id.co.psplauncher.MenuAdapter
import id.co.psplauncher.R
import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.auth.AuthApi
import id.co.psplauncher.data.network.response.PackageListResponse
import id.co.psplauncher.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var isStartOpenDefaultApp: Boolean = false
    private val appName: String = "id.co.solusinegeri.katalisinfostb"
    private var apps: ArrayList<Item>? = null
    private val baseURL = "https://api.dev.katalis.info/"


    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    private var list: ArrayList<Menu> = arrayListOf()

    var handler = Handler()
    var apkUrl = "https://github.com/ibnunaufal/stb-launcher/raw/master/psp-launcher.apk"
    var inputtedApkUrl = ""
    var wifiJob: Job? = null
    private var activePackageList = mutableListOf<String>()

    /*
    1. check localstorage, ada ngga data package list nya
    2. kalo ada pake yg di localstorage, kalo ngga biarin kosong, sekalian hit api
    3. hasil response api nya, set ke adapter RV dan save ke localstorage
    * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showAllOffline()
        getScreenSize()

        binding.btnTest.setOnClickListener {
//            onAlertDialog(mainLayout)
            inputtedApkUrl = apkUrl
//            requestStoragePermission()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }

        viewModel.updateResponse.observe(this){
            if (it is Resource.Success){
                val pInfo: PackageInfo =
                    this.packageManager.getPackageInfo(this.packageName, 0)
                val version = pInfo.versionName.replace(".", "").toInt()
                Log.d("version", version.toString())
                val verApi = it.value.version.replace(".", "").toInt()
                Log.d("verApiInt", verApi.toString())
                if(version < verApi){
                    binding.btnTest.visibility = View.VISIBLE
                }
            }
            else if (it is Resource.Failure){
            }
        }

        viewModel.packageAppResponse.observe(this){
            if (it is Resource.Success){
                Log.d("packageAppResponse", it.value.toString())
                it.value.forEach { item ->
                    activePackageList.add(item.name)
                }
                viewModel.savePackageList(activePackageList.toString())
                showAllOnline()
            }
        }
        viewModel.getPackageApp()

        if(savedInstanceState == null){
            val temp = getPref()
            Log.d("start",temp.toString())
            if(getPref() == "" || getPref() == "false"){
                startDefaultApp()
            }
        }
    }

    fun setPref(bool: String){
        getPreferences(MODE_PRIVATE).edit().putString("isStartOpenDefaultApp",bool).commit();
    }
    fun getPref():String?{
        val bool: String? = getPreferences(MODE_PRIVATE).getString("isStartOpenDefaultApp","");
        return bool
    }

    private fun startDefaultApp(){
        val launchIntent = packageManager.getLaunchIntentForPackage("id.co.solusinegeri.katalisinfostb")

//        val intent = Intent(this, packageManager.getLaunchIntentForPackage("id.co.solusinegeri.katalisinfostb"));
//        startActivity(intent)

        if (launchIntent != null) {
            Handler().postDelayed({
//                if(isStartOpenDefaultApp){
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
//                    isStartOpenDefaultApp = false
//                }
            }, 2000)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        print("longPress $keyCode");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            println("Back button long pressed")
            onLongBackPressed()
//            var ada = false
//            list.forEach {
//                if (it.label.contains("katalisinfostb")){
//                    ada = true
//                }
//            }
//            if (!ada) {
//                onLongBackPressed()
//            } else {
//                startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), 0);
//            }
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    fun onLongBackPressed(){
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Aksi")

        builder.setNegativeButton("Batal"){
                _, _ ->
        }

        val temp: MutableList<String> = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val roleManager = this.getSystemService(Context.ROLE_SERVICE)
//                    as RoleManager
//            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
//                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
//            ){
            val packageManager = this.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val defaultLauncherPackageName = resolveInfo?.activityInfo?.packageName

            if (defaultLauncherPackageName != null && defaultLauncherPackageName != packageName) {
                temp.add("Atur PSP Launcher sebagai default")
            }
        }

        var ada = false
        list.forEach {
            if (it.label.contains("katalisinfostb")){
                ada = true
            }
        }
        if (!ada) {
            if (checkIsTelevision()){
                temp.add("Install Absensi")
            }
        }
        temp.add("Atur Wifi")
        temp.add("Atur Waktu dan Tanggal")
        temp.add("Atur Tampilan (Zoom)")
        temp.add("Buka Pengaturan Lainnya")

        val devices = temp.toTypedArray()
        builder.setItems(
            devices
        ) { _, which ->
            if (temp[which].contains("Install Absensi")) {
                confirmDownload()
            } else if(temp[which].contains("Wifi")) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } else if(temp[which].contains("Waktu")) {
                startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
            } else if(temp[which].contains("Tampilan")) {
                startActivityForResult(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS), 0);
//                startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
            } else if(temp[which].contains("Launcher")) {
                showLauncherSelection()
            } else {
                startActivity(Intent(Settings.ACTION_SETTINGS));
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
    fun confirmDownload(){
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Konfirmasi")
        builder.setMessage("Anda yakin akan mengunduh Absensi")

        builder.setNegativeButton("Batal"){
                _, _ -> onLongBackPressed()
        }

        builder.setPositiveButton("Unduh"){
                _, _ ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("http://play.google.com/store/apps/details?id=id.co.solusinegeri.katalisinfostb")
                setPackage("com.android.vending")
            }
            startActivity(intent)
//            downloadController = DownloadControllerPlaystore(this@MainActivity)
//            inputtedApkUrl = "https://raw.githubusercontent.com/ibnunaufal/stb-launcher/master/Absensi/Latest/app-debug.apk"
//            checkStoragePermission()

        }
        val dialog = builder.create()
        dialog.show()
    }


    fun checkUpdate(){
        viewModel.checkUpdate(this.packageName)
        Log.i("checkupdate", this.packageName)
    }

    fun showAllOffline(){
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val offlineData = viewModel.getActivePackageList()
        val offlineList = offlineData.replace("[", "").replace("]", "").replace(" ", "").split(",")
        Log.i("offlineList" , offlineList.toString())

        val manager = packageManager
        val availableActivities = manager?.queryIntentActivities(i, 0)
        Log.d("all", availableActivities.toString())

        if (offlineData.isNotEmpty()) {
            Log.i("offline", "isNotEmpty")
            binding.rvMenus.layoutManager = LinearLayoutManager(this)
            val menuAdapter = MenuAdapter(list, this)
            binding.rvMenus.apply {
                layoutManager = GridLayoutManager(this@MainActivity, 2)
                adapter = menuAdapter
            }
            binding.rvMenus.setHasFixedSize(true)

            list.clear()
            if (availableActivities != null) {
                for (x in availableActivities){
                    if(x.activityInfo.packageName.contains("vending")){
                        list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                    }
                    for(item in offlineList){
                        if(x.activityInfo.packageName.contains(item)){
                            Log.i("package name", item)
                            list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                        }
                    }

                    menuAdapter.notifyDataSetChanged()
                }
            }
            Log.d("asdasd", list.toString())
            binding.rvMenus.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
            activePackageList.clear()
        } else {
            Log.i("offline", "isEmpty")
        }
    }
    fun showAllOnline(){
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val manager = packageManager
        val availableActivities = manager?.queryIntentActivities(i, 0)
        Log.d("all", availableActivities.toString())

        if (availableActivities != null) {
            binding.rvMenus.layoutManager = LinearLayoutManager(this)
            val menuAdapter = MenuAdapter(list, this)
            binding.rvMenus.apply {
                layoutManager = GridLayoutManager(this@MainActivity, 2)
                adapter = menuAdapter
            }
            binding.rvMenus.setHasFixedSize(true)

            list.clear()
            for (x in availableActivities){
                if(x.activityInfo.packageName.contains("vending")){
                    list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                }
                for(item in activePackageList){
                    if(x.activityInfo.packageName.contains(item)){
                        Log.i("package name", item)
                        list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                    }
                }
                menuAdapter.notifyDataSetChanged()
            }
            Log.d("asdasd", list.toString())
            val offlineData = viewModel.getActivePackageList()
            Log.i("UserPreferences Data", offlineData)
            binding.rvMenus.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
            activePackageList.clear()
        } else { }
    }

    fun getScreenSize(){
        list = list
        val height: Int = this.resources.displayMetrics.heightPixels
        val width: Int = this.resources.displayMetrics.widthPixels
        Log.d("size","height: $height, widht: $width")
        val bigSize = 30
        val smallSize = 16

        if(width > 1000){
//            wifi.textSize = bigSize.toFloat()
            binding.txtclock.textSize = bigSize.toFloat()
        }else{
//            binding.wifi.textSize = smallSize.toFloat()
            binding.txtclock.textSize = smallSize.toFloat()
        }
//        var adapter = ArrayAdapter<Item>


    }


    override fun onResume() {
        super.onResume()
        checkUpdate()
        getPackageList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        val time: LocalDateTime = LocalDateTime.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val timeNow = time.format(formatter).toInt()
        Log.d("time", timeNow.toString())
        if (timeNow >= 20231215){
            super.onBackPressed()
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val packageManager = this.packageManager
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }

                val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                val defaultLauncherPackageName = resolveInfo?.activityInfo?.packageName

                if (defaultLauncherPackageName != null && defaultLauncherPackageName != packageName) {
                    // PSP Launcher is not set as the default launcher
                    val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    builder.setTitle("Peringatan")
                    builder.setMessage("PSP Launcher belum diatur menjadi Launcher default")
                    builder.setPositiveButton("Atur Default") { _, _ ->
                        showLauncherSelection()
                    }
                    builder.setNegativeButton("Nanti") { _, _ ->
                    }

                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
    }
    private fun isAndroidTV(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
    private fun showLauncherSelection() {
        Log.d("showLauncherSelection", "called")
        val settingsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (settingsIntent.resolveActivity(packageManager) != null) {
                Log.d("showLauncherSelection", "${isAndroidTV()}")
                if (isAndroidTV()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = this.getSystemService(Context.ROLE_SERVICE)
                                as RoleManager
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        startActivityForResult(intent, 0)
                        return
                    }
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    Log.d("intent", settingsIntent.toString())
                    startActivity(settingsIntent)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("error", e.toString())
                }
            } else {
                // Handle the case when the settings activity is not available
                // or the device does not support managing default apps
            }
        } else {
            // Handle the case when the device's Android version is below O
        }
    }

    fun getPackageList(){
        viewModel.getPackageApp()
        Log.i("getlist", "called")
    }

    private fun checkIsTelevision(): Boolean {
        val uiMode: Int = resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
    }

    fun openAppDetail(app: String, name: String){
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Opsi $name")

        builder.setNegativeButton("Batal"){
                _, _ ->
        }

        val temp: MutableList<String> = mutableListOf()

        if (app != "com.android.vending"){
            temp.add("Hapus Aplikasi")
        }
        temp.add("Buka Detail Aplikasi")

        val devices = temp.toTypedArray()
        builder.setItems(
            devices
        ) { _, which ->
            if (temp[which] == "Hapus Aplikasi") {
                confirmDelete(app)
            } else {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$app")))
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
    fun confirmDelete(packageName: String){
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        var name = ""
        list.forEach {
            if (packageName == it.label){
                name = it.name
            }
        }
        builder.setTitle("Anda yakin akan melakukan menghapus $name?")
        builder.setPositiveButton("Ya"){
                _,_ -> doRemove(packageName)
        }
        builder.setNegativeButton("Batal"){
                _, _ ->
        }

        val dialog = builder.create()
        dialog.show()
    }
    fun doRemove(packageName: String){
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
        showAllOffline()
    }
}