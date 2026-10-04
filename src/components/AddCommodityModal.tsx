'use client';

import React, { useState } from 'react';
import { FoodCommodity } from '../types';
import { X, PlusCircle } from 'lucide-react';

interface AddCommodityModalProps {
  onAdd: (commodity: FoodCommodity) => void;
  onClose: () => void;
}

export const AddCommodityModal: React.FC<AddCommodityModalProps> = ({ onAdd, onClose }) => {
  const [name, setName] = useState('');
  const [category, setCategory] = useState('Fresh Produce');
  const [baselineShelfLifeDays, setBaselineShelfLifeDays] = useState(5);
  const [idealStorageTemp, setIdealStorageTemp] = useState('2°C - 4°C (Chilled)');
  const [respirationRate, setRespirationRate] = useState('Medium (15-25 mg CO2/kg·h)');
  const [moistureSensitivity, setMoistureSensitivity] = useState(4);
  const [oxygenSensitivity, setOxygenSensitivity] = useState(4);
  const [primarySpoilageFactor, setPrimarySpoilageFactor] = useState('Aerobic bacterial bloom & moisture loss');
  const [targetOtrMin, setTargetOtrMin] = useState(100);
  const [targetOtrMax, setTargetOtrMax] = useState(1500);
  const [targetWvtrMin, setTargetWvtrMin] = useState(5);
  const [targetWvtrMax, setTargetWvtrMax] = useState(25);
  const [iconEmoji, setIconEmoji] = useState('🥑');

  const categories = ['Fresh Produce', 'Meat & Poultry', 'Seafood', 'Bakery', 'Dairy', 'Dry Staples'];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    const newCommodity: FoodCommodity = {
      id: 'comm-' + Date.now(),
      name: name.trim(),
      category,
      baselineShelfLifeDays: Number(baselineShelfLifeDays) || 5,
      idealStorageTemp,
      respirationRate,
      moistureSensitivity: Number(moistureSensitivity),
      oxygenSensitivity: Number(oxygenSensitivity),
      lightSensitivity: 3,
      ethyleneSensitivity: 2,
      primarySpoilageFactor,
      targetOtrMin: Number(targetOtrMin) || 100,
      targetOtrMax: Number(targetOtrMax) || 1000,
      targetWvtrMin: Number(targetWvtrMin) || 5,
      targetWvtrMax: Number(targetWvtrMax) || 30,
      iconEmoji: iconEmoji || '📦',
      description: 'Custom supplier food commodity profile.'
    };

    onAdd(newCommodity);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-100 space-y-4 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
              <PlusCircle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">Add Custom Food Commodity</h3>
              <p className="text-xs text-slate-500">Configure spoilage sensitivity and gas permeability targets</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          <div className="flex gap-2">
            <div className="w-16">
              <label className="block text-slate-700 font-semibold mb-1">Icon</label>
              <input
                type="text"
                value={iconEmoji}
                onChange={(e) => setIconEmoji(e.target.value.slice(0, 2))}
                className="w-full text-center px-2 py-2 rounded-xl border border-slate-200 text-lg"
              />
            </div>
            <div className="flex-1">
              <label className="block text-slate-700 font-semibold mb-1">Commodity Name</label>
              <input
                type="text"
                placeholder="e.g. Hass Avocado, Fresh Salmon"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-700 font-semibold mb-1">Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 bg-white"
              >
                {categories.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-slate-700 font-semibold mb-1">Baseline Shelf Life (Days)</label>
              <input
                type="number"
                min="1"
                max="365"
                value={baselineShelfLifeDays}
                onChange={(e) => setBaselineShelfLifeDays(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Storage Condition</label>
            <input
              type="text"
              value={idealStorageTemp}
              onChange={(e) => setIdealStorageTemp(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200"
            />
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Primary Spoilage Hazard</label>
            <input
              type="text"
              value={primarySpoilageFactor}
              onChange={(e) => setPrimarySpoilageFactor(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-700 font-semibold mb-1">Oxygen Sensitivity (1-5)</label>
              <input
                type="range"
                min="1"
                max="5"
                value={oxygenSensitivity}
                onChange={(e) => setOxygenSensitivity(Number(e.target.value))}
                className="w-full accent-emerald-600"
              />
              <span className="text-[10px] text-slate-500">Rating: {oxygenSensitivity}/5</span>
            </div>

            <div>
              <label className="block text-slate-700 font-semibold mb-1">Moisture Sensitivity (1-5)</label>
              <input
                type="range"
                min="1"
                max="5"
                value={moistureSensitivity}
                onChange={(e) => setMoistureSensitivity(Number(e.target.value))}
                className="w-full accent-emerald-600"
              />
              <span className="text-[10px] text-slate-500">Rating: {moistureSensitivity}/5</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-700 font-semibold mb-1">Target OTR Range (cc/m²)</label>
              <div className="flex items-center gap-1">
                <input
                  type="number"
                  value={targetOtrMin}
                  onChange={(e) => setTargetOtrMin(Number(e.target.value))}
                  className="w-full px-2 py-1.5 rounded-lg border border-slate-200 font-mono text-center"
                />
                <span>-</span>
                <input
                  type="number"
                  value={targetOtrMax}
                  onChange={(e) => setTargetOtrMax(Number(e.target.value))}
                  className="w-full px-2 py-1.5 rounded-lg border border-slate-200 font-mono text-center"
                />
              </div>
            </div>

            <div>
              <label className="block text-slate-700 font-semibold mb-1">Target WVTR Range (g/m²)</label>
              <div className="flex items-center gap-1">
                <input
                  type="number"
                  value={targetWvtrMin}
                  onChange={(e) => setTargetWvtrMin(Number(e.target.value))}
                  className="w-full px-2 py-1.5 rounded-lg border border-slate-200 font-mono text-center"
                />
                <span>-</span>
                <input
                  type="number"
                  value={targetWvtrMax}
                  onChange={(e) => setTargetWvtrMax(Number(e.target.value))}
                  className="w-full px-2 py-1.5 rounded-lg border border-slate-200 font-mono text-center"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-3">
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
              Add Commodity Profile
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
