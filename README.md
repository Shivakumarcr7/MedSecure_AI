# 💊 MedSecure AI

### Scan. Identify. Verify. Stay Safe.

MedSecure AI is an **AI + Blockchain-based medicine identification and verification platform** designed to help users identify medicines and verify their associated registered medicine/batch records.

The project addresses a common problem where individual tablets may be separated from their original medicine strip, making identification and verification difficult.

MedSecure AI combines:

* 📱 QR Code scanning
* 🧠 AI-powered medicine assistance
* 🔐 Cryptographic hashing
* ⛓️ Blockchain verification
* 🗄️ Supabase database
* 🔑 Secure admin authentication

> **Important:** MedSecure AI verifies the associated registered medicine/batch record. It does **not physically prove that an individual loose tablet is genuine**.

---

## 🚀 Features

### 👤 User Features

* Scan medicine QR codes
* Manually enter Medicine ID
* Identify registered medicines
* View medicine information
* Verify medicine records using blockchain
* Detect modified/tampered records
* Detect unknown medicine IDs
* View blockchain transaction information
* Ask questions to the MedSecure AI assistant
* Use the application without creating an account

### 👨‍💼 Admin Features

* Secure admin login
* Register medicines
* Generate unique Medicine IDs
* Generate QR codes
* Download and print QR codes
* Generate SHA-256 medicine hashes
* Register medicine hashes on blockchain
* View registered medicines
* View verification history
* View verification statistics
* Demonstrate tampering detection

---

# 🧩 Problem

When medicine strips are cut and individual tablets are sold, important information from the original packaging may no longer be available.

Users may have difficulty answering:

> What medicine is this?

> What is it used for?

> Is this medicine record registered?

> Has the registered information been modified?

MedSecure AI provides a digital verification workflow using QR codes, cryptographic hashing and blockchain.

---

# 💡 Solution

The platform follows this workflow:

```text
                ADMIN
                  │
                  ▼
        Register Medicine
                  │
                  ▼
       Generate Medicine ID
                  │
                  ▼
        Generate SHA-256 Hash
                  │
                  ▼
       Register Hash on Blockchain
                  │
                  ▼
            Generate QR
                  │
                  ▼
             USER SCANS
                  │
                  ▼
        Retrieve Medicine Record
                  │
                  ▼
       Generate Hash Again
                  │
                  ▼
       Compare With Blockchain
                  │
          ┌───────┴───────┐
          ▼               ▼
       MATCH          DIFFERENT
          │               │
          ▼               ▼
      VERIFIED          FAILED
          │
          ▼
 Medicine Information
          │
          ▼
    MedSecure AI
```

---

# 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │      Next.js        │
                         │ Frontend + Backend  │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
      │   Supabase   │      │  Blockchain  │      │   Gemini AI  │
      │ PostgreSQL   │      │ Hardhat /    │      │   / LLM API  │
      │ + Auth       │      │   Sepolia    │      │              │
      └──────────────┘      └──────────────┘      └──────────────┘
              │                     │
              ▼                     ▼
      Medicine Records       Medicine Hashes
      Verification Logs      Blockchain Records
      Admin Profiles         Transaction Hash
```

---

# 🔐 Verification Architecture

The core verification mechanism is based on cryptographic hashes.

```text
Medicine Record
      │
      ▼
Canonical Data
      │
      ▼
   SHA-256
      │
      ▼
 Current Hash
      │
      │
      ├─────────────── Compare ───────────────┐
      │                                       │
      ▼                                       ▼
Database Hash                         Blockchain Hash
      │                                       │
      └────────────────┬──────────────────────┘
                       │
                       ▼
                   Verification
                       │
              ┌────────┼────────┐
              ▼        ▼        ▼
           VERIFIED   FAILED   UNKNOWN
