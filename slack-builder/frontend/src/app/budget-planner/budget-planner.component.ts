import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';

// Updated interfaces to match the new API DTOs
interface BudgetCategorySummary {
  id: number;
  budgetId: number;
  name: string;
  plannedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  color: string;
  createdAt: string;
  updatedAt: string;
  spentPercentage: number;
  isOverBudget: boolean;
  isNearLimit: boolean;
  status: string;
}

interface BudgetSummary {
  id: number;
  name: string;
  userId: number;
  startDate: string;
  endDate: string;
  totalPlanned: number;
  totalSpent: number;
  totalRemaining: number;
  isActive: boolean;
  isRepeatable: boolean;
  repeatInterval: string | null;
  repeatCount: number;
  createdAt: string;
  updatedAt: string;
  spentPercentage: number;
  isOverBudget: boolean;
  isNearLimit: boolean;
  status: string;
  categories: BudgetCategorySummary[];
  totalCategories: number;
  activeCategories: number;
  overBudgetCategories: number;
  nearLimitCategories: number;
  // Daily spending calculations
  dailySpendingAllowance?: number;
  daysRemaining?: number;
  daysElapsed?: number;
  formattedDailySpendingAllowance?: string;
  dailySpendingDescription?: string;
}

interface DashboardSummary {
  totalBudgets: number;
  activeBudgets: number;
  totalPlanned: number;
  totalSpent: number;
  totalRemaining: number;
  overallSpentPercentage: number;
  overBudgetCount: number;
  nearLimitCount: number;
  completedCount: number;
}

interface BudgetDashboardResponse {
  budgets: BudgetSummary[];
  summary: DashboardSummary;
  overBudgetBudgets: BudgetSummary[];
  nearLimitBudgets: BudgetSummary[];
  completedBudgets: BudgetSummary[];
}

@Component({
  selector: 'app-budget-planner',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './budget-planner.component.html',
  styleUrls: ['./budget-planner.component.css']
})
export class BudgetPlannerComponent implements OnInit {
  budgets: BudgetSummary[] = [];
  dashboardData: BudgetDashboardResponse | null = null;
  currentBudget: BudgetSummary | null = null;
  isCreatingBudget = false;
  isEditingBudget = false;
  editingCategory: BudgetCategorySummary | null = null;
  isLoading = false;
  errorMessage = '';

  // Expose Math for template usage
  Math = Math;

  // Form data for new budget
  newBudget: Partial<BudgetSummary> = {
    name: '',
    startDate: '',
    endDate: '',
    categories: [] as BudgetCategorySummary[],
    isRepeatable: false,
    repeatInterval: null,
    repeatCount: 1
  };

  // Available category colors
  categoryColors = [
    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
    '#FF9F40', '#C9CBCF', '#FF6384', '#4BC0C0', '#FF6384'
  ];

  // Default budget categories - will be loaded from backend
  defaultCategories: string[] = [];

  // Available transaction types - will be loaded from backend
  availableTransactionTypes: string[] = [];

  // Repeat intervals for recurring budgets
  repeatIntervals = [
    { value: 'weekly', label: 'Weekly', description: 'Every week' },
    { value: 'monthly', label: 'Monthly', description: 'Every month' },
    { value: 'yearly', label: 'Yearly', description: 'Every year' }
  ];

