'use client';

import React from 'react';
import { FoodCommodity, PortfolioMetrics, RecommendationResult, SavedPackagingSpec } from '../types';
import { Sparkles, ArrowRight, Clock, ShieldCheck, TreePine, Recycle, Trash2, CheckCircle2, ChevronRight } from 'lucide-react';

interface DashboardViewProps {
  metrics: PortfolioMetrics;
  commodities: FoodCommodity[];
  selectedCommodity: FoodCommodity;
  topRecommendation?: RecommendationResult;
  specs: SavedPackagingSpec[];
  onSelectCommodity: (c: FoodCommodity) => void;
  onNavigateToStudio: () => void;
  onNavigateToAnalytics: () => void;
  onUpdateSpecStatus: (spec: SavedPackagingSpec, newStatus: 'DRAFT' | 'APPROVED' | 'IN_PRODUCTION') => void;
  onDeleteSpec: (id: string) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  metrics,
  commodities,
  selectedCommodity,
  topRecommendation,
  specs,
  onSelectCommodity,
  onNavigateToStudio,
  onNavigateToAnalytics,
  onUpdateSpecStatus,
  onDeleteSpec,
}) => {
  return (
    <div className="space-y-6">
      {/* Hero Supplier Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-emerald-950 via-emerald-900 to-teal-900 text-white p-6 sm:p-8 shadow-xl">
        <div className="absolute right-0 top-0 translate-x-12 -translate-y-12 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-400/20 text-emerald-300 mb-3 border border-emerald-400/30">
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI FOOD PACKAGING KINETIC MODEL</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white mb-2 leading-tight">
            Intelligent Food Packaging & Shelf-Life Optimizer
          </h1>
          <p className="text-sm text-emerald-100/90 leading-relaxed mb-6">
            Predict barrier kinetics (OTR & WVTR), compute shelf-life preservation impact scores, and deploy certified circular packaging substrates to minimize food waste and carbon emissions.
          </p>
          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={onNavigateToStudio}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-emerald-400 hover:bg-emerald-300 text-emerald-950 rounded-xl font-bold text-sm transition shadow-lg shadow-emerald-950/20"
            >
              <span>Launch Recommendation Studio</span>
              <ArrowRight className="w-4 h-4" />
            </button>
            <button
              onClick={onNavigateToAnalytics}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-emerald-900/60 hover:bg-emerald-900 text-white border border-emerald-700/50 rounded-xl font-semibold text-sm transition"
            >
              <span>View Trade-Off Analytics</span>
            </button>
          </div>
        </div>
      </div>

      {/* Portfolio Header KPIs */}
      <div>
        <h2 className="text-base font-bold text-slate-900 mb-3">Portfolio Sustainability & Quality Impact</h2>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
            <div className="flex items-center justify-between text-slate-500 text-xs font-medium mb-1">
              <span>Avg Eco-Score</span>
              <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600">
                <Recycle className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-black text-slate-900">{metrics.averageEcoScore || 92}<span className="text-sm font-normal text-slate-400">/100</span></div>
            <p className="text-[11px] text-slate-500 mt-1">Certified circular index</p>
          </div>

          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
            <div className="flex items-center justify-between text-slate-500 text-xs font-medium mb-1">
              <span>Shelf Extension</span>
              <div className="p-1.5 rounded-lg bg-sky-50 text-sky-600">
                <Clock className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-black text-slate-900">+{metrics.averageShelfLifeExtensionDays || 6.5}<span className="text-sm font-normal text-slate-400"> days</span></div>
            <p className="text-[11px] text-slate-500 mt-1">Average saleable window gain</p>
          </div>

          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
            <div className="flex items-center justify-between text-slate-500 text-xs font-medium mb-1">
              <span>CO₂e Avoided</span>
              <div className="p-1.5 rounded-lg bg-green-50 text-green-600">
                <TreePine className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-black text-slate-900">{metrics.totalCarbonReductionTons.toFixed(1) || '12.4'}<span className="text-sm font-normal text-slate-400"> T</span></div>
            <p className="text-[11px] text-slate-500 mt-1">Fossil emissions mitigated</p>
          </div>

          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
            <div className="flex items-center justify-between text-slate-500 text-xs font-medium mb-1">
              <span>Circularity Rate</span>
              <div className="p-1.5 rounded-lg bg-amber-50 text-amber-600">
                <ShieldCheck className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-black text-slate-900">{metrics.circularMaterialPercentage.toFixed(0) || '94'}<span className="text-sm font-normal text-slate-400">%</span></div>
            <p className="text-[11px] text-slate-500 mt-1">Compostable & recyclables</p>
          </div>
        </div>
      </div>

      {/* Food Commodity Carousel */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-bold text-slate-900">Select Target Food Commodity</h2>
          <span className="text-xs text-slate-500">{commodities.length} commodities available</span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-3">
          {commodities.map((c) => {
            const isSelected = c.id === selectedCommodity.id;
            return (
              <button
                key={c.id}
                onClick={() => onSelectCommodity(c)}
                className={`p-3 rounded-2xl text-left border transition-all ${
                  isSelected
                    ? 'bg-emerald-50/80 border-emerald-500 ring-2 ring-emerald-500/20'
                    : 'bg-white border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="text-2xl mb-1.5">{c.iconEmoji}</div>
                <div className="text-xs font-bold text-slate-900 truncate">{c.name}</div>
                <div className="text-[10px] text-slate-500 truncate">{c.baselineShelfLifeDays}d baseline</div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Live AI Recommendation Spotlight */}
      {topRecommendation && (
        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-4 border-b border-slate-100">
            <div>
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                  {topRecommendation.rankBadge}
                </span>
                <span className="text-xs text-slate-500">for {selectedCommodity.name}</span>
              </div>
              <h3 className="text-lg font-bold text-slate-900 mt-1">{topRecommendation.material.name}</h3>
              <p className="text-xs text-slate-500">{topRecommendation.material.shortCode} • {topRecommendation.material.endOfLife}</p>
            </div>
            <div className="text-right">
              <span className="text-xs text-slate-400">Multi-Objective Score</span>
              <div className="text-2xl font-black text-emerald-700">{topRecommendation.overallScore}<span className="text-sm font-normal text-slate-400">/100</span></div>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 py-4">
            <div className="bg-sky-50/70 p-3 rounded-xl border border-sky-100">
              <span className="text-[11px] font-semibold text-sky-700">Shelf-Life Impact</span>
              <div className="text-lg font-black text-sky-900 mt-0.5">{topRecommendation.shelfLifeImpactScore}/100</div>
              <div className="text-[11px] text-sky-700">{topRecommendation.predictedShelfLifeDays} days (+{topRecommendation.shelfLifeExtensionPercent}%)</div>
            </div>

            <div className="bg-emerald-50/70 p-3 rounded-xl border border-emerald-100">
              <span className="text-[11px] font-semibold text-emerald-700">Eco-Score</span>
              <div className="text-lg font-black text-emerald-900 mt-0.5">{topRecommendation.environmentalImpactScore}/100</div>
              <div className="text-[11px] text-emerald-700">{topRecommendation.degradationRating}</div>
            </div>

            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
              <span className="text-[11px] font-semibold text-slate-600">CO₂ Avoided</span>
              <div className="text-lg font-black text-slate-800 mt-0.5">{topRecommendation.carbonSavingsKgPerTon.toFixed(0)} kg/T</div>
              <div className="text-[11px] text-slate-500">vs Virgin Fossil baseline</div>
            </div>

            <div className="bg-amber-50/70 p-3 rounded-xl border border-amber-100">
              <span className="text-[11px] font-semibold text-amber-700">Cost Benchmark</span>
              <div className="text-lg font-black text-amber-900 mt-0.5">${topRecommendation.material.costPerKgUsd.toFixed(2)}/kg</div>
              <div className="text-[11px] text-amber-700">Raw resin index</div>
            </div>
          </div>

          <div className="flex items-center justify-between pt-2">
            <p className="text-xs text-slate-600 max-w-xl truncate">
              {topRecommendation.barrierAnalysisNotes}
            </p>
            <button
              onClick={onNavigateToStudio}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-emerald-700 hover:bg-emerald-800 text-white rounded-lg text-xs font-bold transition"
            >
              <span>Explore in Studio</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* Active Project Specifications */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-bold text-slate-900">Supplier Project Specifications</h2>
          <button
            onClick={onNavigateToAnalytics}
            className="text-xs font-semibold text-emerald-700 hover:underline"
          >
            Export Spec Sheets
          </button>
        </div>

        <div className="space-y-3">
          {specs.map((s) => (
            <div
              key={s.id}
              className="bg-white rounded-2xl border border-slate-200 p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm hover:border-slate-300 transition"
            >
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-sm font-bold text-slate-900">{s.projectName}</h4>
                  <button
                    onClick={() => {
                      const next = s.status === 'APPROVED' ? 'IN_PRODUCTION' : s.status === 'IN_PRODUCTION' ? 'DRAFT' : 'APPROVED';
                      onUpdateSpecStatus(s, next);
                    }}
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full transition ${
                      s.status === 'IN_PRODUCTION'
                        ? 'bg-emerald-100 text-emerald-800'
                        : s.status === 'APPROVED'
                        ? 'bg-sky-100 text-sky-800'
                        : 'bg-slate-100 text-slate-700'
                    }`}
                  >
                    {s.status.replace('_', ' ')}
                  </button>
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  {s.commodityName} ➔ {s.materialName} ({s.materialCode})
                </p>
              </div>

              <div className="flex items-center gap-6">
                <div className="text-left sm:text-right">
                  <div className="text-xs font-bold text-sky-700">
                    {s.predictedShelfLifeDays} days (+{s.shelfLifeExtensionPercent}%)
                  </div>
                  <div className="text-[11px] text-slate-500">Eco-Score: {s.ecoScore}/100</div>
                </div>

                <div className="text-left sm:text-right">
                  <div className="text-xs font-bold text-slate-900">${s.estimatedUnitCostUsd}/unit</div>
                  <div className="text-[11px] text-slate-500">MOQ: {(s.moqUnits / 1000).toFixed(0)}k</div>
                </div>

                <button
                  onClick={() => onDeleteSpec(s.id)}
                  className="p-1.5 text-slate-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition"
                  title="Delete specification"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
