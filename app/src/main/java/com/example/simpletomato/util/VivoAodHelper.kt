package com.example.simpletomato.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * vivo熄屏显示(AOD)助手类
 * 
 * vivo手机的AOD功能API因设备和系统版本而异，这个类封装了几种常见的实现方式
 */
class VivoAodHelper {
    
    companion object {
        private const val TAG = "VivoAodHelper"
        
        // 检查是否是vivo设备
        fun isVivoDevice(): Boolean {
            return Build.MANUFACTURER.toLowerCase().contains("vivo")
        }
        
        // 检查是否是X200系列设备
        fun isX200Series(): Boolean {
            return Build.MODEL.toLowerCase().contains("x200")
        }
        
        /**
         * 更新vivo AOD显示内容
         * 
         * @param context 上下文
         * @param title 显示标题，如"番茄钟"
         * @param content 显示内容，如"剩余25分钟"
         * @return 是否成功发送更新请求
         */
        fun updateAodDisplay(context: Context, title: String, content: String): Boolean {
            if (!isVivoDevice()) {
                Log.d(TAG, "Not a vivo device, skipping AOD update")
                return false
            }
            
            try {
                // 针对OriginOS 5和X200系列设备的优化方法
                if (isX200Series()) {
                    // OriginOS 5新增的AOD更新方法
                    val intentOS5 = Intent("com.vivo.aod.ACTION_UPDATE_CONTENT")
                    intentOS5.putExtra("app_name", context.getString(context.applicationInfo.labelRes))
                    intentOS5.putExtra("app_title", title)
                    intentOS5.putExtra("app_content", content)
                    intentOS5.putExtra("package_name", context.packageName)
                    // 通知优先级设为高
                    intentOS5.putExtra("priority", "high")
                    context.sendBroadcast(intentOS5)
                    
                    // OriginOS 5另一种可能的方法
                    val intentOS5Alt = Intent("android.intent.action.vivo.AOD_SHOW_NOTIFICATION")
                    intentOS5Alt.putExtra("title", title)
                    intentOS5Alt.putExtra("text", content)
                    intentOS5Alt.putExtra("packageName", context.packageName)
                    context.sendBroadcast(intentOS5Alt)
                    
                    Log.d(TAG, "Sent OriginOS 5 specific AOD requests")
                }
                
                // 尝试多种方法更新AOD显示（保留通用方法作为备选）
                
                // 方法1: 使用com.vivo.aodnotification广播
                val intent1 = Intent("com.vivo.aodnotification")
                intent1.putExtra("title", title)
                intent1.putExtra("content", content)
                intent1.putExtra("packageName", context.packageName)
                context.sendBroadcast(intent1)
                
                // 方法2: 使用FuntouchOS/OriginOS标准广播
                val intent2 = Intent("com.vivo.notification.aod.show")
                intent2.putExtra("aod_type", "custom")
                intent2.putExtra("aod_title", title)
                intent2.putExtra("aod_content", content)
                intent2.putExtra("package_name", context.packageName)
                intent2.putExtra("class_name", "com.example.simpletomato.MainActivity")
                context.sendBroadcast(intent2)
                
                // 方法3: 针对部分新机型
                val intent3 = Intent("com.vivo.smartwake.CUSTOM_AOD_CONTENT")
                intent3.putExtra("title", title)
                intent3.putExtra("content", content)
                intent3.putExtra("packageName", context.packageName)
                intent3.putExtra("showTime", true) // 显示时间
                context.sendBroadcast(intent3)
                
                Log.d(TAG, "Sent AOD update requests with content: $content")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update AOD display", e)
                return false
            }
        }
        
        /**
         * 格式化时间用于AOD显示（只显示分钟）
         */
        fun formatTimeForAod(millis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            return "剩余 $minutes 分钟"
        }
    }
} 