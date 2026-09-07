package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verification_logs",
    indices = [
        Index(value = ["medicineId"]),
        Index(value = ["timestamp"])
    ]
)
data class VerificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: String,
    val verificationResult: String, // VERIFIED, FAILED, UNKNOWN
    val databaseHash: String?,
    val blockchainHash: String?,
    val blockchainStatus: String,
    val timestamp: Long = System.currentTimeMillis()
)
