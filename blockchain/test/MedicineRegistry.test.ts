import { expect } from "chai";
import { ethers } from "hardhat";

describe("MedicineRegistry", function () {
  let registry: any;
  let owner: any;
  let unauthorized: any;

  beforeEach(async function () {
    [owner, unauthorized] = await ethers.getSigners();
    const MedicineRegistry = await ethers.getContractFactory("MedicineRegistry");
    registry = await MedicineRegistry.deploy();
    await registry.waitForDeployment();
  });

  it("should register a medicine with SHA-256 hash and emit event", async function () {
    const medicineId = "MED-IND-2026-000001";
    const batchId = "MED2026A001";
    const testHash = ethers.keccak256(ethers.toUtf8Bytes("canonical-paracetamol-payload"));

    await expect(registry.registerMedicine(medicineId, batchId, testHash))
      .to.emit(registry, "MedicineRegistered")
      .withArgs(medicineId, batchId, testHash, (val: any) => val > 0, owner.address);

    const record = await registry.getMedicineRecord(medicineId);
    expect(record.medicineId).to.equal(medicineId);
    expect(record.batchId).to.equal(batchId);
    expect(record.medicineHash).to.equal(testHash);
    expect(record.exists).to.be.true;
  });

  it("should reject duplicate registration of same medicine ID", async function () {
    const medicineId = "MED-IND-2026-000001";
    const batchId = "MED2026A001";
    const testHash = ethers.keccak256(ethers.toUtf8Bytes("data-1"));

    await registry.registerMedicine(medicineId, batchId, testHash);

    await expect(
      registry.registerMedicine(medicineId, batchId, testHash)
    ).to.be.revertedWith("Medicine ID already registered");
  });

  it("should verify authentic medicine and detect tampered hash", async function () {
    const medicineId = "MED-IND-2026-000002";
    const batchId = "BATCH-XYZ";
    const authenticHash = ethers.keccak256(ethers.toUtf8Bytes("authentic"));
    const tamperedHash = ethers.keccak256(ethers.toUtf8Bytes("tampered"));

    await registry.registerMedicine(medicineId, batchId, authenticHash);

    const [isAuthentic] = await registry.verifyMedicine.staticCall(medicineId, authenticHash);
    expect(isAuthentic).to.be.true;

    const [isTampered] = await registry.verifyMedicine.staticCall(medicineId, tamperedHash);
    expect(isTampered).to.be.false;
  });
});