```

If the current medicine data produces the same hash stored on the blockchain:

```text
✓ VERIFIED MEDICINE
```

If the hashes are different:

```text
⚠ VERIFICATION FAILED
```

If the medicine ID does not exist:

```text
⚠ UNKNOWN MEDICINE
```

---

# 🛠️ Tech Stack

## Frontend

* Next.js
* TypeScript
* Tailwind CSS
* shadcn/ui

## Backend

* Next.js App Router
* Route Handlers
* Server-side functions

## Database

* Supabase PostgreSQL

## Authentication

* Supabase Auth

## Blockchain

* Solidity
* Hardhat
* ethers.js
* Ethereum Sepolia
* Local Hardhat Network

## QR

* QR generation library
* Browser-compatible QR scanner

## AI

* Gemini API / configurable LLM API

## Deployment

* Vercel-compatible

---

# 📂 Project Structure

```text
medsecure-ai/
│
├── app/
│   ├── page.tsx
│   ├── scan/
│   ├── verify/
│   ├── medicine/
│   ├── assistant/
│   ├── login/
│   └── admin/
│       ├── page.tsx
│       ├── register/
│       ├── medicines/
│       └── verifications/
│
├── components/
│   ├── ui/
│   ├── qr-scanner.tsx
│   ├── qr-generator.tsx
│   ├── medicine-card.tsx
│   ├── verification-status.tsx
│   └── blockchain-status.tsx
│
├── lib/
│   ├── supabase/
│   ├── blockchain/
│   ├── hashing/
│   └── ai/
│
├── blockchain/
│   ├── contracts/
│   │   └── MedicineRegistry.sol
│   ├── scripts/
│   ├── test/
│   └── hardhat.config.ts
│
├── supabase/
│   ├── migrations/
│   └── seed.sql
│
├── public/
│
├── .env.example
├── .gitignore
├── README.md
└── package.json
```

---

# 🗺️ Application Routes

| Route                    | Description           |
| ------------------------ | --------------------- |
| `/`                      | Landing page          |
| `/scan`                  | QR scanner            |
| `/verify/[medicineId]`   | Medicine verification |
| `/medicine/[medicineId]` | Medicine information  |
| `/assistant`             | AI assistant          |
| `/login`                 | Admin login           |
| `/admin`                 | Admin dashboard       |
| `/admin/register`        | Register medicine     |
| `/admin/medicines`       | Medicine management   |
| `/admin/verifications`   | Verification history  |

---

# 🗄️ Database

The application uses Supabase PostgreSQL.

## `medicines`

```text
id
medicine_id
name
composition
dosage
uses
side_effects
prescription_required
manufacturer
batch_number
manufacturing_date
expiry_date
medicine_hash
blockchain_tx_hash
blockchain_status
created_at
updated_at
```

## `verification_logs`

```text
id
medicine_id
verification_result
database_hash
blockchain_hash
blockchain_status
timestamp
```

## `profiles`

```text
id
email
role
created_at
```

Row Level Security should be enabled to protect administrative data.

---

# 🔑 Environment Variables

Create a `.env.local` file.

```env
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=

BLOCKCHAIN_RPC_URL=
BLOCKCHAIN_PRIVATE_KEY=
CONTRACT_ADDRESS=

AI_API_KEY=
```

### Variables

| Variable                        | Purpose                         |
| ------------------------------- | ------------------------------- |
| `NEXT_PUBLIC_SUPABASE_URL`      | Supabase project URL            |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Supabase public anonymous key   |
| `BLOCKCHAIN_RPC_URL`            | Blockchain RPC endpoint         |
| `BLOCKCHAIN_PRIVATE_KEY`        | Server-side blockchain wallet   |
| `CONTRACT_ADDRESS`              | Deployed smart contract address |
| `AI_API_KEY`                    | Gemini/LLM API key              |

> ⚠️ Never expose `BLOCKCHAIN_PRIVATE_KEY` or `AI_API_KEY` to the browser.

---

# ⚙️ Installation

## 1. Clone Repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd medsecure-ai
```

## 2. Install Dependencies

```bash
npm install
```

## 3. Configure Environment

```bash
cp .env.example .env.local
```

Add your credentials to `.env.local`.

## 4. Start Development Server

```bash
npm run dev
```

Open:

```text
http://localhost:3000
```

---

# ⛓️ Blockchain Setup

MedSecure AI supports both:

* Local Hardhat blockchain
* Ethereum Sepolia testnet

## Compile Smart Contract

```bash
npx hardhat compile
```

## Run Blockchain Tests

```bash
npx hardhat test
```

## Start Local Blockchain

```bash
npx hardhat node
```

## Deploy Locally

```bash
npx hardhat run blockchain/scripts/deploy.ts --network localhost
```

Copy the resulting contract address into:

```env
CONTRACT_ADDRESS=
```

---

# 🌐 Sepolia Deployment

Configure:

```env
BLOCKCHAIN_RPC_URL=
BLOCKCHAIN_PRIVATE_KEY=
```

Then deploy:

```bash
npx hardhat run blockchain/scripts/deploy.ts --network sepolia
```

After deployment, set:

```env
CONTRACT_ADDRESS=<DEPLOYED_CONTRACT_ADDRESS>
```

> Never commit your blockchain private key to GitHub.

---

# 📜 Smart Contract

The smart contract is located at:

```text
blockchain/contracts/MedicineRegistry.sol
```

The contract stores:

