package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.MedicineHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MedSecure AI", appName)
  }

  @Test
  fun `cryptographic medicine hashing is deterministic`() {
    val hash1 = MedicineHasher.computeHash(
      medicineId = "MED-IND-2026-000001",
      batchNumber = "MED2026A001",
      manufacturer = "Demo Pharma India Pvt. Ltd.",
      name = "Paracetamol 500 mg",
      composition = "Paracetamol IP 500 mg",
      expiryDate = "2028-01-14"
    )

    val hash2 = MedicineHasher.computeHash(
      medicineId = "MED-IND-2026-000001",
      batchNumber = "MED2026A001",
      manufacturer = "Demo Pharma India Pvt. Ltd.",
      name = "Paracetamol 500 mg",
      composition = "Paracetamol IP 500 mg",
      expiryDate = "2028-01-14"
    )

    // Same attributes must generate identical cryptographic SHA-256 hash
    assertEquals(hash1, hash2)
    assertEquals(64, hash1.length)

    // Altering a single attribute must produce completely different hash (Avalanche effect)
    val tamperedHash = MedicineHasher.computeHash(
      medicineId = "MED-IND-2026-000001",
      batchNumber = "MED2026A001",
      manufacturer = "Demo Pharma India Pvt. Ltd.",
      name = "Paracetamol 250 mg",
      composition = "Paracetamol IP 250 mg",
      expiryDate = "2028-01-14"
    )

    assertNotEquals(hash1, tamperedHash)
  }
}
