package com.example.simpletomato

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.simpletomato.databinding.ActivityMainBinding
import com.example.simpletomato.util.VivoAodHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        // 请求通知权限（仅对Android 13及以上版本需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
        
        // 针对vivo设备请求特殊权限
        if (VivoAodHelper.isVivoDevice()) {
            if (VivoAodHelper.isX200Series()) {
                // X200系列专用权限
                binding.root.postDelayed({
                    requestX200SpecificPermissions()
                }, 2000)
            } else {
                // 其他vivo设备
                binding.root.postDelayed({
                    requestVivoSpecificPermissions()
                }, 2000)
            }
        }
        
        // 请求忽略电池优化
        requestIgnoreBatteryOptimization()
    }
    
    /**
     * 请求vivo X200系列特定权限 (OriginOS 5)
     */
    private fun requestX200SpecificPermissions() {
        // 1. 请求自启动权限 (X200专用路径)
        try {
            val intent = Intent()
            intent.component = ComponentName("com.vivo.permissionmanager", 
                                      "com.vivo.permissionmanager.activity.PurviewTabActivity")
            intent.putExtra("packagename", packageName)
            intent.putExtra("tabId", "1")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Toast.makeText(this, "请在列表中找到'SimpleTomato'并允许自启动", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 如果失败，尝试标准路径
            try {
                val intent = Intent()
                intent.component = ComponentName("com.vivo.permissionmanager", 
                                          "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                startActivity(intent)
                Toast.makeText(this, "请在列表中找到'SimpleTomato'并允许自启动", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "请手动前往设置-应用-SimpleTomato-权限，允许自启动", Toast.LENGTH_LONG).show()
            }
        }
        
        // 2. 请求OriginOS 5的AOD(熄屏显示)权限
        try {
            val intent = Intent()
            intent.component = ComponentName("com.android.settings", 
                                     "com.android.settings.Settings\$AlwaysOnDisplayActivity")
            startActivity(intent)
            Toast.makeText(this, "请在熄屏显示设置中允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                // 针对OriginOS 5的标准路径
                val intent = Intent("android.settings.AOD_SETTINGS")
                startActivity(intent)
                Toast.makeText(this, "请在熄屏显示设置中允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "请手动前往设置-显示与亮度-熄屏显示，允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
            }
        }
        
        // 3. 请求X200特有的"显示在其他应用上层"权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                    Toast.makeText(this, "请允许'SimpleTomato'显示在其他应用的上层", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "请手动前往设置-应用-SimpleTomato-权限，允许显示在其他应用上层", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 请求vivo特定权限
     */
    private fun requestVivoSpecificPermissions() {
        // 1. 请求自启动权限
        try {
            val intent = Intent()
            intent.component = ComponentName("com.vivo.permissionmanager", 
                                      "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            startActivity(intent)
            Toast.makeText(this, "请在列表中找到'SimpleTomato'并允许自启动", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 如果失败，引导用户手动设置
            Toast.makeText(this, "请手动前往设置-应用-SimpleTomato-权限，允许自启动", Toast.LENGTH_LONG).show()
        }
        
        // 2. 请求AOD(熄屏显示)权限
        try {
            val intent = Intent("android.settings.AOD_SETTINGS")
            startActivity(intent)
            Toast.makeText(this, "请在熄屏显示设置中允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                // 针对部分vivo机型的备用路径
                val intent = Intent()
                intent.component = ComponentName("com.android.settings", 
                                         "com.android.settings.Settings\$AODSettingsActivity")
                startActivity(intent)
                Toast.makeText(this, "请在熄屏显示设置中允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // 如果都失败，引导用户手动设置
                Toast.makeText(this, "请手动前往设置-显示与亮度-熄屏显示，允许'SimpleTomato'显示", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 请求忽略电池优化
     */
    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "请手动前往设置-电池-应用启动管理，允许'SimpleTomato'后台运行", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }
} 