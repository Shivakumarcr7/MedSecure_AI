package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VerificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationLogDao {

    @Query("SELECT * FROM verification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VerificationLogEntity>>

    @Query("SELECT * FROM verification_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 10): Flow<List<VerificationLogEntity>>

    @Query("SELECT COUNT(*) FROM verification_logs")
    fun getTotalVerificationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM verification_logs WHERE verificationResult = 'VERIFIED'")
    fun getSuccessfulVerificationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM verification_logs WHERE verificationResult = 'FAILED'")
    fun getFailedVerificationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM verification_logs WHERE verificationResult = 'UNKNOWN'")
    fun getUnknownVerificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VerificationLogEntity): Long

    @Query("DELETE FROM verification_logs")
    suspend fun clearLogs()
}
