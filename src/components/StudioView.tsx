'use client';

import React from 'react';
import { FoodCommodity, OptimizationGoal, PackagingMaterial, RecommendationResult } from '../types';
import { Sparkles, Check, Clock, Recycle, Shield, DollarSign, SlidersHorizontal, Loader2, FileCheck2, Cpu } from 'lucide-react';

interface StudioViewProps {
  commodities: FoodCommodity[];
  selectedCommodity: FoodCommodity;
  selectedGoal: OptimizationGoal;
  targetShelfLifeDays: number;
  recommendations: RecommendationResult[];
  selectedRecommendation?: RecommendationResult;
  isProcessing: boolean;
  onSelectCommodity: (c: FoodCommodity) => void;
  onSelectGoal: (g: OptimizationGoal) => void;
  onTargetShelfLifeChange: (days: number) => void;
  onRunGeminiProcessing: () => void;
  onSelectRecommendation: (r: RecommendationResult) => void;
  onOpenAiModal: (r: RecommendationResult) => void;
  onOpenSaveModal: (r: RecommendationResult) => void;
}

export const StudioView: React.FC<StudioViewProps> = ({
  commodities,
  selectedCommodity,
  selectedGoal,
  targetShelfLifeDays,
  recommendations,
  selectedRecommendation,
  isProcessing,
  onSelectCommodity,
  onSelectGoal,
  onTargetShelfLifeChange,
  onRunGeminiProcessing,
  onSelectRecommendation,
  onOpenAiModal,
  onOpenSaveModal,
}) => {
  const goals: { id: OptimizationGoal; label: string; desc: string }[] = [
    {
      id: 'BALANCED_ECO_PERFORMANCE',
      label: 'Balanced Eco & Freshness',
      desc: 'Optimal Pareto balance between maximum shelf life and minimal carbon footprint.'
    },
    {
      id: 'MAX_SHELF_LIFE',
      label: 'Maximum Shelf Life',
      desc: 'Prioritizes superior oxygen and water vapor barrier kinetics to prevent spoilage.'
    },
    {
      id: 'MIN_ENVIRONMENTAL_IMPACT',
      label: 'Net-Zero & Compostable',
      desc: 'Prioritizes home compostability, circularity index, and minimal fossil footprint.'
    },
    {
      id: 'COST_CONSCIOUS_GREEN',
      label: 'Cost-Conscious Sustainable',
      desc: 'Balances sustainable materials within competitive commercial unit cost limits.'
    }
  ];

  return (
    <div className="space-y-6">
      {/* Top Banner: Gemini AI Processor Status */}
      <div className="bg-emerald-950 text-white p-4 rounded-2xl border border-emerald-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-md">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-sm text-white">Gemini API Food Packaging Processor</span>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-300">
                gemini-3.5-flash
              </span>
            </div>
            <p className="text-xs text-emerald-200/80">
              Processing {selectedCommodity.name} respiration rates, barrier targets, and target shelf life.
            </p>
          </div>
        </div>

        <button
          onClick={onRunGeminiProcessing}
          disabled={isProcessing}
          className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-emerald-950 rounded-xl font-bold text-xs transition disabled:opacity-50 shadow-sm"
        >
          {isProcessing ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Simulating Kinetics...</span>
            </>
          ) : (
            <>
              <Sparkles className="w-4 h-4" />
              <span>Re-Evaluate with Gemini AI</span>
            </>
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Parameter Configuration */}
        <div className="lg:col-span-5 space-y-6">
          {/* 1. Food Commodity Profile */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
              <span className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-800 text-xs flex items-center justify-center font-bold">1</span>
              <span>Target Food Commodity Profile</span>
            </h3>

            <div className="flex flex-wrap gap-2 mb-4">
              {commodities.map((c) => {
                const isSel = c.id === selectedCommodity.id;
                return (
                  <button
                    key={c.id}
                    onClick={() => onSelectCommodity(c)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold border transition ${
                      isSel
                        ? 'bg-emerald-700 text-white border-emerald-700 shadow-sm'
                        : 'bg-slate-50 text-slate-700 border-slate-200 hover:border-slate-300'
                    }`}
                  >
                    <span>{c.iconEmoji}</span> <span className="ml-1">{c.name}</span>
                  </button>
                );
              })}
            </div>

            <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200/80 text-xs space-y-1.5 text-slate-700">
              <div className="font-semibold text-slate-900">Hazard: {selectedCommodity.primarySpoilageFactor}</div>
              <div>Respiration: <span className="font-medium text-slate-900">{selectedCommodity.respirationRate}</span></div>
              <div>Target OTR: <span className="font-mono text-emerald-800 font-semibold">{selectedCommodity.targetOtrMin} - {selectedCommodity.targetOtrMax} cc/m²/d</span></div>
              <div>Target WVTR: <span className="font-mono text-emerald-800 font-semibold">{selectedCommodity.targetWvtrMin} - {selectedCommodity.targetWvtrMax} g/m²/d</span></div>
              <div>Storage: <span className="font-medium text-slate-900">{selectedCommodity.idealStorageTemp}</span> (Baseline: {selectedCommodity.baselineShelfLifeDays} days)</div>
            </div>
          </div>

          {/* 2. Goal & Shelf Life Slider */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <span className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-800 text-xs flex items-center justify-center font-bold">2</span>
              <span>Optimization Goal & Freshness Window</span>
            </h3>

            <div className="space-y-2">
              {goals.map((g) => {
                const isSelected = selectedGoal === g.id;
                return (
                  <button
                    key={g.id}
                    onClick={() => onSelectGoal(g.id)}
                    className={`w-full text-left p-3 rounded-xl border text-xs transition flex items-start gap-2.5 ${
                      isSelected
                        ? 'bg-emerald-50/70 border-emerald-600 ring-1 ring-emerald-600/30'
                        : 'bg-white border-slate-200 hover:border-slate-300'
                    }`}
                  >
                    <div className={`mt-0.5 w-4 h-4 rounded-full border flex items-center justify-center ${
                      isSelected ? 'border-emerald-600 bg-emerald-600 text-white' : 'border-slate-300'
                    }`}>
                      {isSelected && <Check className="w-2.5 h-2.5" />}
                    </div>
                    <div>
                      <div className="font-bold text-slate-900">{g.label}</div>
                      <div className="text-[11px] text-slate-500 mt-0.5">{g.desc}</div>
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Target Shelf Life Slider */}
            <div className="pt-2">
              <div className="flex items-center justify-between text-xs font-bold text-slate-900 mb-1">
                <span>Target Desired Shelf Life:</span>
                <span className="text-emerald-700 font-extrabold text-sm">{targetShelfLifeDays} days</span>
              </div>
              <input
                type="range"
                min={selectedCommodity.baselineShelfLifeDays}
                max={Math.max(15, selectedCommodity.baselineShelfLifeDays * 4)}
                value={targetShelfLifeDays}
                onChange={(e) => onTargetShelfLifeChange(Number(e.target.value))}
                className="w-full accent-emerald-600 cursor-pointer"
              />
              <div className="flex justify-between text-[10px] text-slate-400 mt-1">
                <span>Baseline ({selectedCommodity.baselineShelfLifeDays}d)</span>
                <span>Max Extended ({Math.max(15, selectedCommodity.baselineShelfLifeDays * 4)}d)</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Ranked Recommendations with Shelf-Life Impact Scores */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900">
              Ranked Packaging Material Recommendations
            </h3>
            <span className="text-xs text-slate-500 font-medium">
              {recommendations.length} materials evaluated
            </span>
          </div>

          <div className="space-y-3">
            {recommendations.map((rec) => {
              const isSelected = selectedRecommendation?.material.id === rec.material.id;
              return (
                <div
                  key={rec.material.id}
                  onClick={() => onSelectRecommendation(rec)}
                  className={`bg-white rounded-2xl border p-4 sm:p-5 transition shadow-sm cursor-pointer ${
                    isSelected
                      ? 'border-emerald-600 ring-2 ring-emerald-600/20'
                      : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-100">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                          {rec.rankBadge}
                        </span>
                        {rec.isAiGenerated && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-100 text-purple-800 inline-flex items-center gap-1">
                            <Sparkles className="w-2.5 h-2.5" />
                            Gemini AI
                          </span>
                        )}
                        <span className="text-xs text-slate-400">{rec.material.shortCode}</span>
                      </div>
                      <h4 className="text-sm sm:text-base font-bold text-slate-900 mt-1">{rec.material.name}</h4>
                      <p className="text-xs text-slate-500">{rec.material.category} • {rec.material.endOfLife}</p>
                    </div>

                    <div className="text-left sm:text-right">
                      <span className="text-[11px] text-slate-400">Score</span>
                      <div className="text-xl sm:text-2xl font-black text-emerald-700">{rec.overallScore}<span className="text-xs font-normal text-slate-400">/100</span></div>
                    </div>
                  </div>

                  {/* Shelf-Life Impact & Key Metrics */}
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 py-3">
                    <div className="bg-sky-50/70 p-2.5 rounded-xl border border-sky-100">
                      <div className="text-[10px] font-semibold text-sky-700">Shelf-Life Impact</div>
                      <div className="text-base font-black text-sky-950 mt-0.5">{rec.shelfLifeImpactScore}/100</div>
                      <div className="text-[10px] text-sky-700">{rec.predictedShelfLifeDays}d (+{rec.shelfLifeExtensionPercent}%)</div>
                    </div>

                    <div className="bg-emerald-50/70 p-2.5 rounded-xl border border-emerald-100">
                      <div className="text-[10px] font-semibold text-emerald-700">Eco-Score</div>
                      <div className="text-base font-black text-emerald-950 mt-0.5">{rec.environmentalImpactScore}/100</div>
                      <div className="text-[10px] text-emerald-700">Circularity: {rec.material.circularityScore}</div>
                    </div>

                    <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-200">
                      <div className="text-[10px] font-semibold text-slate-600">CO₂e Saved</div>
                      <div className="text-base font-black text-slate-900 mt-0.5">{rec.carbonSavingsKgPerTon.toFixed(0)} kg</div>
                      <div className="text-[10px] text-slate-500">per metric ton</div>
                    </div>

                    <div className="bg-amber-50/70 p-2.5 rounded-xl border border-amber-100">
                      <div className="text-[10px] font-semibold text-amber-700">Unit Cost Index</div>
                      <div className="text-base font-black text-amber-950 mt-0.5">${rec.material.costPerKgUsd.toFixed(2)}</div>
                      <div className="text-[10px] text-amber-700">per kg raw resin</div>
                    </div>
                  </div>

                  <p className="text-xs text-slate-600 mb-3">{rec.barrierAnalysisNotes}</p>

                  {/* Actions */}
                  <div className="flex items-center gap-2 pt-1">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onOpenAiModal(rec);
                      }}
                      className="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold transition"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-purple-600" />
                      <span>AI Rationale</span>
                    </button>

                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onOpenSaveModal(rec);
                      }}
                      className="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl text-xs font-bold transition shadow-sm"
                    >
                      <FileCheck2 className="w-3.5 h-3.5" />
                      <span>Save Spec</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
