import { FoodCommodity, OptimizationGoal, PackagingMaterial, RecommendationResult } from '../types';

export async function processPackagingWithGemini(
  commodity: FoodCommodity,
  goal: OptimizationGoal,
  targetShelfLifeDays: number,
  candidateMaterials: PackagingMaterial[]
): Promise<RecommendationResult[]> {
  const apiKey = process.env.NEXT_PUBLIC_GEMINI_API_KEY || '';

  if (apiKey && apiKey !== 'MY_GEMINI_API_KEY') {
    try {
      const prompt = `
You are a senior Food Packaging Materials Scientist and Barrier Kinetic Simulation AI.
Analyze the following food commodity parameters:
- Commodity: ${commodity.name} (${commodity.category})
- Baseline Shelf Life: ${commodity.baselineShelfLifeDays} days
- Storage Temp: ${commodity.idealStorageTemp}
- Respiration Rate: ${commodity.respirationRate}
- Moisture Sensitivity (1-5): ${commodity.moistureSensitivity}
- Oxygen Sensitivity (1-5): ${commodity.oxygenSensitivity}
- Primary Spoilage Factor: ${commodity.primarySpoilageFactor}
- Target OTR: ${commodity.targetOtrMin} - ${commodity.targetOtrMax} cc/m²/day
- Target WVTR: ${commodity.targetWvtrMin} - ${commodity.targetWvtrMax} g/m²/day
- Optimization Goal: ${goal}
- Target Desired Shelf Life: ${targetShelfLifeDays} days

Candidate Packaging Materials:
${candidateMaterials.map(m => `- shortCode: "${m.shortCode}", name: "${m.name}", category: "${m.category}", OTR: ${m.otr}, WVTR: ${m.wvtr}, carbon: ${m.carbonFootprintKgCo2}, endOfLife: "${m.endOfLife}", circularity: ${m.circularityScore}`).join('\n')}

Return a valid JSON array of recommendations ordered from highest overallScore to lowest:
[
  {
    "shortCode": "string",
    "predictedShelfLifeDays": 8,
    "shelfLifeImpactScore": 88,
    "environmentalImpactScore": 92,
    "barrierMatchScore": 85,
    "overallScore": 91,
    "rankBadge": "⭐ Top AI Pick",
    "barrierAnalysisNotes": "string",
    "aiInsight": "string",
    "suggestedActiveAdditives": "string"
  }
]
`;

      const response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: {
              responseMimeType: 'application/json',
              temperature: 0.2
            }
          })
        }
      );

      if (response.ok) {
        const data = await response.json();
        const text = data?.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) {
          const parsed = JSON.parse(text);
          if (Array.isArray(parsed) && parsed.length > 0) {
            const materialMap = new Map(candidateMaterials.map(m => [m.shortCode, m]));
            const results: RecommendationResult[] = [];

            for (const item of parsed) {
              const material = materialMap.get(item.shortCode) || candidateMaterials.find(m => m.name.includes(item.shortCode));
              if (material) {
                const predictedDays = Number(item.predictedShelfLifeDays) || commodity.baselineShelfLifeDays * 2;
                const extension = ((predictedDays - commodity.baselineShelfLifeDays) / commodity.baselineShelfLifeDays) * 100;
                results.push({
                  material,
                  predictedShelfLifeDays: predictedDays,
                  shelfLifeExtensionPercent: Math.round(extension),
                  shelfLifeImpactScore: Number(item.shelfLifeImpactScore) || 85,
                  environmentalImpactScore: Number(item.environmentalImpactScore) || material.circularityScore,
                  carbonSavingsKgPerTon: Math.max(0, (4.5 - material.carbonFootprintKgCo2) * 1000),
                  degradationRating: material.degradationDays <= 60 ? `Rapid Compost (~${material.degradationDays}d)` : `Circular Closed-Loop`,
                  barrierMatchScore: Number(item.barrierMatchScore) || 80,
                  economicScore: Math.round(Math.max(20, (1 - (material.costPerKgUsd - 1.2) / 4) * 100)),
                  overallScore: Number(item.overallScore) || 85,
                  rankBadge: item.rankBadge || 'High Match',
                  barrierAnalysisNotes: item.barrierAnalysisNotes || `OTR: ${material.otr} | WVTR: ${material.wvtr}`,
                  aiInsight: item.aiInsight,
                  suggestedActiveAdditives: item.suggestedActiveAdditives,
                  isAiGenerated: true
                });
              }
            }
            if (results.length > 0) return results;
          }
        }
      }
    } catch (err) {
      console.warn('Gemini API call failed, falling back to kinetic model:', err);
    }
  }

  // Algorithmic Barrier Kinetic Simulation Fallback
  return computeKineticRecommendations(commodity, goal, targetShelfLifeDays, candidateMaterials);
}

