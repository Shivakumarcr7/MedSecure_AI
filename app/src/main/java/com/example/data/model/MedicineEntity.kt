package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicines",
    indices = [
        Index(value = ["medicineId"], unique = true),
        Index(value = ["batchNumber"])
    ]
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: String,
    val name: String,
    val composition: String,
    val dosage: String,
    val uses: String,
    val sideEffects: String,
    val prescriptionRequired: Boolean,
    val manufacturer: String,
    val batchNumber: String,
    val manufacturingDate: String,
    val expiryDate: String,
    val medicineHash: String,
    val blockchainTxHash: String,
    val blockchainStatus: String = "REGISTERED", // REGISTERED, PENDING, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Controlled demo simulation fields
    val isTamperedDemo: Boolean = false,
    val tamperedName: String? = null,
    val tamperedComposition: String? = null
) {
    /**
     * Returns effective active medicine name (respecting active tamper demonstration)
     */
    val activeName: String
        get() = if (isTamperedDemo && !tamperedName.isNullOrBlank()) tamperedName else name

    /**
     * Returns effective active medicine composition (respecting active tamper demonstration)
     */
    val activeComposition: String
        get() = if (isTamperedDemo && !tamperedComposition.isNullOrBlank()) tamperedComposition else composition
}
