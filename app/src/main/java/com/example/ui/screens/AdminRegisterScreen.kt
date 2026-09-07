package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.MedicineHasher
import com.example.data.model.MedicineEntity
import com.example.qr.QrCodeGenerator
import com.example.ui.components.HashViewer
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.MedTealPrimary

@Composable
fun AdminRegisterScreen(
    isRegistering: Boolean,
    onRegisterSubmit: (
        name: String,
        composition: String,
        dosage: String,
        uses: String,
        sideEffects: String,
        manufacturer: String,
        batchNumber: String,
        mfgDate: String,
        expDate: String,
        prescriptionRequired: Boolean,
        onSuccess: (MedicineEntity) -> Unit
    ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMedicineDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var composition by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var uses by remember { mutableStateOf("") }
    var sideEffects by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var mfgDate by remember { mutableStateOf("2026-03-01") }
    var expDate by remember { mutableStateOf("2028-02-28") }
    var prescriptionRequired by remember { mutableStateOf(false) }

    var formError by remember { mutableStateOf<String?>(null) }
    var registeredMedicineDialog by remember { mutableStateOf<MedicineEntity?>(null) }
    var dialogQrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val scrollState = rememberScrollState()

    // Live preview of deterministic canonical hash
    val previewHash by remember {
        derivedStateOf {
            if (name.isNotBlank() && batchNumber.isNotBlank() && manufacturer.isNotBlank()) {
                MedicineHasher.computeHash(
                    medicineId = "MED-IND-2026-PREVIEW",
                    batchNumber = batchNumber,
                    manufacturer = manufacturer,
                    name = name,
                    composition = composition,
                    expiryDate = expDate
                )
            } else {
                "Fill required fields to preview canonical SHA-256..."
            }
        }
    }

    fun submit() {
        if (name.isBlank() || composition.isBlank() || dosage.isBlank() ||
            uses.isBlank() || manufacturer.isBlank() || batchNumber.isBlank()
        ) {
            formError = "Please fill in all required clinical and batch fields."
            return
        }

        formError = null
        onRegisterSubmit(
            name,
            composition,
            dosage,
            uses,
            sideEffects.ifBlank { "None reported when taken as prescribed." },
            manufacturer,
            batchNumber,
            mfgDate,
            expDate,
            prescriptionRequired
        ) { registeredEntity ->
            dialogQrBitmap = QrCodeGenerator.generateQrBitmap(
                QrCodeGenerator.getVerificationUrl(registeredEntity.medicineId),
                sizePx = 350
            )
            registeredMedicineDialog = registeredEntity
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("admin_register_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("admin_register_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Register Medicine",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cryptographic hashing & smart contract anchor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Registration Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CLINICAL SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Brand / Commercial Name *") },
                    placeholder = { Text("e.g. Amoxicillin 500 mg") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = composition,
                    onValueChange = { composition = it },
                    label = { Text("Active Chemical Composition *") },
                    placeholder = { Text("e.g. Amoxicillin Trihydrate IP 500 mg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_composition_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Standard Dosage Guidance *") },
                    placeholder = { Text("e.g. 1 capsule every 8 hours with water") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uses,
                    onValueChange = { uses = it },
                    label = { Text("Therapeutic Indications / Uses *") },
                    placeholder = { Text("e.g. Treatment of bacterial infections") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sideEffects,
                    onValueChange = { sideEffects = it },
                    label = { Text("Known Side Effects") },
                    placeholder = { Text("e.g. Mild nausea, diarrhea, skin rash") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = prescriptionRequired,
                        onCheckedChange = { prescriptionRequired = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Requires Prescription (Schedule H)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MANUFACTURING & BATCH TRACEABILITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Licensed Manufacturer *") },
                    placeholder = { Text("e.g. Sun Biotech Pharma India Ltd.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Manufacturer Batch Number *") },
                    placeholder = { Text("e.g. BATCH-2026-X99") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = mfgDate,
                        onValueChange = { mfgDate = it },
                        label = { Text("Mfg Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = expDate,
                        onValueChange = { expDate = it },
                        label = { Text("Expiry Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Preview Hash
                Text(
                    text = "LIVE CANONICAL SHA-256 HASH PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                HashViewer(label = "Deterministic Hash (Calculated on the fly)", hash = previewHash)

                if (formError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { submit() },
                    enabled = !isRegistering,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_registration_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isRegistering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Registering on Blockchain...")
                    } else {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Commit Record to Blockchain Ledger", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        MedicalDisclaimerCard()

        // Registration Success Dialog
        if (registeredMedicineDialog != null) {
            val reg = registeredMedicineDialog!!
            AlertDialog(
                onDismissRequest = { registeredMedicineDialog = null },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = reg.medicineId
                            registeredMedicineDialog = null
                            onNavigateToMedicineDetail(id)
                        }
                    ) {
                        Text("View Registered Medicine & QR")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { registeredMedicineDialog = null }) {
                        Text("Close")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MedTealPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text("Medicine Registered Successfully!", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Assigned ID: ${reg.medicineId}\nBatch: ${reg.batchNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (dialogQrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = dialogQrBitmap!!.asImageBitmap(),
                                    contentDescription = "New QR",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tx Hash: ${reg.blockchainTxHash}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            )
        }
    }
}