export function computeKineticRecommendations(
  commodity: FoodCommodity,
  goal: OptimizationGoal,
  targetShelfLifeDays: number,
  candidateMaterials: PackagingMaterial[]
): RecommendationResult[] {
  const results = candidateMaterials.map(material => {
    // 1. Barrier Match Score
    const otrMatch = calcBarrierScore(material.otr, commodity.targetOtrMin, commodity.targetOtrMax);
    const wvtrMatch = calcBarrierScore(material.wvtr, commodity.targetWvtrMin, commodity.targetWvtrMax);

    const o2W = commodity.oxygenSensitivity / 5.0;
    const h2oW = commodity.moistureSensitivity / 5.0;
    const barrierScore = Math.round(Math.max(15, Math.min(100, (otrMatch * o2W + wvtrMatch * h2oW) / (o2W + h2oW))));

    // 2. Shelf Life Extension Kinetics
    const efficiency = barrierScore / 100.0;
    const maxMultiplier =
      commodity.category === 'Fresh Produce' ? 2.2 :
      commodity.category === 'Meat & Poultry' ? 2.8 :
      commodity.category === 'Seafood' ? 2.5 : 2.0;

    const multiplier = 1.0 + (maxMultiplier - 1.0) * efficiency;
    const predictedDays = Math.round(commodity.baselineShelfLifeDays * multiplier);
    const extensionPercent = Math.round(((predictedDays - commodity.baselineShelfLifeDays) / commodity.baselineShelfLifeDays) * 100);

    const shelfLifeImpactScore = Math.round(Math.min(99, Math.max(30, (predictedDays / (commodity.baselineShelfLifeDays * 2.5)) * 100)));

    // 3. Environmental Impact Score
    const carbonScore = Math.max(0, (1.0 - material.carbonFootprintKgCo2 / 4.8) * 100);
    const eolScore = material.endOfLife.includes('Home') ? 100 : material.endOfLife.includes('Industrial') ? 85 : 80;
    const ecoScore = Math.round(0.4 * carbonScore + 0.35 * eolScore + 0.25 * material.circularityScore);
    const carbonSavingsKgPerTon = Math.max(0, (4.5 - material.carbonFootprintKgCo2) * 1000);

    // 4. Economic Score
    const economicScore = Math.round(Math.max(20, Math.min(100, (1.0 - Math.max(0, material.costPerKgUsd - 1.2) / 4.0) * 100)));

    // 5. Overall Weighted Multi-Objective Score
    let overall = 0;
    if (goal === 'BALANCED_ECO_PERFORMANCE') {
      overall = Math.round(0.35 * barrierScore + 0.35 * ecoScore + 0.15 * shelfLifeImpactScore + 0.15 * economicScore);
    } else if (goal === 'MAX_SHELF_LIFE') {
      overall = Math.round(0.45 * shelfLifeImpactScore + 0.35 * barrierScore + 0.15 * ecoScore + 0.05 * economicScore);
    } else if (goal === 'MIN_ENVIRONMENTAL_IMPACT') {
      overall = Math.round(0.55 * ecoScore + 0.20 * barrierScore + 0.15 * shelfLifeImpactScore + 0.10 * economicScore);
    } else {
      overall = Math.round(0.40 * economicScore + 0.30 * ecoScore + 0.20 * barrierScore + 0.10 * shelfLifeImpactScore);
    }

    return {
      material,
      predictedShelfLifeDays: predictedDays,
      shelfLifeExtensionPercent: extensionPercent,
      shelfLifeImpactScore,
      environmentalImpactScore: ecoScore,
      carbonSavingsKgPerTon,
      degradationRating: material.degradationDays <= 60 ? `Rapid Compost (~${material.degradationDays}d)` : `Circular Recyclable`,
      barrierMatchScore: barrierScore,
      economicScore,
      overallScore: Math.min(99, Math.max(20, overall)),
      rankBadge: '',
      barrierAnalysisNotes: `OTR: ${material.otr} cc/m² (Target ${commodity.targetOtrMin}-${commodity.targetOtrMax}), WVTR: ${material.wvtr} g/m² (Target ${commodity.targetWvtrMin}-${commodity.targetWvtrMax})`,
      aiInsight: `${material.name} delivers ${material.otr} cc/m² OTR and ${material.wvtr} g/m² WVTR, controlling ${commodity.primarySpoilageFactor} to preserve product freshness up to ${predictedDays} days while maintaining a low carbon footprint of ${material.carbonFootprintKgCo2} kg CO2e/kg.`,
      suggestedActiveAdditives: commodity.category === 'Fresh Produce' ? 'Potassium permanganate ethylene absorption sachet or micro-perforations.' : 'Inert nitrogen gas flush (<0.5% O2) & food-grade drip absorption layer.',
      isAiGenerated: false
    };
  });

  const sorted = results.sort((a, b) => b.overallScore - a.overallScore);
  const topOverall = sorted[0];
  const topEco = [...sorted].sort((a, b) => b.environmentalImpactScore - a.environmentalImpactScore)[0];
  const topFresh = [...sorted].sort((a, b) => b.predictedShelfLifeDays - a.predictedShelfLifeDays)[0];

  return sorted.map((item, idx) => {
    let badge = 'Alternative Option';
    if (item === topOverall) badge = '⭐ Top Recommendation';
    else if (item === topEco) badge = '🌿 Eco-Champion';
    else if (item === topFresh) badge = '⏱️ Freshness Master';
    else if (idx < 3) badge = `High Match (#${idx + 1})`;
    return { ...item, rankBadge: badge };
  });
}

function calcBarrierScore(actual: number, minT: number, maxT: number): number {
  if (actual >= minT && actual <= maxT) return 100;
  const ratio = actual < minT ? (minT - actual) / Math.max(1, minT) : (actual - maxT) / Math.max(1, maxT);
  const penalty = Math.min(80, Math.log(1 + ratio) * 35);
  return Math.max(20, 100 - penalty);
}
