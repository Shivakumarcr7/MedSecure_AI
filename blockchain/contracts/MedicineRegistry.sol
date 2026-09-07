// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title MedicineRegistry
 * @dev MedSecure AI - Cryptographic Medicine Verification Registry
 * Stores tamper-proof hash records of medicines and batch identifiers.
 */
contract MedicineRegistry {
    struct MedicineRecord {
        string medicineId;
        string batchId;
        bytes32 medicineHash;
        uint256 timestamp;
        address registeredBy;
        bool exists;
    }

    address public owner;
    mapping(address => bool) public authorizedAdmins;
    mapping(string => MedicineRecord) private records;
    string[] private registeredMedicineIds;

    event MedicineRegistered(
        string indexed medicineId,
        string indexed batchId,
        bytes32 medicineHash,
        uint256 timestamp,
        address indexed registeredBy
    );

    event MedicineVerified(
        string indexed medicineId,
        bytes32 providedHash,
        bool isAuthentic,
        uint256 timestamp,
        address verifiedBy
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only contract owner can execute");
        _;
    }

    modifier onlyAdmin() {
        require(msg.sender == owner || authorizedAdmins[msg.sender], "Caller is not authorized admin");
        _;
    }

    constructor() {
        owner = msg.sender;
        authorizedAdmins[msg.sender] = true;
    }

    function setAdmin(address admin, bool status) external onlyOwner {
        authorizedAdmins[admin] = status;
    }

    /**
     * @notice Registers a medicine record with its cryptographic SHA-256 hash
     * @param medicineId Unique public identifier (e.g., MED-IND-2026-000001)
     * @param batchId Manufacturer batch number
     * @param medicineHash SHA-256 canonical hash of the medicine attributes
     */
    function registerMedicine(
        string calldata medicineId,
        string calldata batchId,
        bytes32 medicineHash
    ) external onlyAdmin {
        require(bytes(medicineId).length > 0, "Medicine ID cannot be empty");
        require(medicineHash != bytes32(0), "Hash cannot be zero");
        require(!records[medicineId].exists, "Medicine ID already registered");

        records[medicineId] = MedicineRecord({
            medicineId: medicineId,
            batchId: batchId,
            medicineHash: medicineHash,
            timestamp: block.timestamp,
            registeredBy: msg.sender,
            exists: true
        });

        registeredMedicineIds.push(medicineId);

        emit MedicineRegistered(medicineId, batchId, medicineHash, block.timestamp, msg.sender);
    }

    /**
     * @notice Retrieves the stored on-chain medicine record
     */
    function getMedicineRecord(string calldata medicineId)
        external
        view
        returns (
            string memory id,
            string memory batchId,
            bytes32 medicineHash,
            uint256 timestamp,
            address registeredBy,
            bool exists
        )
    {
        MedicineRecord memory record = records[medicineId];
        return (
            record.medicineId,
            record.batchId,
            record.medicineHash,
            record.timestamp,
            record.registeredBy,
            record.exists
        );
    }

    /**
     * @notice Verifies whether a candidate hash matches the canonical on-chain hash
     */
    function verifyMedicine(string calldata medicineId, bytes32 candidateHash)
        external
        returns (bool isAuthentic, bytes32 storedHash)
    {
        MedicineRecord memory record = records[medicineId];
        require(record.exists, "Medicine record not found on blockchain");

        bool authentic = (record.medicineHash == candidateHash);

        emit MedicineVerified(medicineId, candidateHash, authentic, block.timestamp, msg.sender);

        return (authentic, record.medicineHash);
    }

    /**
     * @notice Returns total number of registered medicines
     */
    function getTotalRegistered() external view returns (uint256) {
        return registeredMedicineIds.length;
    }
}
