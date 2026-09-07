import { ethers } from "hardhat";

async function main() {
  console.log("Deploying MedicineRegistry contract to target network...");

  const MedicineRegistry = await ethers.getContractFactory("MedicineRegistry");
  const registry = await MedicineRegistry.deploy();
  await registry.waitForDeployment();

  const address = await registry.getAddress();
  console.log(`MedicineRegistry deployed successfully to: ${address}`);
  console.log(`Update CONTRACT_ADDRESS=${address} in your .env configuration.`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