```text
medicineId
batchId
medicineHash
timestamp
registeredBy
```

Core functionality includes:

```solidity
registerMedicine(
    string medicineId,
    string batchId,
    bytes32 medicineHash
)
```

and:

```solidity
getMedicineRecord(
    string medicineId
)
```

The contract should prevent:

* Unauthorized registration
* Duplicate medicine registration

It should emit events such as:

```text
MedicineRegistered
MedicineVerified
```

---

# 🧪 Medicine Registration

An administrator can register:

```text
Medicine Name
Composition
Dosage
Uses
Side Effects
Manufacturer
Batch Number
Manufacturing Date
Expiry Date
Prescription Required
```

Example:

```text
Medicine:
Paracetamol 500 mg

Composition:
Paracetamol 500 mg

Manufacturer:
Demo Pharma Pvt. Ltd.

Batch:
MED2026A001
```

The system generates a unique identifier:

```text
MED-IND-2026-000001
```

---

# 🔗 QR Code

Each registered medicine receives a QR code.

Example:

```text
https://YOUR_DOMAIN/verify/MED-IND-2026-000001
```

The QR code contains the:

```text
Medicine ID / Verification URL
```

It does **not** contain the complete medicine record.

Users can scan the QR code using:

```text
/scan
```

A manual Medicine ID input is also available for desktop demonstrations.

---

# 🔍 Medicine Verification

When a user scans a medicine QR:

```text
QR Code
   ↓
Medicine ID
   ↓
Supabase
   ↓
Medicine Record
   ↓
SHA-256
   ↓
Current Hash
   ↓
Blockchain
   ↓
Registered Hash
   ↓
Compare
```

### Verified

```text
✓ VERIFIED MEDICINE

Medicine record matches the blockchain record.
```

### Failed

```text
⚠ VERIFICATION FAILED

The current medicine information does not
match the blockchain-registered record.

Possible tampering detected.
```

### Unknown

```text
⚠ UNKNOWN MEDICINE

No registered medicine record was found.
```

Every verification attempt should be stored in `verification_logs`.

---

# 🧬 Tamper Detection Demo

One of the key demonstrations is blockchain-based tamper detection.

### Original

```text
Original Medicine
       ↓
Hash = ABC123
       ↓
Blockchain = ABC123
       ↓
✓ VERIFIED
```

### Modified

```text
Modified Medicine
       ↓
Hash = XYZ789
       ↓
Blockchain = ABC123
       ↓
⚠ VERIFICATION FAILED
```

The demo should use a controlled mechanism so that production records are not accidentally modified.

---

# 🤖 AI Assistant

The AI assistant is available at:

```text
/assistant
```

The assistant receives the **verified medicine information** as context.

Example:

```text
User:
What is this medicine used for?

AI:
Based on the verified medicine information,
this medicine is used for...
```

The assistant must only answer using information supplied by the application.

---

# 🛡️ AI Safety

MedSecure AI must not:

* Diagnose diseases
* Create personalized prescriptions
* Change prescribed dosage
* Tell users to stop medication
* Recommend prescription medicines without professional guidance
* Invent medicine information

The AI should follow rules equivalent to:

```text
You are a medicine information assistant.

Only answer using the verified medicine information
provided by the application.

If the required information is unavailable,
say that it is unavailable.

Never invent medical information.

Do not diagnose or prescribe.

Do not recommend changing dosage or treatment.

Always remind users that medical decisions should be
made with a qualified healthcare professional.
```

The application should display:

> MedSecure AI provides informational assistance and does not replace advice from a qualified healthcare professional.

---

# 👨‍💼 Admin Dashboard

The dashboard displays:

```text
Total Medicines
Total Verifications
Successful Verifications
Failed Verifications
Unknown Medicines
```

Recent activity:

```text
MED-001   ✓ Verified
MED-002   ✓ Verified
MED-003   ⚠ Failed
MED-004   ? Unknown
```

Admins can manage registered medicines and view verification activity.

---

# 🧪 Demo Data

The MVP can be seeded with demonstration medicines:

```text
Paracetamol 500 mg
Cetirizine 10 mg
Omeprazole 20 mg
Azithromycin 500 mg
```

Example demo manufacturer:

```text
Demo Pharma Pvt. Ltd.
```

> ⚠️ These records are demonstration data and must not be presented as official pharmaceutical records.

---

# 🧪 Testing

Important functionality should be tested.

## Hashing

```text
Same data
   ↓
Same hash
```

```text
Modified data
   ↓
Different hash
```

## Blockchain

Test:

* Medicine registration
* Medicine retrieval
* Duplicate registration rejection
* Admin authorization

