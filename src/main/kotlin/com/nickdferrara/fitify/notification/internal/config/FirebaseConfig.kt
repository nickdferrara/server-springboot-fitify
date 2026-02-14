package com.nickdferrara.fitify.notification.internal.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct
import java.io.FileInputStream

@Configuration
internal class FirebaseConfig(
    @Value("\${fitify.firebase.service-account-path:}")
    private val serviceAccountPath: String,
) {

    @PostConstruct
    fun initFirebase() {
        if (serviceAccountPath.isBlank() || FirebaseApp.getApps().isNotEmpty()) {
            return
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccountPath)))
            .build()

        FirebaseApp.initializeApp(options)
    }
}
