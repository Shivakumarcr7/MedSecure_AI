package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiMedicineAssistant
import com.example.data.MedSecureDatabase
import com.example.data.model.MedicineEntity
import com.example.data.model.VerificationLogEntity
import com.example.data.repository.MedicineRepository
import com.example.data.repository.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI, SYSTEM
}

class MedSecureViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MedSecureDatabase.getInstance(application, viewModelScope)
    val repository = MedicineRepository(database.medicineDao(), database.verificationLogDao())

    // Observables
    val allMedicines: StateFlow<List<MedicineEntity>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<VerificationLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<VerificationLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMedicinesCount: StateFlow<Int> = repository.totalMedicinesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVerificationsCount: StateFlow<Int> = repository.totalVerificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val successfulVerificationsCount: StateFlow<Int> = repository.successfulVerificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedVerificationsCount: StateFlow<Int> = repository.failedVerificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unknownVerificationsCount: StateFlow<Int> = repository.unknownVerificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Auth State (Supabase Auth / Admin session)
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminEmail = MutableStateFlow<String?>(null)
    val adminEmail: StateFlow<String?> = _adminEmail.asStateFlow()

    // Verification State
    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    // AI Assistant State
    private val _selectedMedicine = MutableStateFlow<MedicineEntity?>(null)
    val selectedMedicine: StateFlow<MedicineEntity?> = _selectedMedicine.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Registration UI State
    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    private val _lastRegisteredMedicine = MutableStateFlow<MedicineEntity?>(null)
    val lastRegisteredMedicine: StateFlow<MedicineEntity?> = _lastRegisteredMedicine.asStateFlow()

    init {
        // Initial setup
        resetAssistantGreeting()
    }

    fun loginAdmin(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.isNotBlank()) {
            _isAdminLoggedIn.value = true
            _adminEmail.value = email.trim()
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _adminEmail.value = null
    }

    fun performVerification(medicineId: String) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationResult.value = null
            // Slight natural delay for scanning & cryptographic verification visualization
            kotlinx.coroutines.delay(600)
            val result = repository.verifyMedicine(medicineId)
            _verificationResult.value = result
            _isVerifying.value = false

            // Auto-select for AI context if found
            when (result) {
                is VerificationResult.Success -> selectMedicineForAi(result.medicine)
                is VerificationResult.Tampered -> selectMedicineForAi(result.medicine)
                is VerificationResult.Unknown -> {}
            }
        }
    }

    fun selectMedicineForAi(medicine: MedicineEntity) {
        _selectedMedicine.value = medicine
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.SYSTEM,
                text = "MedSecure AI is initialized with verified record for ${medicine.activeName} (Batch: ${medicine.batchNumber}).\n\nAsk any question about therapeutic indications, dosage guidance, or common side effects."
            )
        )
    }

    fun resetAssistantGreeting() {
        if (_chatMessages.value.isEmpty()) {
            _chatMessages.value = listOf(
                ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = "Welcome to MedSecure AI Assistant. Scan or select a verified medicine to analyze clinical indications, composition, and dosage safely."
                )
            )
        }
    }

    fun sendAiQuestion(userQuery: String) {
        val currentMedicine = _selectedMedicine.value
        if (userQuery.isBlank() || currentMedicine == null) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = userQuery.trim())
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiThinking.value = true
            val replyText = GeminiMedicineAssistant.askAssistant(currentMedicine, userQuery)
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.AI, text = replyText)
            _isAiThinking.value = false
        }
    }

    fun registerNewMedicine(
        name: String,
        composition: String,
        dosage: String,
        uses: String,
        sideEffects: String,
        manufacturer: String,
        batchNumber: String,
        manufacturingDate: String,
        expiryDate: String,
        prescriptionRequired: Boolean,
        onSuccess: (MedicineEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isRegistering.value = true
            val result = repository.registerMedicine(
                name = name,
                composition = composition,
                dosage = dosage,
                uses = uses,
                sideEffects = sideEffects,
                manufacturer = manufacturer,
                batchNumber = batchNumber,
                manufacturingDate = manufacturingDate,
                expiryDate = expiryDate,
                prescriptionRequired = prescriptionRequired
            )
            _isRegistering.value = false
            result.getOrNull()?.let {
                _lastRegisteredMedicine.value = it
                onSuccess(it)
            }
        }
    }

    fun toggleTamperDemo(medicineId: String, enableTamper: Boolean) {
        viewModelScope.launch {
            repository.toggleTamperDemo(medicineId, enableTamper)
            // If the current verified screen is showing this medicine, re-verify to demonstrate the tamper detection immediately
            val cur = _verificationResult.value
            if (cur != null) {
                performVerification(medicineId)
            }
        }
    }
}
