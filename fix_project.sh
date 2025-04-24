android {
    // 其他配置...
    
    signingConfigs {
        release {
            storeFile file("your_keystore.p12")
            storePassword "your_password"
            keyAlias "your_key_alias"
            keyPassword "your_key_password"
            storeType "PKCS12"  // 明确指定PKCS12格式
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // 其他配置...
        }
    }
}