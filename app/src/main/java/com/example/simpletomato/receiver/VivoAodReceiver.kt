package com.example.simpletomato.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 用于接收vivo AOD(熄屏显示)相关的广播
 */
class VivoAodReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "VivoAodReceiver"
        var isAodEnabled = false
            private set
            
        // OriginOS 5的广播Action
        private const val ACTION_AOD_STATE_CHANGED = "com.vivo.aod.ACTION_AOD_STATE_CHANGED"
        // 旧版本的广播Action
        private const val ACTION_AOD_ENABLED = "com.vivo.aod.receiver.ACTION_AOD_ENABLED"
        private const val ACTION_AOD_DISABLED = "com.vivo.aod.receiver.ACTION_AOD_DISABLED"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received vivo AOD broadcast: $action")
        
        when (action) {
            ACTION_AOD_ENABLED -> {
                isAodEnabled = true
                Log.d(TAG, "Vivo AOD has been enabled (legacy)")
                updateAodContent(context)
            }
            ACTION_AOD_DISABLED -> {
                isAodEnabled = false
                Log.d(TAG, "Vivo AOD has been disabled (legacy)")
            }
            ACTION_AOD_STATE_CHANGED -> {
                // OriginOS 5的状态变化
                val isEnabled = intent.getBooleanExtra("aod_enabled", false)
                isAodEnabled = isEnabled
                Log.d(TAG, "Vivo AOD state changed (OriginOS 5): enabled=$isEnabled")
                
                if (isEnabled) {
                    updateAodContent(context)
                }
            }
        }
    }
    
    /**
     * 更新AOD内容（这个方法在需要时可以从其他地方调用）
     */
    fun updateAodContent(context: Context) {
        if (!isAodEnabled) return
        
        try {
            // 针对OriginOS 5的方法
            val intentOS5 = Intent("com.vivo.aod.ACTION_UPDATE_CONTENT")
            intentOS5.putExtra("package_name", context.packageName)
            intentOS5.putExtra("class_name", "com.example.simpletomato.MainActivity")
            context.sendBroadcast(intentOS5)
            
            // 备用方法 - 针对旧版本
            val intent = Intent("com.vivo.smartwake.SHOW_CUSTOM_AOD")
            intent.putExtra("packageName", context.packageName)
            intent.putExtra("className", "com.example.simpletomato.MainActivity")
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Sent AOD update requests")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update AOD content", e)
        }
    }
} 