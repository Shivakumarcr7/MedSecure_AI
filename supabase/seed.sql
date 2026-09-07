-- MedSecure AI - Demonstration Seed Data
-- Clearly labeled as sample/demo data for educational and research purposes.

INSERT INTO profiles (email, role) VALUES
('admin@medsecure.ai', 'admin'),
('inspector@pharmagov.in', 'auditor')
ON CONFLICT (email) DO NOTHING;

INSERT INTO medicines (
    medicine_id,
    name,
    composition,
    dosage,
    uses,
    side_effects,
    prescription_required,
    manufacturer,
    batch_number,
    manufacturing_date,
    expiry_date,
    medicine_hash,
    blockchain_tx_hash,
    blockchain_status
) VALUES
(
    'MED-IND-2026-000001',
    'Paracetamol 500 mg',
    'Paracetamol IP 500 mg',
    '1 tablet every 4 to 6 hours as needed (Maximum 4g/day)',
    'Relief of mild to moderate pain (headache, body ache) and reduction of fever.',
    'Rare when taken as directed: Nausea, allergic skin reactions. High doses cause liver toxicity.',
    false,
    'Demo Pharma India Pvt. Ltd.',
    'MED2026A001',
    '2026-01-15',
    '2028-01-14',
    'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    '0x7d89b02a3a5c71b659c2394fd5e7300c0be6c8fbc27b0bf9ef4dc8577eb0c821',
    'REGISTERED'
),
(
    'MED-IND-2026-000002',
    'Cetirizine 10 mg',
    'Cetirizine Hydrochloride IP 10 mg',
    '1 tablet once daily preferably at bedtime',
    'Relief of allergy symptoms including allergic rhinitis, runny nose, sneezing, and urticaria/hives.',
    'Drowsiness, fatigue, dry mouth, dizziness, mild headache.',
    false,
    'Apex Healthcare Labs (Demo)',
    'MED2026B042',
    '2026-02-10',
    '2028-02-09',
    '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4',
    '0x41f92e7381db2ef25bf92289c09c31398c8808dcff32b498e7276537bfa39801',
    'REGISTERED'
),
(
    'MED-IND-2026-000003',
    'Omeprazole 20 mg',
    'Omeprazole Magnesium IP 20 mg',
    '1 capsule daily in the morning before food',
    'Treatment of gastroesophageal reflux disease (GERD), heartburn, acid indigestion, and peptic ulcers.',
    'Headache, abdominal pain, diarrhea, nausea, constipation, flatulence.',
    false,
    'Zenith Life Sciences (Demo)',
    'MED2026C109',
    '2026-03-01',
    '2027-08-31',
    'a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e',
    '0x9c31168cb769188a10058b88d8442e5a6104bc18a4d7d9198647cc9d8d67240c',
    'REGISTERED'
),
(
    'MED-IND-2026-000004',
    'Azithromycin 500 mg',
    'Azithromycin Dihydrate IP equivalent to Azithromycin 500 mg',
    '1 tablet daily for 3 to 5 days as prescribed by physician',
    'Antibiotic prescribed for bacterial respiratory infections, sinusitis, throat infections, and skin infections.',
    'Diarrhea, nausea, vomiting, abdominal cramps, altered taste.',
    true,
    'BioShield Pharmaceuticals (Demo)',
    'MED2026D770',
    '2026-04-12',
    '2027-10-11',
    'c2c10b7f83e226a27e7d01beafc48d68962635c249a40590a9ab9ec3798cf026',
    '0x2e08c02c6b4fa7bf5132204c3f58a8a91c9448834d80766b93bbbb7a1dfa0112',
    'REGISTERED'
)
ON CONFLICT (medicine_id) DO NOTHING;

INSERT INTO verification_logs (medicine_id, verification_result, database_hash, blockchain_hash, blockchain_status) VALUES
('MED-IND-2026-000001', 'VERIFIED', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'REGISTERED'),
('MED-IND-2026-000002', 'VERIFIED', '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4', '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4', 'REGISTERED'),
('MED-FAKE-999999', 'UNKNOWN', NULL, NULL, 'NOT_FOUND');
