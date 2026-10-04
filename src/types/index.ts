export interface FoodCommodity {
  id: string;
  name: string;
  category: string;
  baselineShelfLifeDays: number;
  idealStorageTemp: string;
  respirationRate: string;
  moistureSensitivity: number; // 1-5
  oxygenSensitivity: number;   // 1-5
  lightSensitivity: number;    // 1-5
  ethyleneSensitivity: number; // 1-5
  primarySpoilageFactor: string;
  targetOtrMin: number;
  targetOtrMax: number;
  targetWvtrMin: number;
  targetWvtrMax: number;
  iconEmoji: string;
  description: string;
}

export interface PackagingMaterial {
  id: string;
  name: string;
  shortCode: string;
  category: string;
  description: string;
  otr: number; // cc/m²/day
  wvtr: number; // g/m²/day
  co2Permeability: number;
  tensileStrengthMpa: number;
  carbonFootprintKgCo2: number;
  endOfLife: string;
  degradationDays: number;
  circularityScore: number; // 0-100
  costPerKgUsd: number;
  minTempCelsius: number;
  maxTempCelsius: number;
  certifications: string;
  bestSuitedFor: string;
}

export type OptimizationGoal =
  | 'BALANCED_ECO_PERFORMANCE'
  | 'MAX_SHELF_LIFE'
  | 'MIN_ENVIRONMENTAL_IMPACT'
  | 'COST_CONSCIOUS_GREEN';

export interface RecommendationResult {
  material: PackagingMaterial;
  predictedShelfLifeDays: number;
  shelfLifeExtensionPercent: number;
  shelfLifeImpactScore: number; // 0-100
  environmentalImpactScore: number; // 0-100
  carbonSavingsKgPerTon: number;
  degradationRating: string;
  barrierMatchScore: number;
  economicScore: number;
  overallScore: number;
  rankBadge: string;
  barrierAnalysisNotes: string;
  aiInsight?: string;
  suggestedActiveAdditives?: string;
  isAiGenerated?: boolean;
}

export interface SavedPackagingSpec {
  id: string;
  projectName: string;
  commodityName: string;
  commodityCategory: string;
  materialName: string;
  materialCode: string;
  baselineShelfLifeDays: number;
  predictedShelfLifeDays: number;
  shelfLifeExtensionPercent: number;
  ecoScore: number;
  carbonSavingsKgPerTon: number;
  endOfLifeMethod: string;
  targetOtrWvtrSummary: string;
  estimatedUnitCostUsd: number;
  moqUnits: number;
  status: 'DRAFT' | 'APPROVED' | 'IN_PRODUCTION';
  supplierNotes: string;
  timestamp: number;
}

export interface PortfolioMetrics {
  totalApprovedSpecs: number;
  averageEcoScore: number;
  averageShelfLifeExtensionDays: number;
  totalCarbonReductionTons: number;
  circularMaterialPercentage: number;
}
