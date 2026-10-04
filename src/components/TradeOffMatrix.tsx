'use client';

import React, { useState } from 'react';
import { RecommendationResult } from '../types';

interface TradeOffMatrixProps {
  recommendations: RecommendationResult[];
  onSelect: (rec: RecommendationResult) => void;
}

export const TradeOffMatrix: React.FC<TradeOffMatrixProps> = ({
  recommendations,
  onSelect,
}) => {
  const [selectedIdx, setSelectedIdx] = useState<number>(0);
  const activeItem = recommendations[selectedIdx] || recommendations[0];

  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1">
        <div>
          <h3 className="text-base font-bold text-slate-900">Trade-Off Quadrant Matrix: Freshness vs Eco-Score</h3>
          <p className="text-xs text-slate-500">Multi-objective Pareto frontier analysis for material selection.</p>
        </div>
      </div>

      {/* SVG Scatter Plot */}
      <div className="relative w-full h-72 bg-slate-50 rounded-2xl border border-slate-200 p-4 overflow-hidden">
        {/* Quadrant Background Shading */}
        <div className="absolute inset-0 grid grid-cols-2 grid-rows-2 pointer-events-none">
          <div className="bg-sky-50/40 border-r border-b border-dashed border-slate-300 flex items-start justify-start p-2">
            <span className="text-[10px] font-bold text-sky-800">⚡ High Barrier / Moderate Eco</span>
          </div>
          <div className="bg-emerald-50/50 border-b border-dashed border-slate-300 flex items-start justify-end p-2">
            <span className="text-[10px] font-bold text-emerald-800">🌿 Eco-Champions (Optimal)</span>
          </div>
          <div className="border-r border-dashed border-slate-300 flex items-end justify-start p-2">
            <span className="text-[10px] font-bold text-slate-400">⚠️ Sub-Optimal</span>
          </div>
          <div className="bg-amber-50/30 flex items-end justify-end p-2">
            <span className="text-[10px] font-bold text-amber-800">🌱 Ultra-Green / Standard Life</span>
          </div>
        </div>

        {/* Dynamic SVG Plot Points */}
        <svg className="w-full h-full relative z-10">
          {recommendations.map((rec, idx) => {
            const isSelected = selectedIdx === idx;
            // X: Eco Score (0 to 100) -> 5% to 95%
            const cx = `${5 + (rec.environmentalImpactScore / 100) * 90}%`;
            // Y: Shelf life extension (0 to 200%) -> 90% down to 10%
            const maxExt = 200;
            const normY = Math.min(maxExt, Math.max(0, rec.shelfLifeExtensionPercent)) / maxExt;
            const cy = `${90 - normY * 80}%`;

            const color =
              rec.environmentalImpactScore >= 85 && rec.shelfLifeExtensionPercent >= 70
                ? '#047857'
                : rec.shelfLifeExtensionPercent >= 70
                ? '#0284c7'
                : rec.environmentalImpactScore >= 85
                ? '#d97706'
                : '#64748b';

            return (
              <g
                key={rec.material.id}
                className="cursor-pointer transition-all"
                onClick={() => {
                  setSelectedIdx(idx);
                  onSelect(rec);
                }}
              >
                {isSelected && (
                  <circle
                    cx={cx}
                    cy={cy}
                    r="14"
                    fill={color}
                    fillOpacity="0.2"
                    className="animate-pulse"
                  />
                )}
                <circle
                  cx={cx}
                  cy={cy}
                  r={isSelected ? '8' : '6'}
                  fill={color}
                  stroke="#ffffff"
                  strokeWidth="2"
                />
                <text
                  x={cx}
                  y={cy}
                  dy="-10"
                  textAnchor="middle"
                  className="text-[10px] font-bold fill-slate-700 pointer-events-none select-none"
                >
                  {rec.material.shortCode}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Selected Item Banner */}
      {activeItem && (
        <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div>
            <span className="font-bold text-slate-900">{activeItem.material.name} ({activeItem.material.shortCode})</span>
            <div className="text-slate-500 mt-0.5">
              Freshness: <strong className="text-sky-700">+{activeItem.shelfLifeExtensionPercent}% ({activeItem.predictedShelfLifeDays} days)</strong> • Shelf-Life Score: <strong className="text-sky-700">{activeItem.shelfLifeImpactScore}/100</strong> • Eco-Score: <strong className="text-emerald-700">{activeItem.environmentalImpactScore}/100</strong>
            </div>
          </div>
          <span className="px-2.5 py-1 rounded-lg bg-emerald-100 text-emerald-800 font-bold whitespace-nowrap">
            {activeItem.rankBadge}
          </span>
        </div>
      )}
    </div>
  );
};
