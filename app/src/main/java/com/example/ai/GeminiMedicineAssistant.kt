package com.example.ai

import com.example.BuildConfig
import com.example.data.model.MedicineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiMedicineAssistant {

    const val DISCLAIMER = "MedSecure AI provides informational assistance and does not replace advice from a qualified healthcare professional."

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val SYSTEM_INSTRUCTION = """
        You are a certified, safety-focused medicine information assistant for MedSecure AI.
        
        CRITICAL SAFETY CONSTRAINTS:
        1. Only answer questions using the verified medicine information provided in the context below.
        2. If the required information is unavailable in the provided verified data, explicitly state that it is unavailable.
        3. Never invent, extrapolate, or speculate on medical information.
        4. Do NOT diagnose medical conditions or diseases.
        5. Do NOT provide personalized prescriptions or recommend changing dosage or treatment.
        6. Never advise a patient to discontinue medications without direct physician consultation.
        7. Always remind users that medical decisions must be made with a qualified healthcare professional.
        8. Keep explanations clear, calm, concise, and accessible to non-medical users.
    """.trimIndent()

    suspend fun askAssistant(
        medicine: MedicineEntity,
        userQuery: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val medicineContext = """
            VERIFIED MEDICINE RECORD (MEDSECURE BLOCKCHAIN VERIFIED):
            - Medicine ID: ${medicine.medicineId}
            - Brand Name: ${medicine.activeName}
            - Active Composition: ${medicine.activeComposition}
            - Manufacturer: ${medicine.manufacturer}
            - Batch Number: ${medicine.batchNumber}
            - Standard Dosage: ${medicine.dosage}
            - Verified Therapeutic Uses: ${medicine.uses}
            - Known Side Effects: ${medicine.sideEffects}
            - Prescription Required: ${if (medicine.prescriptionRequired) "Yes (Schedule H / Prescription Drug)" else "No (Over The Counter)"}
            - Expiry Date: ${medicine.expiryDate}
            - Blockchain Status: ${medicine.blockchainStatus}
        """.trimIndent()

        // If a real API key is configured, perform live Gemini call
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val fullPrompt = "$medicineContext\n\nUser Question: $userQuery"

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", fullPrompt))
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", SYSTEM_INSTRUCTION))
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.2) // Low temperature for high factual accuracy
                        put("topP", 0.9)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val reply = parts?.optJSONObject(0)?.optString("text")
                        if (!reply.isNullOrBlank()) {
                            return@withContext reply.trim() + "\n\n⚠️ " + DISCLAIMER
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully on network/quota issues
                e.printStackTrace()
            }
        }

        // Context-grounded intelligent fallback adhering strictly to safety guidelines
        generateSafeLocalResponse(medicine, userQuery)
    }

    private fun generateSafeLocalResponse(medicine: MedicineEntity, query: String): String {
        val q = query.lowercase().trim()

        val answer = when {
            q.contains("use") || q.contains("purpose") || q.contains("what is this for") || q.contains("indication") || q.contains("why") -> {
                "Based on the verified medicine information for ${medicine.activeName}:\n\n" +
                        "• Therapeutic Uses: ${medicine.uses}\n" +
                        "• Active Composition: ${medicine.activeComposition}\n" +
                        "• Prescription Requirement: ${if (medicine.prescriptionRequired) "Yes (Must be prescribed by a physician)" else "Over The Counter (OTC)"}"
            }
            q.contains("side effect") || q.contains("reaction") || q.contains("allergy") || q.contains("harm") || q.contains("danger") -> {
                "According to the registered pharmaceutical record for ${medicine.activeName}:\n\n" +
                        "• Reported Side Effects: ${medicine.sideEffects}\n\n" +
                        "If you experience any severe discomfort or unexpected allergic symptoms, please contact an emergency healthcare provider immediately."
            }
            q.contains("dose") || q.contains("how much") || q.contains("how often") || q.contains("when") || q.contains("take") -> {
                "Standard dosage information listed on the verified record:\n\n" +
                        "• Dosage Guidance: ${medicine.dosage}\n\n" +
                        "Note: Never exceed the recommended dosage. Consult your physician or pharmacist for individualized dosage instructions."
            }
            q.contains("batch") || q.contains("manufacturer") || q.contains("expire") || q.contains("expiry") || q.contains("date") || q.contains("genuine") -> {
                "Verified Manufacturer & Batch Details:\n\n" +
                        "• Manufacturer: ${medicine.manufacturer}\n" +
                        "• Batch Number: ${medicine.batchNumber}\n" +
                        "• Expiry Date: ${medicine.expiryDate}\n" +
                        "• Blockchain Verification: ${medicine.blockchainStatus} (Tamper-proof on-chain record)"
            }
            q.contains("pregnant") || q.contains("child") || q.contains("kid") || q.contains("stop") || q.contains("diagnose") || q.contains("disease") -> {
                "This specialized query requires clinical evaluation. MedSecure AI is strictly constrained from diagnosing or advising on specialized medical conditions. Please consult a licensed medical practitioner before taking this medication."
            }
            else -> {
                "Based on the verified record for ${medicine.activeName} (${medicine.activeComposition}):\n\n" +
                        "• Primary Uses: ${medicine.uses}\n" +
                        "• Standard Dosage: ${medicine.dosage}\n" +
                        "• Key Side Effects: ${medicine.sideEffects}\n" +
                        "• Batch: ${medicine.batchNumber} | Expiry: ${medicine.expiryDate}"
            }
        }

        return "$answer\n\n⚠️ $DISCLAIMER"
    }
}
