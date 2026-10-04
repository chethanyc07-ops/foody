'use client';

import React, { useState } from 'react';
import { FoodCommodity, PackagingMaterial } from '../types';
import { Search, Plus, Shield, Recycle, Thermometer } from 'lucide-react';

interface CatalogViewProps {
  materials: PackagingMaterial[];
  commodities: FoodCommodity[];
  onSelectCommodityForStudio: (c: FoodCommodity) => void;
  onOpenAddCommodity: () => void;
}

export const CatalogView: React.FC<CatalogViewProps> = ({
  materials,
  commodities,
  onSelectCommodityForStudio,
  onOpenAddCommodity,
}) => {
  const [activeTab, setActiveTab] = useState<'materials' | 'commodities'>('materials');
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');

  const materialCategories = ['All', 'Bio-based Compostable', 'Recycled Polymer', 'Fiber/Cellulose', 'High-Barrier Mono-Material', 'Active Bio-Coating'];
  const commodityCategories = ['All', 'Fresh Produce', 'Meat & Poultry', 'Seafood', 'Bakery', 'Dairy', 'Dry Staples'];

  const filteredMaterials = materials.filter(m => {
    const matchesCat = selectedCategory === 'All' || m.category === selectedCategory;
    const matchesSearch = !search || m.name.toLowerCase().includes(search.toLowerCase()) || m.shortCode.toLowerCase().includes(search.toLowerCase());
    return matchesCat && matchesSearch;
  });

  const filteredCommodities = commodities.filter(c => {
    const matchesCat = selectedCategory === 'All' || c.category === selectedCategory;
    const matchesSearch = !search || c.name.toLowerCase().includes(search.toLowerCase());
    return matchesCat && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Tab Switcher & Search */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex p-1 bg-slate-100 rounded-xl max-w-sm">
            <button
              onClick={() => {
                setActiveTab('materials');
                setSelectedCategory('All');
              }}
              className={`flex-1 py-1.5 px-3 rounded-lg text-xs font-bold transition ${
                activeTab === 'materials' ? 'bg-white text-emerald-800 shadow-sm' : 'text-slate-600'
              }`}
            >
              Packaging Materials ({materials.length})
            </button>
            <button
              onClick={() => {
                setActiveTab('commodities');
                setSelectedCategory('All');
              }}
              className={`flex-1 py-1.5 px-3 rounded-lg text-xs font-bold transition ${
                activeTab === 'commodities' ? 'bg-white text-emerald-800 shadow-sm' : 'text-slate-600'
              }`}
            >
              Food Commodities ({commodities.length})
            </button>
          </div>

          <div className="flex items-center gap-2">
            <div className="relative flex-1 sm:w-64">
              <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
              <input
                type="text"
                placeholder={activeTab === 'materials' ? 'Search materials or shortcodes...' : 'Search commodities...'}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>

            {activeTab === 'commodities' && (
              <button
                onClick={onOpenAddCommodity}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl text-xs font-bold transition shadow-sm whitespace-nowrap"
              >
                <Plus className="w-4 h-4" />
                <span>Add Custom</span>
              </button>
            )}
          </div>
        </div>

        {/* Category Pills */}
        <div className="flex flex-wrap gap-1.5">
          {(activeTab === 'materials' ? materialCategories : commodityCategories).map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1 rounded-lg text-xs font-medium transition ${
                selectedCategory === cat
                  ? 'bg-emerald-700 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Materials Grid */}
      {activeTab === 'materials' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredMaterials.map((m) => (
            <div key={m.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-3">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-xs bg-slate-100 px-2 py-0.5 rounded text-slate-800">{m.shortCode}</span>
                    <span className="text-xs text-slate-500">{m.category}</span>
                  </div>
                  <h4 className="text-base font-bold text-slate-900 mt-1">{m.name}</h4>
                </div>
                <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-100">
                  Circularity {m.circularityScore}/100
                </span>
              </div>

              <p className="text-xs text-slate-600 leading-relaxed">{m.description}</p>

              <div className="grid grid-cols-4 gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100 text-center text-xs">
                <div>
                  <div className="text-[10px] text-slate-400">OTR</div>
                  <div className="font-mono font-bold text-slate-800">{m.otr}</div>
                </div>
                <div>
                  <div className="text-[10px] text-slate-400">WVTR</div>
                  <div className="font-mono font-bold text-slate-800">{m.wvtr}</div>
                </div>
                <div>
                  <div className="text-[10px] text-slate-400">CO₂ Footprint</div>
                  <div className="font-mono font-bold text-emerald-700">{m.carbonFootprintKgCo2} kg</div>
                </div>
                <div>
                  <div className="text-[10px] text-slate-400">Degradation</div>
                  <div className="font-mono font-bold text-slate-800">~{m.degradationDays}d</div>
                </div>
              </div>

              <div className="flex items-center justify-between text-xs text-slate-500 pt-1">
                <span>End of Life: <strong className="text-slate-800">{m.endOfLife}</strong></span>
                <span>Raw Resin: <strong className="text-amber-700">${m.costPerKgUsd.toFixed(2)}/kg</strong></span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* Commodities Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredCommodities.map((c) => (
            <div key={c.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="text-3xl">{c.iconEmoji}</div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">{c.name}</h4>
                    <span className="text-xs text-slate-500">{c.category} • {c.baselineShelfLifeDays}d baseline</span>
                  </div>
                </div>

                <button
                  onClick={() => onSelectCommodityForStudio(c)}
                  className="px-2.5 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 rounded-lg text-xs font-bold transition border border-emerald-200"
                >
                  Evaluate
                </button>
              </div>

              <p className="text-xs text-slate-600">{c.primarySpoilageFactor}</p>

              <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100 text-xs space-y-1 text-slate-700">
                <div className="flex justify-between">
                  <span className="text-slate-500">Target OTR:</span>
                  <span className="font-mono font-semibold">{c.targetOtrMin} - {c.targetOtrMax} cc</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Target WVTR:</span>
                  <span className="font-mono font-semibold">{c.targetWvtrMin} - {c.targetWvtrMax} g</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Storage:</span>
                  <span className="font-semibold text-emerald-700">{c.idealStorageTemp}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
