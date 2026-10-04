'use client';

import React from 'react';
import { FoodCommodity, RecommendationResult } from '../types';
import { Sparkles, X, Lightbulb } from 'lucide-react';

interface AiRationaleModalProps {
  commodity: FoodCommodity;
  recommendation: RecommendationResult;
  onClose: () => void;
}

export const AiRationaleModal: React.FC<AiRationaleModalProps> = ({
  commodity,
  recommendation,
  onClose,
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-100 space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-purple-100 text-purple-700 rounded-xl">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">AI Materials Science Rationale</h3>
              <p className="text-xs text-slate-500">Gemini 3.5 Flash Kinetic Simulation Analysis</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Pairing summary chip */}
        <div className="bg-emerald-50/70 p-3.5 rounded-xl border border-emerald-100 text-xs">
          <div className="font-bold text-emerald-900">
            {commodity.iconEmoji} {commodity.name} ➔ {recommendation.material.name} ({recommendation.material.shortCode})
          </div>
          <div className="text-emerald-700 mt-1">
            Shelf-Life Score: <strong className="font-semibold">{recommendation.shelfLifeImpactScore}/100</strong> • Predicted: <strong className="font-semibold">{recommendation.predictedShelfLifeDays} days (+{recommendation.shelfLifeExtensionPercent}%)</strong>
          </div>
        </div>

        {/* AI Rationale Text */}
        <div className="text-xs text-slate-700 leading-relaxed space-y-2 bg-slate-50 p-4 rounded-xl border border-slate-200">
          <p>{recommendation.aiInsight || recommendation.barrierAnalysisNotes}</p>
        </div>

        {/* Active Additives Callout */}
        {recommendation.suggestedActiveAdditives && (
          <div className="bg-amber-50 p-3.5 rounded-xl border border-amber-200 flex items-start gap-2.5">
            <Lightbulb className="w-4 h-4 text-amber-600 mt-0.5 shrink-0" />
            <div className="text-xs">
              <div className="font-bold text-amber-900">Active Packaging Additive Tip</div>
              <div className="text-amber-800 mt-0.5">{recommendation.suggestedActiveAdditives}</div>
            </div>
          </div>
        )}

        <div className="pt-2 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold transition"
          >
            Close Analysis
          </button>
        </div>
      </div>
    </div>
  );
};
