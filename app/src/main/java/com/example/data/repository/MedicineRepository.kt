package com.example.data.repository

import com.example.blockchain.BlockchainVerificationService
import com.example.crypto.MedicineHasher
import com.example.data.dao.MedicineDao
import com.example.data.dao.VerificationLogDao
import com.example.data.model.MedicineEntity
import com.example.data.model.VerificationLogEntity
import kotlinx.coroutines.flow.Flow

sealed class VerificationResult {
    data class Success(
        val medicine: MedicineEntity,
        val currentHash: String,
        val blockchainHash: String,
        val transactionHash: String,
        val timestamp: Long
    ) : VerificationResult()

    data class Tampered(
        val medicine: MedicineEntity,
        val currentHash: String,
        val expectedBlockchainHash: String,
        val transactionHash: String,
        val warning: String
    ) : VerificationResult()

    data class Unknown(
        val medicineId: String,
        val reason: String
    ) : VerificationResult()
}

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val logDao: VerificationLogDao
) {

    val allMedicines: Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()
    val allLogs: Flow<List<VerificationLogEntity>> = logDao.getAllLogs()
    val recentLogs: Flow<List<VerificationLogEntity>> = logDao.getRecentLogs(10)
    val totalMedicinesCount: Flow<Int> = medicineDao.getMedicineCount()
    val totalVerificationsCount: Flow<Int> = logDao.getTotalVerificationsCount()
    val successfulVerificationsCount: Flow<Int> = logDao.getSuccessfulVerificationsCount()
    val failedVerificationsCount: Flow<Int> = logDao.getFailedVerificationsCount()
    val unknownVerificationsCount: Flow<Int> = logDao.getUnknownVerificationsCount()

    suspend fun getMedicineById(medicineId: String): MedicineEntity? {
        return medicineDao.getMedicineById(medicineId.trim().uppercase())
    }

    fun getMedicineByIdFlow(medicineId: String): Flow<MedicineEntity?> {
        return medicineDao.getMedicineByIdFlow(medicineId.trim().uppercase())
    }

    /**
     * Executes the complete 4-step medicine verification workflow
     */
    suspend fun verifyMedicine(medicineId: String): VerificationResult {
        val cleanId = medicineId.trim().uppercase()

        // Step 1: Find the medicine in local database
        val localMedicine = medicineDao.getMedicineById(cleanId)
        if (localMedicine == null) {
            // Unknown medicine
            logDao.insertLog(
                VerificationLogEntity(
                    medicineId = cleanId,
                    verificationResult = "UNKNOWN",
                    databaseHash = null,
                    blockchainHash = null,
                    blockchainStatus = "NOT_FOUND"
                )
            )
            return VerificationResult.Unknown(
                medicineId = cleanId,
                reason = "No registered pharmaceutical record was found in the database for '$cleanId'."
            )
        }

        // Step 2: Regenerate the medicine hash from the current database record
        // If tamper demo mode is active, activeName and activeComposition will reflect the altered blister data
        val currentComputedHash = MedicineHasher.computeHash(
            medicineId = localMedicine.medicineId,
            batchNumber = localMedicine.batchNumber,
            manufacturer = localMedicine.manufacturer,
            name = localMedicine.activeName,
            composition = localMedicine.activeComposition,
            expiryDate = localMedicine.expiryDate
        )

        // Step 3: Retrieve original hash from blockchain smart contract ledger
        val onChainRecord = BlockchainVerificationService.getOnChainRecord(cleanId)

        return if (onChainRecord == null) {
            logDao.insertLog(
                VerificationLogEntity(
                    medicineId = cleanId,
                    verificationResult = "UNKNOWN",
                    databaseHash = currentComputedHash,
                    blockchainHash = null,
                    blockchainStatus = "NOT_ON_CHAIN"
                )
            )
            VerificationResult.Unknown(
                medicineId = cleanId,
                reason = "The medicine exists locally but has not been anchored to the blockchain registry."
            )
        } else if (onChainRecord.medicineHash.equals(currentComputedHash, ignoreCase = true)) {
            // Step 4a: Hashes match -> VERIFIED
            logDao.insertLog(
                VerificationLogEntity(
                    medicineId = cleanId,
                    verificationResult = "VERIFIED",
                    databaseHash = currentComputedHash,
                    blockchainHash = onChainRecord.medicineHash,
                    blockchainStatus = "REGISTERED"
                )
            )
            VerificationResult.Success(
                medicine = localMedicine,
                currentHash = currentComputedHash,
                blockchainHash = onChainRecord.medicineHash,
                transactionHash = onChainRecord.transactionHash,
                timestamp = System.currentTimeMillis()
            )
        } else {
            // Step 4b: Hashes differ -> TAMPERED / VERIFICATION FAILED
            logDao.insertLog(
                VerificationLogEntity(
                    medicineId = cleanId,
                    verificationResult = "FAILED",
                    databaseHash = currentComputedHash,
                    blockchainHash = onChainRecord.medicineHash,
                    blockchainStatus = "TAMPER_DETECTED"
                )
            )
            VerificationResult.Tampered(
                medicine = localMedicine,
                currentHash = currentComputedHash,
                expectedBlockchainHash = onChainRecord.medicineHash,
                transactionHash = onChainRecord.transactionHash,
                warning = "The current medicine information does not match the blockchain-registered record. Possible tampering, tablet alteration, or counterfeit strip detected."
            )
        }
    }

    /**
     * Registers a new medicine, hashes it, anchors it to blockchain, and saves to DB.
     */
    suspend fun registerMedicine(
        name: String,
        composition: String,
        dosage: String,
        uses: String,
        sideEffects: String,
        manufacturer: String,
        batchNumber: String,
        manufacturingDate: String,
        expiryDate: String,
        prescriptionRequired: Boolean
    ): Result<MedicineEntity> {
        val maxId = medicineDao.getMaxId() ?: 4L
        val nextNum = maxId + 1
        val generatedMedicineId = "MED-IND-2026-%06d".format(nextNum)

        // Step A: Generate deterministic SHA-256
        val hash = MedicineHasher.computeHash(
            medicineId = generatedMedicineId,
            batchNumber = batchNumber,
            manufacturer = manufacturer,
            name = name,
            composition = composition,
            expiryDate = expiryDate
        )

        // Step B: Register on blockchain smart contract
        val blockchainResult = BlockchainVerificationService.registerOnBlockchain(
            medicineId = generatedMedicineId,
            batchId = batchNumber,
            medicineHash = hash
        )

        val txHash = blockchainResult.getOrNull()?.transactionHash
            ?: ("0x" + MedicineHasher.hashString("local-$generatedMedicineId-${System.currentTimeMillis()}"))

        val entity = MedicineEntity(
            medicineId = generatedMedicineId,
            name = name.trim(),
            composition = composition.trim(),
            dosage = dosage.trim(),
            uses = uses.trim(),
            sideEffects = sideEffects.trim(),
            prescriptionRequired = prescriptionRequired,
            manufacturer = manufacturer.trim(),
            batchNumber = batchNumber.trim(),
            manufacturingDate = manufacturingDate.trim(),
            expiryDate = expiryDate.trim(),
            medicineHash = hash,
            blockchainTxHash = txHash,
            blockchainStatus = "REGISTERED"
        )

        val rowId = medicineDao.insertMedicine(entity)
        return Result.success(entity.copy(id = rowId))
    }

    /**
     * Controlled Tamper Demo helper to toggle altered state in the database
     */
    suspend fun toggleTamperDemo(medicineId: String, enableTamper: Boolean) {
        val medicine = medicineDao.getMedicineById(medicineId) ?: return
        if (enableTamper) {
            medicineDao.setTamperState(
                medicineId = medicineId,
                isTampered = true,
                tamperedName = "${medicine.name} (Adulterated 250 mg)",
                tamperedComposition = "${medicine.composition} + Starch Filler (Counterfeit)"
            )
        } else {
            medicineDao.setTamperState(
                medicineId = medicineId,
                isTampered = false,
                tamperedName = null,
                tamperedComposition = null
            )
        }
    }
}
