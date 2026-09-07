package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.crypto.MedicineHasher
import com.example.data.dao.MedicineDao
import com.example.data.dao.VerificationLogDao
import com.example.data.model.MedicineEntity
import com.example.data.model.VerificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MedicineEntity::class, VerificationLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MedSecureDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun verificationLogDao(): VerificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: MedSecureDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): MedSecureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedSecureDatabase::class.java,
                    "medsecure_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDemoData(database.medicineDao(), database.verificationLogDao())
                    }
                }
            }
        }

        suspend fun populateInitialDemoData(medicineDao: MedicineDao, logDao: VerificationLogDao) {
            val sample1Name = "Paracetamol 500 mg"
            val sample1Comp = "Paracetamol IP 500 mg"
            val sample1Batch = "MED2026A001"
            val sample1Mfg = "Demo Pharma India Pvt. Ltd."
            val sample1Exp = "2028-01-14"
            val sample1Hash = MedicineHasher.computeHash(
                medicineId = "MED-IND-2026-000001",
                batchNumber = sample1Batch,
                manufacturer = sample1Mfg,
                name = sample1Name,
                composition = sample1Comp,
                expiryDate = sample1Exp
            )

            val sample2Name = "Cetirizine 10 mg"
            val sample2Comp = "Cetirizine Hydrochloride IP 10 mg"
            val sample2Batch = "MED2026B042"
            val sample2Mfg = "Apex Healthcare Labs (Demo)"
            val sample2Exp = "2028-02-09"
            val sample2Hash = MedicineHasher.computeHash(
                medicineId = "MED-IND-2026-000002",
                batchNumber = sample2Batch,
                manufacturer = sample2Mfg,
                name = sample2Name,
                composition = sample2Comp,
                expiryDate = sample2Exp
            )

            val sample3Name = "Omeprazole 20 mg"
            val sample3Comp = "Omeprazole Magnesium IP 20 mg"
            val sample3Batch = "MED2026C109"
            val sample3Mfg = "Zenith Life Sciences (Demo)"
            val sample3Exp = "2027-08-31"
            val sample3Hash = MedicineHasher.computeHash(
                medicineId = "MED-IND-2026-000003",
                batchNumber = sample3Batch,
                manufacturer = sample3Mfg,
                name = sample3Name,
                composition = sample3Comp,
                expiryDate = sample3Exp
            )

            val sample4Name = "Azithromycin 500 mg"
            val sample4Comp = "Azithromycin Dihydrate IP equivalent to 500 mg"
            val sample4Batch = "MED2026D770"
            val sample4Mfg = "BioShield Pharmaceuticals (Demo)"
            val sample4Exp = "2027-10-11"
            val sample4Hash = MedicineHasher.computeHash(
                medicineId = "MED-IND-2026-000004",
                batchNumber = sample4Batch,
                manufacturer = sample4Mfg,
                name = sample4Name,
                composition = sample4Comp,
                expiryDate = sample4Exp
            )

            val seeds = listOf(
                MedicineEntity(
                    id = 1,
                    medicineId = "MED-IND-2026-000001",
                    name = sample1Name,
                    composition = sample1Comp,
                    dosage = "1 tablet every 4 to 6 hours as needed (Max 4g/day)",
                    uses = "Relief of mild to moderate pain (headache, body ache) and reduction of fever.",
                    sideEffects = "Rare at normal doses: Nausea, allergic skin reactions. Excessive doses cause severe liver toxicity.",
                    prescriptionRequired = false,
                    manufacturer = sample1Mfg,
                    batchNumber = sample1Batch,
                    manufacturingDate = "2026-01-15",
                    expiryDate = sample1Exp,
                    medicineHash = sample1Hash,
                    blockchainTxHash = "0x7d89b02a3a5c71b659c2394fd5e7300c0be6c8fbc27b0bf9ef4dc8577eb0c821",
                    blockchainStatus = "REGISTERED"
                ),
                MedicineEntity(
                    id = 2,
                    medicineId = "MED-IND-2026-000002",
                    name = sample2Name,
                    composition = sample2Comp,
                    dosage = "1 tablet once daily preferably at bedtime",
                    uses = "Relief of allergy symptoms including allergic rhinitis, runny nose, sneezing, and urticaria/hives.",
                    sideEffects = "Drowsiness, dry mouth, mild headache, tiredness, dizziness.",
                    prescriptionRequired = false,
                    manufacturer = sample2Mfg,
                    batchNumber = sample2Batch,
                    manufacturingDate = "2026-02-10",
                    expiryDate = sample2Exp,
                    medicineHash = sample2Hash,
                    blockchainTxHash = "0x41f92e7381db2ef25bf92289c09c31398c8808dcff32b498e7276537bfa39801",
                    blockchainStatus = "REGISTERED"
                ),
                MedicineEntity(
                    id = 3,
                    medicineId = "MED-IND-2026-000003",
                    name = sample3Name,
                    composition = sample3Comp,
                    dosage = "1 capsule daily in the morning before food",
                    uses = "Treatment of gastroesophageal reflux disease (GERD), heartburn, acid indigestion, and peptic ulcers.",
                    sideEffects = "Headache, abdominal pain, diarrhea, nausea, constipation, flatulence.",
                    prescriptionRequired = false,
                    manufacturer = sample3Mfg,
                    batchNumber = sample3Batch,
                    manufacturingDate = "2026-03-01",
                    expiryDate = sample3Exp,
                    medicineHash = sample3Hash,
                    blockchainTxHash = "0x9c31168cb769188a10058b88d8442e5a6104bc18a4d7d9198647cc9d8d67240c",
                    blockchainStatus = "REGISTERED"
                ),
                MedicineEntity(
                    id = 4,
                    medicineId = "MED-IND-2026-000004",
                    name = sample4Name,
                    composition = sample4Comp,
                    dosage = "1 tablet daily for 3 to 5 days as prescribed by physician",
                    uses = "Antibiotic prescribed for bacterial respiratory infections, sinusitis, throat infections, and skin infections.",
                    sideEffects = "Diarrhea, nausea, vomiting, abdominal cramps, altered taste.",
                    prescriptionRequired = true,
                    manufacturer = sample4Mfg,
                    batchNumber = sample4Batch,
                    manufacturingDate = "2026-04-12",
                    expiryDate = sample4Exp,
                    medicineHash = sample4Hash,
                    blockchainTxHash = "0x2e08c02c6b4fa7bf5132204c3f58a8a91c9448834d80766b93bbbb7a1dfa0112",
                    blockchainStatus = "REGISTERED"
                )
            )

            medicineDao.insertMedicines(seeds)

            val initialLogs = listOf(
                VerificationLogEntity(
                    medicineId = "MED-IND-2026-000001",
                    verificationResult = "VERIFIED",
                    databaseHash = sample1Hash,
                    blockchainHash = sample1Hash,
                    blockchainStatus = "REGISTERED",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                ),
                VerificationLogEntity(
                    medicineId = "MED-IND-2026-000002",
                    verificationResult = "VERIFIED",
                    databaseHash = sample2Hash,
                    blockchainHash = sample2Hash,
                    blockchainStatus = "REGISTERED",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45
                ),
                VerificationLogEntity(
                    medicineId = "MED-TEST-TAMPERED",
                    verificationResult = "FAILED",
                    databaseHash = "9f83...modified",
                    blockchainHash = "4a12...original",
                    blockchainStatus = "TAMPER_DETECTED",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15
                ),
                VerificationLogEntity(
                    medicineId = "MED-FAKE-999999",
                    verificationResult = "UNKNOWN",
                    databaseHash = null,
                    blockchainHash = null,
                    blockchainStatus = "NOT_FOUND",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5
                )
            )

            for (log in initialLogs) {
                logDao.insertLog(log)
            }
        }
    }
}