  // API base URL
  private readonly API_BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadDashboard();
    this.loadCategories();
    this.loadTransactionTypes();
  }

  // Load comprehensive dashboard data from new API
  loadDashboard() {
    this.isLoading = true;
    this.errorMessage = '';

    console.log('Loading dashboard data from new API...');
    this.http.get<BudgetDashboardResponse>(`${this.API_BASE_URL}/budgets/dashboard?userId=1`)
      .subscribe({
        next: (dashboard) => {
          console.log('Dashboard data received:', dashboard);
          this.dashboardData = dashboard;
          this.budgets = dashboard.budgets;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading dashboard:', error);
          this.errorMessage = 'Failed to load budget data. Please try again.';
          this.isLoading = false;

          // Fallback to old API method
          this.loadBudgetsLegacy();
        }
      });
  }

  // Legacy method as fallback
  private loadBudgetsLegacy() {
    console.log('Falling back to legacy API...');
    this.http.get<BudgetSummary[]>(`${this.API_BASE_URL}/budgets/?userId=1`)
      .subscribe({
        next: (budgets) => {
          console.log('Legacy budgets response:', budgets);
          this.budgets = budgets;
        },
        error: (error) => {
          console.error('Legacy API also failed:', error);
          this.loadBudgetsFromLocalStorage();
        }
      });
  }

  // Load categories for each budget (legacy method)
  private loadBudgetsWithCategories(budgets: BudgetSummary[]) {
    this.budgets = [];
    let loadedCount = 0;
    const totalBudgets = budgets.length;

    budgets.forEach((budget, index) => {
      // Load categories for this budget
      this.http.get<BudgetCategorySummary[]>(`${this.API_BASE_URL}/budgets/${budget.id}/categories`)
        .subscribe({
          next: (categories) => {
            // Create a complete budget object with categories
            const completeBudget: BudgetSummary = {
              ...budget,
              categories: categories,
              totalCategories: categories.length,
              activeCategories: categories.filter(cat => cat.spentAmount < cat.plannedAmount).length,
              overBudgetCategories: categories.filter(cat => cat.isOverBudget).length,
              nearLimitCategories: categories.filter(cat => cat.isNearLimit).length
            };

            this.budgets[index] = completeBudget;
            loadedCount++;

            // If all budgets are loaded, trigger change detection
            if (loadedCount === totalBudgets) {
              console.log('Complete budgets loaded from legacy API:', this.budgets);
              this.budgets = [...this.budgets];
            }
          },
          error: (error) => {
            console.error(`Error loading categories for budget ${budget.id}:`, error);
            // Create budget without categories
            const incompleteBudget: BudgetSummary = {
              ...budget,
              categories: [],
              totalCategories: 0,
              activeCategories: 0,
              overBudgetCategories: 0,
              nearLimitCategories: 0
            };
            this.budgets[index] = incompleteBudget;
            loadedCount++;

            if (loadedCount === totalBudgets) {
              console.log('Complete budgets loaded from legacy API (with errors):', this.budgets);
              this.budgets = [...this.budgets];
            }
          }
        });
    });
  }

  // Fallback to localStorage if backend fails
  private loadBudgetsFromLocalStorage() {
    const savedBudgets = localStorage.getItem('budgets');
    if (savedBudgets) {
      this.budgets = JSON.parse(savedBudgets);
      console.log('Budgets loaded from localStorage (fallback):', this.budgets);
    }
  }

  // Load categories from backend
  loadCategories() {
    // This will be updated to use the new API structure
    this.defaultCategories = [
      'Food & Dining', 'Shopping', 'Transportation', 'Entertainment',
      'Utilities', 'Healthcare', 'Other'
    ];
  }

  // Load transaction types from backend
  loadTransactionTypes() {
    // This will be updated to use the new API structure
    this.availableTransactionTypes = [
      'purchase', 'withdrawal', 'transfer', 'deposit'
    ];
  }

  // Get status color for budget
  getStatusColor(status: string): string {
    switch (status) {
      case 'over-budget': return '#ff4444';
      case 'near-limit': return '#ffaa00';
      case 'completed': return '#00aa00';
      case 'on-track': return '#00aa00';
      default: return '#666666';
    }
  }

  // Get status icon for budget
  getStatusIcon(status: string): string {
    switch (status) {
      case 'over-budget': return '⚠️';
      case 'near-limit': return '⚡';
      case 'completed': return '✅';
      case 'on-track': return '📊';
      default: return '❓';
    }
  }

  // Get progress bar color based on percentage
  getProgressBarColor(percentage: number): string {
    if (percentage >= 100) return '#ff4444';
    if (percentage >= 80) return '#ffaa00';
    return '#00aa00';
  }

  // Debug method to check budget data
  debugBudgets() {
    console.log('Current budgets array:', this.budgets);
    console.log('Dashboard data:', this.dashboardData);
    console.log('Budgets length:', this.budgets.length);
    this.budgets.forEach((budget, index) => {
      console.log(`Budget ${index}:`, {
        id: budget.id,
        name: budget.name,
        totalPlanned: budget.totalPlanned,
        totalSpent: budget.totalSpent,
        totalRemaining: budget.totalRemaining,
        status: budget.status,
        categoriesCount: budget.categories?.length || 0
      });
    });
  }

  // Start creating a new budget
  startCreateBudget() {
    this.isCreatingBudget = true;
    this.isEditingBudget = false;
    this.currentBudget = null;
    this.newBudget = {
      name: '',
      startDate: this.getCurrentDate(),
      endDate: this.getDateInFuture(30), // Default to 30 days
      categories: []
    };
  }

  // Add a default category to the new budget
  addDefaultCategory(categoryName: string) {
    if (!this.newBudget.categories) {
      this.newBudget.categories = [];
    }

    const newCategory: BudgetCategorySummary = {
      id: Date.now() + Math.random(),
      budgetId: 0, // Will be set when budget is created
      name: categoryName,
      plannedAmount: 0,
      spentAmount: 0,
      remainingAmount: 0,
      color: this.categoryColors[this.newBudget.categories.length % this.categoryColors.length],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      spentPercentage: 0,
      isOverBudget: false,
      isNearLimit: false,
      status: 'on-track'
    };

    this.newBudget.categories.push(newCategory);
  }

  // Remove a category from the new budget
  removeCategory(categoryId: number) {
    if (!this.newBudget.categories) {
      this.newBudget.categories = [];
    }
    this.newBudget.categories = this.newBudget.categories.filter(c => c.id !== categoryId);
  }

  // Create the budget
  createBudget() {
    if (!this.newBudget.name || !this.newBudget.startDate || !this.newBudget.endDate || !this.newBudget.categories || this.newBudget.categories.length === 0) {
      alert('Please fill in all required fields and add at least one category.');
      return;
    }

    const budget: BudgetSummary = {
      id: Date.now(),
      name: this.newBudget.name,
      userId: 1, // Placeholder, will be replaced by actual user ID
      startDate: this.newBudget.startDate,
      endDate: this.newBudget.endDate,
      totalPlanned: this.newBudget.categories.reduce((sum, cat) => sum + cat.plannedAmount, 0),
      totalSpent: 0,
      totalRemaining: this.newBudget.categories.reduce((sum, cat) => sum + cat.plannedAmount, 0),
      isActive: true,
      isRepeatable: this.newBudget.isRepeatable || false,
      repeatInterval: this.newBudget.repeatInterval || null,
      repeatCount: this.newBudget.repeatCount || 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      spentPercentage: 0,
      isOverBudget: false,
      isNearLimit: false,
      status: 'on-track',
      categories: this.newBudget.categories.map(cat => ({
        ...cat,
        remainingAmount: cat.plannedAmount - cat.spentAmount
      })),
      totalCategories: this.newBudget.categories.length,
      activeCategories: this.newBudget.categories.filter(cat => cat.plannedAmount > cat.spentAmount).length,
      overBudgetCategories: this.newBudget.categories.filter(cat => cat.plannedAmount <= cat.spentAmount).length,
      nearLimitCategories: this.newBudget.categories.filter(cat => cat.plannedAmount > cat.spentAmount && cat.plannedAmount - cat.spentAmount < 0.2 * cat.plannedAmount).length // Example near limit calculation
    };

    this.budgets.push(budget);

    // Try to save to backend first
    this.saveBudgetToBackend(budget);

    this.isCreatingBudget = false;
    this.newBudget = {};
  }

  // Cancel budget creation
  cancelCreateBudget() {
    this.isCreatingBudget = false;
    this.newBudget = {};
  }

  // Select a budget to view
  selectBudget(budget: BudgetSummary) {
    this.currentBudget = budget;
    this.isCreatingBudget = false;
    this.isEditingBudget = false;
  }

  // Edit budget category
  editCategory(category: BudgetCategorySummary) {
    this.editingCategory = { ...category };
  }

  // Save category changes
  saveCategoryChanges() {
    if (!this.editingCategory || !this.currentBudget) return;

    const categoryIndex = this.currentBudget.categories.findIndex(c => c.id === this.editingCategory!.id);
    if (categoryIndex !== -1) {
      this.currentBudget.categories[categoryIndex] = { ...this.editingCategory };
      this.updateBudgetTotals();

      // Try to save to backend first
      this.saveBudgetToBackend(this.currentBudget);
    }

    this.editingCategory = null;
  }

  // Cancel category editing
  cancelCategoryEditing() {
    this.editingCategory = null;
  }

  // Update budget totals
  updateBudgetTotals() {
    if (!this.currentBudget) return;

    this.currentBudget.totalPlanned = this.currentBudget.categories.reduce((sum, cat) => sum + cat.plannedAmount, 0);
    this.currentBudget.totalSpent = this.currentBudget.categories.reduce((sum, cat) => sum + cat.spentAmount, 0);
    this.currentBudget.totalRemaining = this.currentBudget.totalPlanned - this.currentBudget.totalSpent;
    this.currentBudget.spentPercentage = this.currentBudget.totalPlanned === 0 ? 0 : (this.currentBudget.totalSpent / this.currentBudget.totalPlanned) * 100;
    this.currentBudget.isOverBudget = this.currentBudget.spentPercentage > 100;
    this.currentBudget.isNearLimit = this.currentBudget.spentPercentage > 80 && this.currentBudget.spentPercentage < 100; // Example near limit calculation
    this.currentBudget.status = this.currentBudget.isOverBudget ? 'over-budget' : this.currentBudget.isNearLimit ? 'near-limit' : 'on-track';
  }

  // Delete budget
  deleteBudget(budgetId: number) {
    if (confirm('Are you sure you want to delete this budget?')) {
      this.budgets = this.budgets.filter(b => b.id !== budgetId);

      // Try to delete from backend first
      this.http.delete(`${this.API_BASE_URL}/budgets/${budgetId}`)
        .subscribe({
          next: () => {
            console.log('Budget deleted from backend');
          },
          error: (error) => {
            console.error('Error deleting budget from backend:', error);
            // Fallback to localStorage
            this.saveBudgetsToLocalStorage();
          }
        });

      if (this.currentBudget?.id === budgetId) {
        this.currentBudget = null;
      }
    }
  }

  // Utility methods
  getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  getDateInFuture(days: number): string {
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + days);
    return futureDate.toISOString().split('T')[0];
  }

  // Get budget progress percentage
  getBudgetProgress(budget: BudgetSummary): number {
    if (budget.totalPlanned === 0) return 0;
    return Math.min((budget.totalSpent / budget.totalPlanned) * 100, 100);
  }

  // Get budget status color
  getBudgetStatusColor(budget: BudgetSummary): string {
    const progress = this.getBudgetProgress(budget);
    if (progress >= 100) return '#dc3545'; // Red for over budget
    if (progress >= 80) return '#ffc107'; // Yellow for near limit
    return '#28a745'; // Green for good
  }

  // Get repeat interval display text
  getRepeatDisplayText(budget: BudgetSummary): string {
    if (!budget.isRepeatable || !budget.repeatInterval) return '';

    const interval = budget.repeatInterval;
    const count = budget.repeatCount;

    if (count === 1) {
      return `Repeats ${interval}`;
    } else {
      return `Repeats ${interval} (${count} times)`;
    }
  }

  // Calculate progress percentage
  getProgressPercentage(planned: number, spent: number): number {
    if (planned === 0) return 0;
    return Math.min((spent / planned) * 100, 100);
  }

  // Get progress color based on spending
  getProgressColor(planned: number, spent: number): string {
    const percentage = this.getProgressPercentage(planned, spent);
    if (percentage >= 90) return '#dc3545'; // Red
    if (percentage >= 75) return '#ffc107'; // Yellow
    return '#28a745'; // Green
  }

  // Toggle repeatable option
  toggleRepeatable() {
    if (this.newBudget.isRepeatable) {
      this.newBudget.repeatInterval = null;
      this.newBudget.repeatCount = 1;
    }
  }

  // TrackBy methods for better performance
  trackBudgetById(index: number, budget: BudgetSummary): number {
    return budget.id;
  }

  trackCategoryById(index: number, category: BudgetCategorySummary): number {
    return category.id;
  }

  // Save budget to backend API
  saveBudgetToBackend(budget: BudgetSummary) {
    if (budget.id) {
      // Update existing budget
      this.http.put<BudgetSummary>(`${this.API_BASE_URL}/budgets/${budget.id}`, budget)
        .subscribe({
          next: (updatedBudget) => {
            console.log('Budget updated in backend:', updatedBudget);
            this.loadDashboard(); // Reload to get fresh data
          },
          error: (error) => {
            console.error('Error updating budget in backend:', error);
            // Fallback to localStorage
            this.saveBudgetsToLocalStorage();
          }
        });
    } else {
      // Create new budget
      this.http.post<BudgetSummary>(`${this.API_BASE_URL}/budgets/`, budget)
        .subscribe({
          next: (createdBudget) => {
            console.log('Budget created in backend:', createdBudget);
            this.loadDashboard(); // Reload to get fresh data
          },
          error: (error) => {
            console.error('Error creating budget in backend:', error);
            // Fallback to localStorage
            this.saveBudgetsToLocalStorage();
          }
        });
    }
  }

  // Fallback to localStorage if backend fails
  private saveBudgetsToLocalStorage() {
    localStorage.setItem('budgets', JSON.stringify(this.budgets));
    console.log('Budgets saved to localStorage (fallback)');
  }

  // Check if a category is already added to prevent duplicates
  isCategoryAlreadyAdded(categoryName: string): boolean {
    return this.newBudget.categories?.some(cat => cat.name === categoryName) || false;
  }

  // Helper method to format daily spending allowance
  getFormattedDailySpendingAllowance(budget: BudgetSummary): string {
    if (budget.dailySpendingAllowance === undefined || budget.dailySpendingAllowance === null) {
      return 'N/A';
    }

    if (budget.dailySpendingAllowance <= 0) {
      return 'Budget exceeded';
    }

    return `$${budget.dailySpendingAllowance.toFixed(2)}`;
  }

  // Helper method to get days remaining
  getDaysRemaining(budget: BudgetSummary): number | null {
    if (budget.daysRemaining !== undefined && budget.daysRemaining !== null) {
      return budget.daysRemaining;
    }

    // Calculate if not provided by backend
    try {
      const startDate = new Date(budget.startDate);
      const endDate = new Date(budget.endDate);
      const today = new Date();

      // If budget has ended, return 0
      if (today > endDate) {
        return 0;
      }

      // If budget hasn't started yet, return full period
      if (today < startDate) {
        const timeDiff = endDate.getTime() - startDate.getTime();
        return Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
      }

      // Calculate remaining days from today to end date
      const timeDiff = endDate.getTime() - today.getTime();
      const remainingDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
      return Math.max(0, remainingDays);
    } catch (error) {
      return null;
    }
  }

  // Helper method to get daily spending description
  getDailySpendingDescription(budget: BudgetSummary): string {
    const dailyAllowance = budget.dailySpendingAllowance;
    const daysRemaining = this.getDaysRemaining(budget);

    if (dailyAllowance === undefined || dailyAllowance === null) {
      return 'Unable to calculate daily allowance';
    }

    if (dailyAllowance <= 0) {
      return 'Budget exceeded - no daily allowance available';
    }

    if (daysRemaining !== null && daysRemaining > 0) {
      return `$${dailyAllowance.toFixed(2)} per day for ${daysRemaining} days`;
    }

    return `$${dailyAllowance.toFixed(2)} per day`;
  }

  // Navigation method
  goBack() {
    this.router.navigate(['/home']);
  }
}
