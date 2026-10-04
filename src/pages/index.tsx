'use client';

import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { FoodCommodity, OptimizationGoal, PackagingMaterial, PortfolioMetrics, RecommendationResult, SavedPackagingSpec } from '../types';
import { DEFAULT_COMMODITIES, DEFAULT_MATERIALS, INITIAL_SPECS } from '../data/defaultData';
import { processPackagingWithGemini, computeKineticRecommendations } from '../lib/gemini';
import { Header } from '../components/Header';
import { DashboardView } from '../components/DashboardView';
import { StudioView } from '../components/StudioView';
import { AnalyticsView } from '../components/AnalyticsView';
import { CatalogView } from '../components/CatalogView';
import { AiRationaleModal } from '../components/AiRationaleModal';
import { SpecSheetModal } from '../components/SpecSheetModal';
import { AddCommodityModal } from '../components/AddCommodityModal';

export default function Home() {
  const [currentTab, setCurrentTab] = useState<'dashboard' | 'studio' | 'analytics' | 'catalog'>('dashboard');

  const [commodities, setCommodities] = useState<FoodCommodity[]>(DEFAULT_COMMODITIES);
  const [materials] = useState<PackagingMaterial[]>(DEFAULT_MATERIALS);
  const [specs, setSpecs] = useState<SavedPackagingSpec[]>(INITIAL_SPECS);

  const [selectedCommodity, setSelectedCommodity] = useState<FoodCommodity>(DEFAULT_COMMODITIES[0]);
  const [selectedGoal, setSelectedGoal] = useState<OptimizationGoal>('BALANCED_ECO_PERFORMANCE');
  const [targetShelfLifeDays, setTargetShelfLifeDays] = useState<number>(DEFAULT_COMMODITIES[0].baselineShelfLifeDays * 2);

  const [recommendations, setRecommendations] = useState<RecommendationResult[]>([]);
  const [selectedRecommendation, setSelectedRecommendation] = useState<RecommendationResult | undefined>(undefined);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [geminiStatus, setGeminiStatus] = useState<string>('Gemini 3.5 Flash Model Connected');

  // Modals state
  const [aiModalRec, setAiModalRec] = useState<RecommendationResult | null>(null);
  const [saveModalRec, setSaveModalRec] = useState<RecommendationResult | null>(null);
  const [showAddCommodityModal, setShowAddCommodityModal] = useState<boolean>(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // Run Recommendation function
  const runEvaluation = useCallback(async (
    commodity: FoodCommodity,
    goal: OptimizationGoal,
    targetDays: number
  ) => {
    setIsProcessing(true);
    setGeminiStatus(`Evaluating ${commodity.name} parameters...`);

    try {
      const results = await processPackagingWithGemini(commodity, goal, targetDays, materials);
      setRecommendations(results);
      setSelectedRecommendation(results[0]);
      const isAi = results[0]?.isAiGenerated;
      setGeminiStatus(isAi ? 'Gemini AI Evaluated' : 'Kinetic Model Synced');
    } catch {
      const fallback = computeKineticRecommendations(commodity, goal, targetDays, materials);
      setRecommendations(fallback);
      setSelectedRecommendation(fallback[0]);
      setGeminiStatus('Kinetic Model Active');
    } finally {
      setIsProcessing(false);
    }
  }, [materials]);

  // Initial and parameter change triggers
  useEffect(() => {
    runEvaluation(selectedCommodity, selectedGoal, targetShelfLifeDays);
  }, [selectedCommodity, selectedGoal, targetShelfLifeDays, runEvaluation]);

  const handleCommodityChange = (c: FoodCommodity) => {
    setSelectedCommodity(c);
    setTargetShelfLifeDays(c.baselineShelfLifeDays * 2);
  };

  // Portfolio metrics calculation
  const portfolioMetrics: PortfolioMetrics = useMemo(() => {
    const approved = specs.filter(s => s.status === 'APPROVED' || s.status === 'IN_PRODUCTION');
    const count = Math.max(1, approved.length);
    const avgEco = approved.reduce((acc, s) => acc + s.ecoScore, 0) / count;
    const avgExt = approved.reduce((acc, s) => acc + (s.predictedShelfLifeDays - s.baselineShelfLifeDays), 0) / count;
    const totalCO2 = approved.reduce((acc, s) => acc + (s.carbonSavingsKgPerTon * (s.moqUnits / 1000)) / 1000, 0);
    const circularCount = approved.filter(s => s.endOfLifeMethod.includes('Compost') || s.endOfLifeMethod.includes('Recyclable')).length;
    const circularRate = (circularCount / count) * 100;

    return {
      totalApprovedSpecs: approved.length,
      averageEcoScore: Math.round(avgEco),
      averageShelfLifeExtensionDays: Number(avgExt.toFixed(1)),
      totalCarbonReductionTons: Number(totalCO2.toFixed(1)),
      circularMaterialPercentage: Math.round(circularRate)
    };
  }, [specs]);

  const handleSaveSpec = (newSpec: SavedPackagingSpec) => {
    setSpecs(prev => [newSpec, ...prev]);
    showToast(`Specification "${newSpec.projectName}" approved and saved!`);
  };

  const handleUpdateSpecStatus = (spec: SavedPackagingSpec, newStatus: 'DRAFT' | 'APPROVED' | 'IN_PRODUCTION') => {
    setSpecs(prev => prev.map(s => s.id === spec.id ? { ...s, status: newStatus } : s));
    showToast(`Status updated to ${newStatus.replace('_', ' ')}`);
  };

  const handleDeleteSpec = (id: string) => {
    setSpecs(prev => prev.filter(s => s.id !== id));
    showToast('Specification deleted');
  };

  const handleAddCommodity = (newCommodity: FoodCommodity) => {
    setCommodities(prev => [newCommodity, ...prev]);
    handleCommodityChange(newCommodity);
    setCurrentTab('studio');
    showToast(`Added custom commodity "${newCommodity.name}"`);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Header
        currentTab={currentTab}
        onTabChange={(tab) => setCurrentTab(tab as any)}
        onOpenAddCommodity={() => setShowAddCommodityModal(true)}
        geminiStatus={geminiStatus}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        {currentTab === 'dashboard' && (
          <DashboardView
            metrics={portfolioMetrics}
            commodities={commodities}
            selectedCommodity={selectedCommodity}
            topRecommendation={recommendations[0]}
            specs={specs}
            onSelectCommodity={handleCommodityChange}
            onNavigateToStudio={() => setCurrentTab('studio')}
            onNavigateToAnalytics={() => setCurrentTab('analytics')}
            onUpdateSpecStatus={handleUpdateSpecStatus}
            onDeleteSpec={handleDeleteSpec}
          />
        )}

        {currentTab === 'studio' && (
          <StudioView
            commodities={commodities}
            selectedCommodity={selectedCommodity}
            selectedGoal={selectedGoal}
            targetShelfLifeDays={targetShelfLifeDays}
            recommendations={recommendations}
            selectedRecommendation={selectedRecommendation}
            isProcessing={isProcessing}
            onSelectCommodity={handleCommodityChange}
            onSelectGoal={setSelectedGoal}
            onTargetShelfLifeChange={setTargetShelfLifeDays}
            onRunGeminiProcessing={() => runEvaluation(selectedCommodity, selectedGoal, targetShelfLifeDays)}
            onSelectRecommendation={setSelectedRecommendation}
            onOpenAiModal={setAiModalRec}
            onOpenSaveModal={setSaveModalRec}
          />
        )}

        {currentTab === 'analytics' && (
          <AnalyticsView
            commodity={selectedCommodity}
            recommendations={recommendations}
            materials={materials}
            selectedRecommendation={selectedRecommendation}
          />
        )}

        {currentTab === 'catalog' && (
          <CatalogView
            materials={materials}
            commodities={commodities}
            onSelectCommodityForStudio={(c) => {
              handleCommodityChange(c);
              setCurrentTab('studio');
            }}
            onOpenAddCommodity={() => setShowAddCommodityModal(true)}
          />
        )}
      </main>

      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 bg-slate-900 text-white px-4 py-3 rounded-2xl shadow-xl text-xs font-semibold animate-in slide-in-from-bottom flex items-center gap-2 border border-slate-700">
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Modals */}
      {aiModalRec && (
        <AiRationaleModal
          commodity={selectedCommodity}
          recommendation={aiModalRec}
          onClose={() => setAiModalRec(null)}
        />
      )}

      {saveModalRec && (
        <SpecSheetModal
          commodity={selectedCommodity}
          recommendation={saveModalRec}
          onSave={handleSaveSpec}
          onClose={() => setSaveModalRec(null)}
        />
      )}

      {showAddCommodityModal && (
        <AddCommodityModal
          onAdd={handleAddCommodity}
          onClose={() => setShowAddCommodityModal(false)}
        />
      )}
    </div>
  );
}
