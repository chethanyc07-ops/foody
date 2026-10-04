'use client';

import React from 'react';
import { PackagingMaterial } from '../types';

interface LcaComparisonProps {
  currentMaterial?: PackagingMaterial;
}

export const LcaComparison: React.FC<LcaComparisonProps> = ({ currentMaterial }) => {
  const baselineKg = 4.5;
  const currentKg = currentMaterial ? currentMaterial.carbonFootprintKgCo2 : 1.1;

  const items = [
    {
      name: currentMaterial ? currentMaterial.name : 'Selected Bio-Material',
      tag: currentMaterial ? currentMaterial.endOfLife : 'Home Compostable',
      val: currentKg,
      color: 'bg-emerald-600',
      text: 'text-emerald-700'
    },
    {
      name: '100% rPET Closed-Loop Polymer',
      tag: 'Curbside Recyclable',
      val: 1.45,
      color: 'bg-sky-600',
      text: 'text-sky-700'
    },
    {
      name: 'Molded Sugarcane Bagasse Pulp',
      tag: 'Agricultural Upcycled Fiber',
      val: 0.45,
      color: 'bg-teal-600',
      text: 'text-teal-700'
    },
    {
      name: 'Virgin Fossil Polymer Baseline (PET / Alu / PS)',
      tag: 'Conventional Linear Plastic',
      val: baselineKg,
      color: 'bg-red-500',
      text: 'text-red-700'
    }
  ];

  const maxVal = 5.0;
  const savingsPct = Math.round(((baselineKg - currentKg) / baselineKg) * 100);

  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
      <div>
        <h3 className="text-base font-bold text-slate-900">Life Cycle Assessment (LCA) Carbon Benchmark</h3>
        <p className="text-xs text-slate-500">Global Warming Potential in kg CO₂-equivalent per kg packaging material.</p>
      </div>

      <div className="space-y-3">
        {items.map((item, i) => {
          const widthPct = Math.min(100, Math.max(5, (item.val / maxVal) * 100));
          return (
            <div key={i} className="space-y-1">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-800">{item.name} <span className="text-[10px] text-slate-400 font-normal">({item.tag})</span></span>
                <span className={`font-mono font-bold ${item.text}`}>{item.val.toFixed(2)} kg CO₂e</span>
              </div>
              <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full ${item.color} transition-all duration-500`}
                  style={{ width: `${widthPct}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>

      {/* Carbon Reduction Callout */}
      <div className="p-3.5 bg-emerald-50 rounded-xl border border-emerald-200/80 flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold text-xs shrink-0">
          -{savingsPct}%
        </div>
        <div className="text-xs">
          <div className="font-bold text-emerald-900">{savingsPct}% Lower Carbon Footprint</div>
          <div className="text-emerald-700">
            Saves approx {((baselineKg - currentKg) * 1000).toFixed(0)} kg CO₂e per metric ton compared to conventional petroleum plastic.
          </div>
        </div>
      </div>
    </div>
  );
};
