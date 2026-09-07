package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId LIMIT 1")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId LIMIT 1")
    fun getMedicineByIdFlow(medicineId: String): Flow<MedicineEntity?>

    @Query("SELECT * FROM medicines WHERE batchNumber = :batchNumber LIMIT 1")
    suspend fun getMedicineByBatch(batchNumber: String): MedicineEntity?

    @Query("SELECT COUNT(*) FROM medicines")
    fun getMedicineCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET isTamperedDemo = :isTampered, tamperedName = :tamperedName, tamperedComposition = :tamperedComposition, updatedAt = :timestamp WHERE medicineId = :medicineId")
    suspend fun setTamperState(
        medicineId: String,
        isTampered: Boolean,
        tamperedName: String?,
        tamperedComposition: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT MAX(id) FROM medicines")
    suspend fun getMaxId(): Long?
}
