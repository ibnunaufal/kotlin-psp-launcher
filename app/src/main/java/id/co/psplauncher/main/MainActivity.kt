package id.co.psplauncher.main

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.Item
import id.co.psplauncher.Menu
import id.co.psplauncher.MenuAdapter
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.databinding.ActivityMainBinding
import kotlinx.coroutines.Job


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var isStartOpenDefaultApp: Boolean = false
    private val appName: String = "id.co.solusinegeri.katalisinfostb"
    private var apps: ArrayList<Item>? = null


    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    private var list: ArrayList<Menu> = arrayListOf()

    var handler = Handler()
    var apkUrl = "https://github.com/ibnunaufal/stb-launcher/raw/master/psp-launcher.apk"
    var inputtedApkUrl = ""
    var wifiJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showAll()

        viewModel.updateResponse.observe(this){
            if (it is Resource.Success){
                val pInfo: PackageInfo =
                    this.packageManager.getPackageInfo(this.packageName, 0)
                val version = pInfo.versionName
                Log.d("version", version)
                if(it.value.version != version){
                    binding.btnTest.visibility = android.view.View.VISIBLE
                }
            }
            else if (it is Resource.Failure){
            }
        }
    }

    fun checkUpdate(){
        viewModel.checkUpdate(this.packageName)
    }

    fun showAll(){
        apps = ArrayList()

        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val manager = packageManager
        val availableActivities = manager?.queryIntentActivities(i, 0)
        Log.d("all", availableActivities.toString())

        if (availableActivities != null) {
            binding.rvMenus.layoutManager = LinearLayoutManager(this)
            val menuAdapter = MenuAdapter(list, this)
            binding.rvMenus.adapter = menuAdapter

            Log.d("qweqwe", apps.toString())
            binding.rvMenus.setHasFixedSize(true)

            list.clear()
            for (x in availableActivities){
                Log.d("zxc", x.activityInfo.packageName)
                if(x.activityInfo.packageName.contains("solusinegeri") &&
                    ! x.activityInfo.packageName.contains("psplauncher")){
                    Log.d("zxc", x.activityInfo.packageName)
                    list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                }
                if(x.activityInfo.packageName.contains("vending")){
                    list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                }
                if(x.activityInfo.packageName.contains("settings")){
                    list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                }
                if(x.activityInfo.packageName == "id.co.pspinfo"){
                    list.add(Menu(x.activityInfo.packageName, x.loadLabel(manager).toString(), x.loadIcon(manager)))
                }
                menuAdapter.notifyDataSetChanged()
            }

            Log.d("asdasd", list.toString())
            binding.rvMenus.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()

        }
    }

    override fun onResume() {
        super.onResume()
        checkUpdate()
    }

    override fun onBackPressed() {
        val c = false
        if (c){
            super.onBackPressed()
        }
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

    private fun showLauncherSelection() {
        Log.d("showLauncherSelection", "called")
        val settingsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (settingsIntent.resolveActivity(packageManager) != null) {
                try {
                    Log.d("intent", settingsIntent.toString())
                    startActivity(settingsIntent)
                } catch (e: IntentSender.SendIntentException){
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
}