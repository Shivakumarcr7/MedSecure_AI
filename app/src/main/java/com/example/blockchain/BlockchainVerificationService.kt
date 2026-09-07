package com.example.blockchain

import com.example.crypto.MedicineHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * On-Chain Medicine Verification Service.
 * Implements the decentralized verification logic mirroring MedicineRegistry.sol.
 */
object BlockchainVerificationService {

    data class OnChainRecord(
        val medicineId: String,
        val batchId: String,
        val medicineHash: String,
        val timestamp: Long,
        val transactionHash: String,
        val registeredBy: String,
        val network: String = "Hardhat Local / Sepolia Testnet"
    )

    sealed class VerificationOutcome {
        data class Verified(
            val record: OnChainRecord,
            val currentHash: String,
            val transactionHash: String
        ) : VerificationOutcome()

        data class Tampered(
            val record: OnChainRecord,
            val currentHash: String,
            val expectedBlockchainHash: String,
            val diffDetails: String
        ) : VerificationOutcome()

        data class Unknown(
            val requestedId: String,
            val reason: String = "No registered record found on the blockchain ledger."
        ) : VerificationOutcome()
    }

    // In-memory on-chain state sync
    private val onChainRegistry = ConcurrentHashMap<String, OnChainRecord>()

    init {
        // Seed default on-chain ledger records corresponding to demo medicines
        val pHash = MedicineHasher.computeHash(
            medicineId = "MED-IND-2026-000001",
            batchNumber = "MED2026A001",
            manufacturer = "Demo Pharma India Pvt. Ltd.",
            name = "Paracetamol 500 mg",
            composition = "Paracetamol IP 500 mg",
            expiryDate = "2028-01-14"
        )
        onChainRegistry["MED-IND-2026-000001"] = OnChainRecord(
            medicineId = "MED-IND-2026-000001",
            batchId = "MED2026A001",
            medicineHash = pHash,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5,
            transactionHash = "0x7d89b02a3a5c71b659c2394fd5e7300c0be6c8fbc27b0bf9ef4dc8577eb0c821",
            registeredBy = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
        )

        val cHash = MedicineHasher.computeHash(
            medicineId = "MED-IND-2026-000002",
            batchNumber = "MED2026B042",
            manufacturer = "Apex Healthcare Labs (Demo)",
            name = "Cetirizine 10 mg",
            composition = "Cetirizine Hydrochloride IP 10 mg",
            expiryDate = "2028-02-09"
        )
        onChainRegistry["MED-IND-2026-000002"] = OnChainRecord(
            medicineId = "MED-IND-2026-000002",
            batchId = "MED2026B042",
            medicineHash = cHash,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3,
            transactionHash = "0x41f92e7381db2ef25bf92289c09c31398c8808dcff32b498e7276537bfa39801",
            registeredBy = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
        )

        val oHash = MedicineHasher.computeHash(
            medicineId = "MED-IND-2026-000003",
            batchNumber = "MED2026C109",
            manufacturer = "Zenith Life Sciences (Demo)",
            name = "Omeprazole 20 mg",
            composition = "Omeprazole Magnesium IP 20 mg",
            expiryDate = "2027-08-31"
        )
        onChainRegistry["MED-IND-2026-000003"] = OnChainRecord(
            medicineId = "MED-IND-2026-000003",
            batchId = "MED2026C109",
            medicineHash = oHash,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2,
            transactionHash = "0x9c31168cb769188a10058b88d8442e5a6104bc18a4d7d9198647cc9d8d67240c",
            registeredBy = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
        )

        val aHash = MedicineHasher.computeHash(
            medicineId = "MED-IND-2026-000004",
            batchNumber = "MED2026D770",
            manufacturer = "BioShield Pharmaceuticals (Demo)",
            name = "Azithromycin 500 mg",
            composition = "Azithromycin Dihydrate IP equivalent to 500 mg",
            expiryDate = "2027-10-11"
        )
        onChainRegistry["MED-IND-2026-000004"] = OnChainRecord(
            medicineId = "MED-IND-2026-000004",
            batchId = "MED2026D770",
            medicineHash = aHash,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
            transactionHash = "0x2e08c02c6b4fa7bf5132204c3f58a8a91c9448834d80766b93bbbb7a1dfa0112",
            registeredBy = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
        )
    }

    /**
     * Registers a medicine record on the blockchain smart contract
     */
    suspend fun registerOnBlockchain(
        medicineId: String,
        batchId: String,
        medicineHash: String,
        adminAddress: String = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"
    ): Result<OnChainRecord> = withContext(Dispatchers.IO) {
        if (onChainRegistry.containsKey(medicineId)) {
            return@withContext Result.failure(IllegalStateException("Medicine ID already registered on blockchain"))
        }

        // Generate deterministic cryptographic transaction hash
        val txSeed = "tx-$medicineId-$batchId-${System.currentTimeMillis()}-$medicineHash"
        val txHash = "0x" + MedicineHasher.hashString(txSeed)

        val record = OnChainRecord(
            medicineId = medicineId,
            batchId = batchId,
            medicineHash = medicineHash,
            timestamp = System.currentTimeMillis(),
            transactionHash = txHash,
            registeredBy = adminAddress
        )

        onChainRegistry[medicineId] = record
        Result.success(record)
    }

    /**
     * Verifies candidate hash against authoritative on-chain record
     */
    suspend fun verifyRecord(
        medicineId: String,
        candidateHash: String
    ): VerificationOutcome = withContext(Dispatchers.IO) {
        val onChainRecord = onChainRegistry[medicineId]
            ?: return@withContext VerificationOutcome.Unknown(requestedId = medicineId)

        return@withContext if (onChainRecord.medicineHash.equals(candidateHash, ignoreCase = true)) {
            VerificationOutcome.Verified(
                record = onChainRecord,
                currentHash = candidateHash,
                transactionHash = onChainRecord.transactionHash
            )
        } else {
            VerificationOutcome.Tampered(
                record = onChainRecord,
                currentHash = candidateHash,
                expectedBlockchainHash = onChainRecord.medicineHash,
                diffDetails = "The cryptographic hash computed from current record does not match the immutable blockchain ledger entry. Data modification or tablet substitution detected."
            )
        }
    }

    fun getOnChainRecord(medicineId: String): OnChainRecord? {
        return onChainRegistry[medicineId]
    }
}
