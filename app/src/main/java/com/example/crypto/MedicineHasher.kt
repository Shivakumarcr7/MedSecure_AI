package com.example.crypto

import java.security.MessageDigest

/**
 * Deterministic cryptographic hashing engine for MedSecure AI.
 * Computes canonical SHA-256 hash across medicine attributes.
 */
object MedicineHasher {

    data class CanonicalPayload(
        val medicineId: String,
        val batchNumber: String,
        val manufacturer: String,
        val name: String,
        val composition: String,
        val expiryDate: String
    ) {
        /**
         * Normalized canonical string representation (deterministic key ordering & trimmed lowercase)
         */
        fun toCanonicalString(): String {
            return buildString {
                append("{")
                append("\"batchNumber\":\"").append(batchNumber.trim()).append("\",")
                append("\"composition\":\"").append(composition.trim()).append("\",")
                append("\"expiryDate\":\"").append(expiryDate.trim()).append("\",")
                append("\"manufacturer\":\"").append(manufacturer.trim()).append("\",")
                append("\"medicineId\":\"").append(medicineId.trim().uppercase()).append("\",")
                append("\"name\":\"").append(name.trim()).append("\"")
                append("}")
            }
        }
    }

    /**
     * Computes the canonical SHA-256 hash for given medicine attributes.
     */
    fun computeHash(
        medicineId: String,
        batchNumber: String,
        manufacturer: String,
        name: String,
        composition: String,
        expiryDate: String
    ): String {
        val payload = CanonicalPayload(
            medicineId = medicineId,
            batchNumber = batchNumber,
            manufacturer = manufacturer,
            name = name,
            composition = composition,
            expiryDate = expiryDate
        )
        return hashString(payload.toCanonicalString())
    }

    /**
     * Computes raw SHA-256 hex string.
     */
    fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
