package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.model.RecommendationResult
import com.example.data.repository.PackagingRepository
import com.example.ui.screens.AddCommodityDialog
import com.example.ui.screens.AiRationaleDialog
import com.example.ui.screens.AnalyticalReportingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MaterialsCatalogScreen
import com.example.ui.screens.RecommendationStudioScreen
import com.example.ui.screens.SaveSpecDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PackagingViewModel
import com.example.ui.viewmodel.PackagingViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PackagingViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repo = PackagingRepository(
            commodityDao = db.commodityDao(),
            materialDao = db.materialDao(),
            specDao = db.packagingSpecDao()
        )
        PackagingViewModelFactory(repo)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val commodities by viewModel.allCommodities.collectAsStateWithLifecycle()
                val materials by viewModel.allMaterials.collectAsStateWithLifecycle()
                val specs by viewModel.savedSpecs.collectAsStateWithLifecycle()
                val portfolioMetrics by viewModel.portfolioMetrics.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                var currentTab by remember { mutableIntStateOf(0) }
                var showAddCommodityDialog by remember { mutableStateOf(false) }
                var showSaveSpecDialog by remember { mutableStateOf(false) }
                var showAiRationaleDialog by remember { mutableStateOf(false) }
                var activeRecommendationForAction by remember { mutableStateOf<RecommendationResult?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.eventFlow.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                // Handle system back button to return to dashboard if on sub-screens
                BackHandler(enabled = currentTab != 0) {
                    currentTab = 0
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.testTag("app_header_title")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF047857)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🌿", fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = "EcoPack AI",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Packaging Recommendation & Analytics",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            },
                            actions = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFD1FAE5))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF059669))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Gemini AI",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showAddCommodityDialog = true },
                                    modifier = Modifier.testTag("add_commodity_icon_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Custom Food Commodity",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Supplier Dashboard") },
                                label = { Text("Dashboard", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Recommendation Studio") },
                                label = { Text("Studio", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytical Reports") },
                                label = { Text("Analytics", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Science, contentDescription = "Materials Catalog") },
                                label = { Text("Materials", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == 0) {
                            FloatingActionButton(
                                onClick = { currentTab = 1 },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                modifier = Modifier.testTag("launch_studio_fab")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run AI Recs", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(
                                metrics = portfolioMetrics,
                                commodities = commodities,
                                selectedCommodity = uiState.selectedCommodity,
                                topRecommendation = uiState.recommendations.firstOrNull(),
                                recentSpecs = specs,
                                onSelectCommodity = { comm ->
                                    viewModel.selectCommodity(comm)
                                },
                                onNavigateToStudio = { currentTab = 1 },
                                onNavigateToReports = { currentTab = 2 },
                                onOpenAddCommodity = { showAddCommodityDialog = true },
                                onUpdateSpecStatus = { spec, newStatus ->
                                    viewModel.updateSpecStatus(spec, newStatus)
                                },
                                onDeleteSpec = { spec ->
                                    viewModel.deleteSpec(spec)
                                }
                            )

                            1 -> RecommendationStudioScreen(
                                commodities = commodities,
                                selectedCommodity = uiState.selectedCommodity,
                                selectedGoal = uiState.selectedGoal,
                                targetShelfLifeDays = uiState.targetShelfLifeDays,
                                recommendations = uiState.recommendations,
                                selectedRecommendation = uiState.selectedRecommendation,
                                isRunningRecommendation = uiState.isRunningRecommendation,
                                isGeminiProcessing = uiState.isGeminiProcessing,
                                geminiStatusMessage = uiState.geminiStatusMessage,
                                onSelectCommodity = { comm ->
                                    viewModel.selectCommodity(comm)
                                },
                                onSelectGoal = { goal ->
                                    viewModel.selectGoal(goal)
                                },
                                onTargetShelfLifeChanged = { days ->
                                    viewModel.setTargetShelfLifeDays(days)
                                },
                                onProcessWithGemini = {
                                    viewModel.processPackagingParametersWithGemini()
                                },
                                onSelectRecommendation = { rec ->
                                    viewModel.selectRecommendationForDetail(rec)
                                },
                                onOpenAiRationale = { rec ->
                                    activeRecommendationForAction = rec
                                    viewModel.fetchAiInsight(rec)
                                    showAiRationaleDialog = true
                                },
                                onOpenSaveSpec = { rec ->
                                    activeRecommendationForAction = rec
                                    showSaveSpecDialog = true
                                }
                            )

                            2 -> AnalyticalReportingScreen(
                                commodity = uiState.selectedCommodity,
                                recommendations = uiState.recommendations,
                                materials = materials,
                                selectedRecommendation = uiState.selectedRecommendation,
                                onSelectRecommendation = { rec ->
                                    viewModel.selectRecommendationForDetail(rec)
                                },
                                onShowMessage = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )

                            3 -> MaterialsCatalogScreen(
                                materials = materials,
                                commodities = commodities,
                                onSelectCommodityForStudio = { comm ->
                                    viewModel.selectCommodity(comm)
                                    currentTab = 1
                                },
                                onOpenAddCommodity = { showAddCommodityDialog = true }
                            )
                        }
                    }
                }

                // Dialog: Add Custom Commodity
                if (showAddCommodityDialog) {
                    AddCommodityDialog(
                        onDismiss = { showAddCommodityDialog = false },
                        onConfirm = { name, cat, life, temp, resp, moist, oxy, spoil, otrMin, otrMax, wvtrMin, wvtrMax, emoji ->
                            viewModel.addNewCommodity(
                                name = name,
                                category = cat,
                                baselineShelfLife = life,
                                idealTemp = temp,
                                respiration = resp,
                                moistureSens = moist,
                                oxygenSens = oxy,
                                spoilageFactor = spoil,
                                otrMin = otrMin,
                                otrMax = otrMax,
                                wvtrMin = wvtrMin,
                                wvtrMax = wvtrMax,
                                emoji = emoji
                            )
                            showAddCommodityDialog = false
                            currentTab = 1
                        }
                    )
                }

                // Dialog: Save Spec
                if (showSaveSpecDialog && activeRecommendationForAction != null && uiState.selectedCommodity != null) {
                    SaveSpecDialog(
                        commodity = uiState.selectedCommodity!!,
                        result = activeRecommendationForAction!!,
                        onDismiss = { showSaveSpecDialog = false },
                        onSave = { projectName, unitCost, moq, notes, status ->
                            viewModel.savePackagingSpec(
                                projectName = projectName,
                                rec = activeRecommendationForAction!!,
                                unitCost = unitCost,
                                moq = moq,
                                supplierNotes = notes,
                                status = status
                            )
                            showSaveSpecDialog = false
                        }
                    )
                }

                // Dialog: AI Rationale
                if (showAiRationaleDialog && activeRecommendationForAction != null && uiState.selectedCommodity != null) {
                    AiRationaleDialog(
                        commodity = uiState.selectedCommodity!!,
                        result = activeRecommendationForAction!!,
                        aiText = uiState.aiInsightText,
                        isLoading = uiState.isGeneratingAiInsight,
                        onRegenerate = {
                            viewModel.fetchAiInsight(activeRecommendationForAction!!)
                        },
                        onDismiss = { showAiRationaleDialog = false }
                    )
                }
            }
        }
    }
}
