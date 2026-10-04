'use client';

import React, { useState } from 'react';
import { FoodCommodity, PackagingMaterial, RecommendationResult } from '../types';
import { TradeOffMatrix } from './TradeOffMatrix';
import { LcaComparison } from './LcaComparison';
import { Copy, Check, Download, Share2 } from 'lucide-react';

interface AnalyticsViewProps {
  commodity: FoodCommodity;
  recommendations: RecommendationResult[];
  materials: PackagingMaterial[];
  selectedRecommendation?: RecommendationResult;
}

export const AnalyticsView: React.FC<AnalyticsViewProps> = ({
  commodity,
  recommendations,
  materials,
  selectedRecommendation,
}) => {
  const [copied, setCopied] = useState(false);
  const activeRec = selectedRecommendation || recommendations[0];

  const specSheetText = activeRec ? `
============================================================
           ECOPACK AI TECHNICAL SPECIFICATION SHEET
============================================================
Date Generated    : ${new Date().toISOString().split('T')[0]}
Project ID        : SPEC-${commodity.category.slice(0, 3).toUpperCase()}-${activeRec.material.shortCode}
Status            : APPROVED FOR COMMERCIAL PROTOTYPE

[ 1. COMMODITY PARAMETERS ]
Commodity         : ${commodity.name} (${commodity.category})
Baseline Life     : ${commodity.baselineShelfLifeDays} days @ ${commodity.idealStorageTemp}
Primary Spoilage  : ${commodity.primarySpoilageFactor}
Target Barrier    : OTR ${commodity.targetOtrMin}-${commodity.targetOtrMax} cc | WVTR ${commodity.targetWvtrMin}-${commodity.targetWvtrMax} g

[ 2. SELECTED PACKAGING MATERIAL ]
Resin / Substrate : ${activeRec.material.name}
Grade Shortcode   : ${activeRec.material.shortCode}
Classification    : ${activeRec.material.category}
Permeability      : OTR: ${activeRec.material.otr} cc/m²/d/atm | WVTR: ${activeRec.material.wvtr} g/m²/d
Mechanical Specs  : Tensile Strength ${activeRec.material.tensileStrengthMpa} MPa
Temperature Range : ${activeRec.material.minTempCelsius}°C to ${activeRec.material.maxTempCelsius}°C

[ 3. PERFORMANCE & ENVIRONMENTAL IMPACT ]
Predicted Shelf   : ${activeRec.predictedShelfLifeDays} days (+${activeRec.shelfLifeExtensionPercent}% Freshness Gain)
Shelf-Life Impact : ${activeRec.shelfLifeImpactScore}/100
Eco-Score Rating  : ${activeRec.environmentalImpactScore}/100 (Optimal Circular Tier)
Embodied Carbon   : ${activeRec.material.carbonFootprintKgCo2} kg CO2e / kg material
Carbon Avoided    : ${activeRec.carbonSavingsKgPerTon.toFixed(0)} kg CO2e / metric ton vs Virgin Fossil
End-of-Life Route : ${activeRec.material.endOfLife} (~${activeRec.material.degradationDays} days)
Circularity Score : ${activeRec.material.circularityScore}/100

[ 4. REGULATORY & FOOD CONTACT COMPLIANCE ]
Certifications    : ${activeRec.material.certifications}
Additives Required: ${activeRec.suggestedActiveAdditives || 'Standard inert gas flush'}
============================================================
`.trim() : '';

  const handleCopy = () => {
    navigator.clipboard.writeText(specSheetText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6">
      {/* 1. Trade-Off Quadrant Matrix */}
      <TradeOffMatrix
        recommendations={recommendations}
        onSelect={(rec) => {}}
      />

      {/* 2. Life Cycle Assessment (LCA) Comparison */}
      <LcaComparison currentMaterial={activeRec?.material} />

      {/* 3. Permeability & Properties Catalog Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
        <h3 className="text-base font-bold text-slate-900 mb-1">Barrier Permeability & Life Cycle Catalog</h3>
        <p className="text-xs text-slate-500 mb-4">Complete physical, gas barrier, and environmental degradation parameters.</p>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold">
                <th className="py-2.5 px-3">Grade Code</th>
                <th className="py-2.5 px-3">Material Name</th>
                <th className="py-2.5 px-3">Category</th>
                <th className="py-2.5 px-3">OTR (cc/m²)</th>
                <th className="py-2.5 px-3">WVTR (g/m²)</th>
                <th className="py-2.5 px-3">CO₂ Footprint</th>
                <th className="py-2.5 px-3">Degradation</th>
                <th className="py-2.5 px-3">Circularity</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {materials.map((m) => (
                <tr key={m.id} className="hover:bg-slate-50/50">
                  <td className="py-2 px-3 font-mono font-bold text-slate-900">{m.shortCode}</td>
                  <td className="py-2 px-3 font-medium text-slate-800">{m.name}</td>
                  <td className="py-2 px-3 text-slate-500">{m.category}</td>
                  <td className="py-2 px-3 font-mono">{m.otr}</td>
                  <td className="py-2 px-3 font-mono">{m.wvtr}</td>
                  <td className="py-2 px-3 font-bold text-emerald-700">{m.carbonFootprintKgCo2} kg/kg</td>
                  <td className="py-2 px-3 text-slate-600">~{m.degradationDays}d</td>
                  <td className="py-2 px-3 font-semibold text-slate-900">{m.circularityScore}/100</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 4. Exportable Supplier Technical Specification Report */}
      {activeRec && (
        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h3 className="text-base font-bold text-slate-900">Technical Spec Sheet & Bill of Materials</h3>
              <p className="text-xs text-slate-500">Export-ready compliance report for suppliers and converters.</p>
            </div>

            <button
              onClick={handleCopy}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold transition"
            >
              {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              <span>{copied ? 'Copied!' : 'Copy Spec Sheet'}</span>
            </button>
          </div>

          <div className="bg-slate-950 text-slate-200 font-mono text-xs p-4 rounded-xl overflow-x-auto whitespace-pre leading-relaxed border border-slate-800 shadow-inner">
            {specSheetText}
          </div>
        </div>
      )}
    </div>
  );
};
