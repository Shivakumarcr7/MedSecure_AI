-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: profiles
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'user' CHECK (role IN ('admin', 'user', 'auditor')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Table: medicines
CREATE TABLE IF NOT EXISTS medicines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    medicine_id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    composition TEXT NOT NULL,
    dosage TEXT NOT NULL,
    uses TEXT NOT NULL,
    side_effects TEXT NOT NULL,
    prescription_required BOOLEAN NOT NULL DEFAULT false,
    manufacturer TEXT NOT NULL,
    batch_number TEXT NOT NULL,
    manufacturing_date TEXT NOT NULL,
    expiry_date TEXT NOT NULL,
    medicine_hash TEXT NOT NULL,
    blockchain_tx_hash TEXT,
    blockchain_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (blockchain_status IN ('REGISTERED', 'PENDING', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_medicines_medicine_id ON medicines (medicine_id);
CREATE INDEX IF NOT EXISTS idx_medicines_batch_number ON medicines (batch_number);

-- Table: verification_logs
CREATE TABLE IF NOT EXISTS verification_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    medicine_id TEXT NOT NULL,
    verification_result TEXT NOT NULL CHECK (verification_result IN ('VERIFIED', 'FAILED', 'UNKNOWN')),
    database_hash TEXT,
    blockchain_hash TEXT,
    blockchain_status TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_verification_logs_medicine_id ON verification_logs (medicine_id);
CREATE INDEX IF NOT EXISTS idx_verification_logs_timestamp ON verification_logs (timestamp DESC);

-- Enable Row Level Security (RLS)
ALTER TABLE medicines ENABLE ROW LEVEL SECURITY;
ALTER TABLE verification_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Public can read medicines and verification logs
CREATE POLICY "Allow public read on medicines" ON medicines FOR SELECT USING (true);
CREATE POLICY "Allow public read on verification_logs" ON verification_logs FOR SELECT USING (true);
CREATE POLICY "Allow public insert on verification_logs" ON verification_logs FOR INSERT WITH CHECK (true);

-- Only authenticated admins can insert/update medicines
CREATE POLICY "Allow admin write on medicines" ON medicines FOR ALL USING (
    auth.role() = 'authenticated'
);
