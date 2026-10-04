'use client';

import React, { useState } from 'react';
import { FoodCommodity, RecommendationResult, SavedPackagingSpec } from '../types';
import { X, FileCheck2 } from 'lucide-react';

interface SpecSheetModalProps {
  commodity: FoodCommodity;
  recommendation: RecommendationResult;
  onSave: (spec: SavedPackagingSpec) => void;
  onClose: () => void;
}

export const SpecSheetModal: React.FC<SpecSheetModalProps> = ({
  commodity,
  recommendation,
  onSave,
  onClose,
}) => {
  const [projectName, setProjectName] = useState(`${commodity.name} - ${recommendation.material.shortCode} Pack`);
  const [unitCost, setUnitCost] = useState((recommendation.material.costPerKgUsd * 0.035).toFixed(3));
  const [moq, setMoq] = useState(25000);
  const [notes, setNotes] = useState(`Commercial rollout spec using certified ${recommendation.material.endOfLife} bio-substrate.`);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const newSpec: SavedPackagingSpec = {
      id: 'spec-' + Date.now(),
      projectName,
      commodityName: commodity.name,
      commodityCategory: commodity.category,
      materialName: recommendation.material.name,
      materialCode: recommendation.material.shortCode,
      baselineShelfLifeDays: commodity.baselineShelfLifeDays,
      predictedShelfLifeDays: recommendation.predictedShelfLifeDays,
      shelfLifeExtensionPercent: recommendation.shelfLifeExtensionPercent,
      ecoScore: recommendation.environmentalImpactScore,
      carbonSavingsKgPerTon: recommendation.carbonSavingsKgPerTon,
      endOfLifeMethod: recommendation.material.endOfLife,
      targetOtrWvtrSummary: `OTR: ${recommendation.material.otr} | WVTR: ${recommendation.material.wvtr}`,
      estimatedUnitCostUsd: Number(unitCost) || 0.08,
      moqUnits: Number(moq) || 10000,
      status: 'APPROVED',
      supplierNotes: notes,
      timestamp: Date.now()
    };
    onSave(newSpec);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
              <FileCheck2 className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">Save Supplier Packaging Spec</h3>
              <p className="text-xs text-slate-500">Approve prototype design for supplier portfolio</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSave} className="space-y-3.5 text-xs">
          <div>
            <label className="block text-slate-700 font-semibold mb-1">Project / SKU Name</label>
            <input
              type="text"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-700 font-semibold mb-1">Est. Unit Cost ($)</label>
              <input
                type="number"
                step="0.001"
                value={unitCost}
                onChange={(e) => setUnitCost(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-700 font-semibold mb-1">Target MOQ (Units)</label>
              <input
                type="number"
                value={moq}
                onChange={(e) => setMoq(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Supplier Notes & Guidelines</label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-100 text-emerald-800">
            <span className="font-bold">Shelf Life Impact: {recommendation.shelfLifeImpactScore}/100</span> ({recommendation.predictedShelfLifeDays} days, +{recommendation.shelfLifeExtensionPercent}%)
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-semibold transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl font-bold transition shadow-sm"
            >
              Approve & Save Specification
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