## Verification

Test:

```text
Valid medicine → VERIFIED
Unknown ID     → UNKNOWN
Modified data  → VERIFICATION FAILED
```

## AI

Test:

* Verified medicine context is passed to the AI.
* The AI does not invent information.
* The AI does not diagnose.
* The AI does not prescribe.
* Missing information is reported as unavailable.

Run:

```bash
npm test
```

and:

```bash
npx hardhat test
```

---

# 🔒 Security

MedSecure AI should implement:

* Server-side validation
* Input sanitization
* Supabase Row Level Security
* Admin authorization
* Server-only blockchain private key
* Server-only AI API key
* No secrets in client components
* No private keys in Git
* `.env.local` in `.gitignore`

Never commit:

```text
.env.local
API keys
Private keys
RPC credentials
Service-role secrets
```

---

# ⚠️ Medical Disclaimer

MedSecure AI is a prototype.

> This prototype uses demonstration medicine data and is intended for educational and research purposes. It does not replace official pharmaceutical verification systems or professional medical advice.

The project does not claim:

* Government approval
* Pharmaceutical manufacturer certification
* Clinical validation
* Regulatory approval

---

# 🎯 Complete Demo

The complete MVP demonstration should follow this sequence:

### Admin

```text
Login
  ↓
Register Medicine
  ↓
Generate Medicine ID
  ↓
Generate SHA-256 Hash
  ↓
Register Hash on Blockchain
  ↓
Generate QR
```

### User

```text
Scan QR
  ↓
Retrieve Medicine
  ↓
Recalculate Hash
  ↓
Compare With Blockchain
  ↓
✓ VERIFIED
  ↓
View Medicine Information
  ↓
Ask MedSecure AI
```

### Tampering Demo

```text
Modify Demo Record
  ↓
Recalculate Hash
  ↓
Compare With Original Blockchain Hash
  ↓
⚠ VERIFICATION FAILED
```

### Unknown Demo

```text
Invalid Medicine ID
  ↓
⚠ UNKNOWN MEDICINE
```

---

# ✅ Success Criteria

The project is considered functional when the following workflow works:

* [x] Run the application locally
* [x] Login as admin
* [x] Register a medicine
* [x] Generate a unique Medicine ID
* [x] Generate a QR code
* [x] Generate a SHA-256 hash
* [x] Register the hash on blockchain
* [x] Scan the QR
* [x] Retrieve the medicine
* [x] Verify the blockchain record
* [x] Display `VERIFIED`
* [x] Demonstrate modified data
* [x] Display `VERIFICATION FAILED`
* [x] Test an unknown medicine
* [x] Ask the AI assistant about a verified medicine
* [x] View verification history
* [x] View the admin dashboard

---

# 🚧 Limitations

This is an MVP and intentionally does not include:

* Real pharmaceutical company integrations
* Government databases
* Payment systems
* Large-scale supply-chain infrastructure
* Native Android/iOS applications
* AI model training
* Kubernetes or microservices
* Production pharmaceutical authentication infrastructure

The focus is a **functional, understandable and demonstrable prototype**.

---

# 🗺️ Development Roadmap

```text
Phase 1   Project Initialization
    ↓
Phase 2   UI & Routing
    ↓
Phase 3   Supabase
    ↓
Phase 4   Medicine Registration
    ↓
Phase 5   QR Generation
    ↓
Phase 6   QR Scanner
    ↓
Phase 7   Verification Page
    ↓
Phase 8   Hashing
    ↓
Phase 9   Smart Contract
    ↓
Phase 10  Blockchain Registration
    ↓
Phase 11  Blockchain Verification
    ↓
Phase 12  Tamper Detection
    ↓
Phase 13  AI Assistant
    ↓
Phase 14  Authentication
    ↓
Phase 15  Admin Dashboard
    ↓
Phase 16  Testing
    ↓
Phase 17  UI Polish
    ↓
Phase 18  Deployment
```

---

# 🚀 Deployment

The application is designed to be Vercel-compatible.

Before deployment:

1. Configure Supabase.
2. Deploy the smart contract.
3. Configure the blockchain RPC.
4. Configure the server-side wallet.
5. Configure the AI API.
6. Add production environment variables.
7. Verify database security policies.
8. Test QR verification.
9. Test blockchain verification.
10. Test AI functionality.

Never expose private keys or API secrets in frontend code.

---

# 📄 License

This project is intended as an educational and research MVP.

Add your preferred license before publishing the repository publicly.

---

## 💊 MedSecure AI

**Scan. Identify. Verify. Stay Safe.**
