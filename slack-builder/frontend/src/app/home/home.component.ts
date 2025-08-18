import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Chart, ChartConfiguration, ChartData } from 'chart.js/auto';
import { Router } from '@angular/router';
import { trigger, state, style, transition, animate } from '@angular/animations';

export interface Transaction {
  id?: number;
  amount: number;
  vendor: string;
  type: 'purchase' | 'refund' | 'expense' | 'income' | 'transfer' | 'other';
  category: string;
  date: string;
  notes?: string;
  userId?: number;
}

interface CategoryData {
  category: string;
  total: number;
  count: number;
  percentage: number;
  monthlyBreakdown?: Array<{
    category: string;
    amount: number;
  }>;
  transactions?: Transaction[]; // Add transactions for yearly view
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  animations: [
    trigger('slideDown', [
      state('false', style({
        height: '0',
        opacity: '0',
        overflow: 'hidden'
      })),
      state('true', style({
        height: '*',
        opacity: '1',
        overflow: 'visible'
      })),
      transition('false <=> true', [
        animate('300ms ease-in-out')
      ])
    ])
  ]
})
export class HomeComponent implements OnInit, AfterViewInit {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  isSyncing = false;
  isTimeFrameLoading = false; // New loading state for time frame changes
  chartType: 'pie' | 'bar' = 'pie';
  currentChart: Chart | null = null;

  // Time frame for spending overview
  timeFrame: 'week' | 'month' | 'year' = 'month';

  // Remove budget planner modal properties
  // showBudgetPlanner = false;

  constructor(private http: HttpClient, private router: Router) {}

  // Update budget planner method to navigate to the page
  openBudgetPlanner() {
    this.router.navigate(['/budget-planner']);
  }

  // Pagination properties
  currentPage = 1;
  transactionsPerPage = 5; // Show 5 transactions per page

  // Backend API URL - adjust this to match your backend port
  private apiUrl = 'http://localhost:8080/transactions/';

  // User ID for fetching transactions - you can make this configurable
  private userId = 1;

  // Transactions from backend
  allTransactions: Transaction[] = [];

  // Loading and error states
  isLoading = false;
  error: string | null = null;

  // Edit functionality
  editingTransactionId: number | null = null;
  editingTransaction: Transaction | null = null;

  // Chart data
  categoryData: CategoryData[] = [];
  totalSpending = 0;

  // Budget selection for spending overview
  selectedBudgetId: string = '';
  availableBudgets: any[] = [];

  // Available categories and transaction types from backend
  availableCategories: string[] = [];
  availableTransactionTypes: string[] = [];

  // Category selection for detailed view
  selectedCategory: string | null = null;
  filteredTransactions: Transaction[] = [];

  // Add expanded categories tracking for dropdown effect
  expandedCategories: Set<string> = new Set();

  // Pagination for dropdown transactions
  dropdownPageSize = 5; // Show 5 transactions per page
  dropdownPages: Map<string, number> = new Map();

  ngOnInit() {
    // Fetch data for the default time frame (month)
    this.fetchTransactions(this.timeFrame);
    this.loadCategories();
    this.loadTransactionTypes();
    this.loadBudgets();
  }

  ngAfterViewInit() {
    // Chart will be initialized after data is loaded
  }

  // Load available budgets for the dropdown
  loadBudgets() {
    this.http.get<any[]>(`http://localhost:8080/budgets/?userId=${this.userId}`)
      .subscribe({
        next: (budgets) => {
          this.availableBudgets = budgets;
          console.log('Loaded budgets for user', this.userId, ':', budgets);
        },
        error: (error) => {
          console.error('Error loading budgets:', error);
          this.availableBudgets = [];
        }
      });
  }

  // Handle budget selection change
  onBudgetChange() {
    this.processChartData();
    if (this.currentChart) {
      this.updateChart();
    }
  }

  // Get spending overview title based on selected budget
  getSpendingOverviewTitle(): string {
    if (this.selectedBudgetId) {
      const selectedBudget = this.availableBudgets.find(b => b.id.toString() === this.selectedBudgetId);
      if (selectedBudget) {
        return `Spending Overview - ${selectedBudget.name}`;
      }
    }
    return 'Spending Overview';
  }

  // Edit functionality methods
  startEdit(transaction: Transaction) {
    if (transaction.id !== undefined) {
      this.editingTransactionId = transaction.id;
      this.editingTransaction = { ...transaction }; // Create a copy for editing
    }
  }

  cancelEdit() {
    this.editingTransactionId = null;
    this.editingTransaction = null;
  }

  saveEdit() {
    if (!this.editingTransaction) return;

    this.isLoading = true;
    const url = `${this.apiUrl}${this.editingTransaction.id}`;

    this.http.put<Transaction>(url, this.editingTransaction)
      .subscribe({
        next: (updatedTransaction) => {
          // Update the transaction in the local array
          const index = this.allTransactions.findIndex(t => t.id === updatedTransaction.id);
          if (index !== -1) {
            this.allTransactions[index] = updatedTransaction;
          }

          this.editingTransactionId = null;
          this.editingTransaction = null;
          this.isLoading = false;

          // Refresh chart data
          this.processChartData();

          // Show success message
          alert('Transaction updated successfully!');
        },
        error: (error) => {
          console.error('Error updating transaction:', error);
          this.error = 'Failed to update transaction. Please try again.';
          this.isLoading = false;
        }
      });
  }

  isEditing(transactionId: number): boolean {
    return this.editingTransactionId === transactionId;
  }

  // Get appropriate symbol for transaction type
  getTransactionSymbol(type: string): string {
    switch (type) {
      case 'purchase':
      case 'expense':
        return '-';
      case 'income':
      case 'refund':
        return '+';
      case 'transfer':
      case 'other':
        return '±';
      default:
        return '';
    }
  }

  // Process transactions for chart data
  processChartData() {
    if (this.timeFrame === 'year') {
      this.processYearlyTrendData();
    } else {
      this.processCategoryData();
    }
  }

  // Standardize categories to match Slack bot options
  standardizeCategory(category: string | null | undefined): string {
    if (!category) return 'Uncategorized';

    const lowerCategory = category.toLowerCase();

    // Map all categories to the 8 standard Slack bot categories
    if (lowerCategory.includes('health') ||
        lowerCategory.includes('medical') ||
        lowerCategory.includes('doctor') ||
        lowerCategory.includes('pharmacy') ||
        lowerCategory.includes('dental') ||
        lowerCategory.includes('vision') ||
        lowerCategory.includes('therapy') ||
        lowerCategory.includes('urgent') ||
        lowerCategory.includes('care') ||
        lowerCategory.includes('hospital') ||
        lowerCategory.includes('clinic') ||
        lowerCategory.includes('physician') ||
        lowerCategory.includes('nurse') ||
        lowerCategory.includes('ambulance') ||
        lowerCategory.includes('emergency')) {
      return 'Healthcare';
    }

    if (lowerCategory.includes('food') ||
        lowerCategory.includes('dining') ||
        lowerCategory.includes('restaurant') ||
        lowerCategory.includes('cafe') ||
        lowerCategory.includes('coffee') ||
        lowerCategory.includes('grocery') ||
        lowerCategory.includes('meal') ||
        lowerCategory.includes('lunch') ||
        lowerCategory.includes('dinner') ||
        lowerCategory.includes('breakfast') ||
        lowerCategory.includes('snack') ||
        lowerCategory.includes('takeout') ||
        lowerCategory.includes('delivery')) {
      return 'Food & Dining';
    }

    if (lowerCategory.includes('shopping') ||
        lowerCategory.includes('store') ||
        lowerCategory.includes('mall') ||
        lowerCategory.includes('retail') ||
        lowerCategory.includes('clothing') ||
        lowerCategory.includes('apparel') ||
        lowerCategory.includes('fashion') ||
        lowerCategory.includes('electronics') ||
        lowerCategory.includes('gadget') ||
        lowerCategory.includes('book') ||
        lowerCategory.includes('toy') ||
        lowerCategory.includes('gift') ||
        lowerCategory.includes('home') ||
        lowerCategory.includes('garden') ||
        lowerCategory.includes('furniture') ||
        lowerCategory.includes('decor') ||
        lowerCategory.includes('jewelry') ||
        lowerCategory.includes('accessory')) {
      return 'Shopping';
    }

    if (lowerCategory.includes('transportation') ||
        lowerCategory.includes('transport') ||
        lowerCategory.includes('uber') ||
        lowerCategory.includes('lyft') ||
        lowerCategory.includes('taxi') ||
        lowerCategory.includes('bus') ||
        lowerCategory.includes('train') ||
        lowerCategory.includes('subway') ||
        lowerCategory.includes('gas') ||
        lowerCategory.includes('fuel') ||
        lowerCategory.includes('parking') ||
        lowerCategory.includes('toll') ||
        lowerCategory.includes('car') ||
        lowerCategory.includes('vehicle') ||
        lowerCategory.includes('maintenance') ||
        lowerCategory.includes('repair') ||
        lowerCategory.includes('mechanic')) {
      return 'Transportation';
    }

    if (lowerCategory.includes('entertainment') ||
        lowerCategory.includes('entertain') ||
        lowerCategory.includes('movie') ||
        lowerCategory.includes('theater') ||
        lowerCategory.includes('concert') ||
        lowerCategory.includes('show') ||
        lowerCategory.includes('game') ||
        lowerCategory.includes('gaming') ||
        lowerCategory.includes('sport') ||
        lowerCategory.includes('fitness') ||
        lowerCategory.includes('gym') ||
        lowerCategory.includes('workout') ||
        lowerCategory.includes('hobby') ||
        lowerCategory.includes('leisure') ||
        lowerCategory.includes('fun') ||
        lowerCategory.includes('amusement') ||
        lowerCategory.includes('park') ||
        lowerCategory.includes('attraction')) {
      return 'Entertainment';
    }

    if (lowerCategory.includes('utilities') ||
        lowerCategory.includes('utility') ||
        lowerCategory.includes('bill') ||
        lowerCategory.includes('electric') ||
        lowerCategory.includes('electricity') ||
        lowerCategory.includes('water') ||
        lowerCategory.includes('gas') ||
        lowerCategory.includes('heat') ||
        lowerCategory.includes('cooling') ||
        lowerCategory.includes('internet') ||
        lowerCategory.includes('wifi') ||
        lowerCategory.includes('phone') ||
        lowerCategory.includes('telephone') ||
        lowerCategory.includes('cable') ||
        lowerCategory.includes('tv') ||
        lowerCategory.includes('television') ||
        lowerCategory.includes('rent') ||
        lowerCategory.includes('mortgage') ||
        lowerCategory.includes('insurance') ||
        lowerCategory.includes('subscription') ||
        lowerCategory.includes('service')) {
      return 'Utilities & Bills';
    }

    if (lowerCategory.includes('education') ||
        lowerCategory.includes('school') ||
        lowerCategory.includes('college') ||
        lowerCategory.includes('university') ||
        lowerCategory.includes('course') ||
        lowerCategory.includes('class') ||
        lowerCategory.includes('training') ||
        lowerCategory.includes('workshop') ||
        lowerCategory.includes('seminar') ||
        lowerCategory.includes('tutorial') ||
        lowerCategory.includes('lesson') ||
        lowerCategory.includes('study') ||
        lowerCategory.includes('book') ||
        lowerCategory.includes('textbook') ||
        lowerCategory.includes('tuition') ||
        lowerCategory.includes('fee') ||
        lowerCategory.includes('academic')) {
      return 'Education';
    }

    if (lowerCategory.includes('travel') ||
        lowerCategory.includes('trip') ||
        lowerCategory.includes('vacation') ||
        lowerCategory.includes('flight') ||
        lowerCategory.includes('airline') ||
        lowerCategory.includes('hotel') ||
        lowerCategory.includes('lodging') ||
        lowerCategory.includes('accommodation') ||
        lowerCategory.includes('resort') ||
        lowerCategory.includes('cruise') ||
        lowerCategory.includes('tour') ||
        lowerCategory.includes('sightseeing') ||
        lowerCategory.includes('adventure') ||
        lowerCategory.includes('getaway') ||
        lowerCategory.includes('journey') ||
        lowerCategory.includes('expedition')) {
      return 'Travel';
    }

    // If no match found, return as Uncategorized
    return 'Uncategorized';
  }

  // Process data for weekly/monthly views (category breakdown)
  processCategoryData() {
    const categoryMap = new Map<string, number>();
    let total = 0;

    // Get filtered transactions based on time frame
    const filteredTransactions = this.getFilteredTransactionsByTimeFrame();
    const timeFrameRange = this.getCurrentTimeFrameRange();

    console.log(`\n🔍 IMMEDIATE DEBUG: Processing category data for ${this.timeFrame}`);
    console.log(`🔍 IMMEDIATE DEBUG: Time frame range: ${timeFrameRange.start} to ${timeFrameRange.end}`);
    console.log(`🔍 IMMEDIATE DEBUG: Raw filtered transactions count: ${filteredTransactions.length}`);

    // Show ALL transactions that are being processed (this will reveal the issue)
    console.log(`🔍 IMMEDIATE DEBUG: ALL transactions being processed:`);
    filteredTransactions.forEach((t, index) => {
      const transactionDate = new Date(t.date);
      const isInRange = transactionDate >= new Date(timeFrameRange.start) && transactionDate <= new Date(timeFrameRange.end);
      console.log(`  ${index + 1}. ${t.date} (${transactionDate.toLocaleDateString()}) | ${t.vendor} | $${t.amount} | ${t.category} | IN_RANGE: ${isInRange}`);
    });

    // Only include spending transactions (purchase, expense)
    filteredTransactions.forEach(transaction => {
      if (transaction.type === 'purchase' || transaction.type === 'expense') {
        // Standardize healthcare categories
        const standardizedCategory = this.standardizeCategory(transaction.category);
        const currentTotal = categoryMap.get(standardizedCategory) || 0;
        categoryMap.set(standardizedCategory, currentTotal + transaction.amount);
        total += transaction.amount;
      }
    });

    console.log(`🔍 IMMEDIATE DEBUG: Spending transactions count: ${filteredTransactions.filter(t => t.type === 'purchase' || t.type === 'expense').length}`);
    console.log(`🔍 IMMEDIATE DEBUG: Total spending amount: ${total}`);

    // Convert to array and calculate percentages
    this.categoryData = Array.from(categoryMap.entries()).map(([category, categoryTotal]) => ({
      category,
      total: categoryTotal,
      count: filteredTransactions.filter(t =>
        (t.type === 'purchase' || t.type === 'expense') &&
        this.standardizeCategory(t.category) === category
      ).length,
      percentage: total > 0 ? (categoryTotal / total) * 100 : 0
    }));

    // Sort by total amount (descending)
    this.categoryData.sort((a, b) => b.total - a.total);
    this.totalSpending = total;

    // COMPREHENSIVE DEBUGGING: Print all transactions for each category
    console.log(`\n=== COMPREHENSIVE CATEGORY DEBUGGING FOR ${this.timeFrame.toUpperCase()} ===`);
    console.log(`Time frame: ${this.timeFrame} (${timeFrameRange.start} to ${timeFrameRange.end})`);
    console.log(`Total filtered transactions: ${filteredTransactions.length}`);
    console.log(`Total spending amount: $${total.toFixed(2)}`);
    console.log(`\n--- BREAKDOWN BY CATEGORY ---`);

    this.categoryData.forEach(category => {
      console.log(`${category.category}: $${category.total.toFixed(2)} (${category.count} transactions, ${category.percentage.toFixed(1)}%)`);
    });

    // SPECIAL DEBUGGING: Check for healthcare-related transactions
    console.log(`\n--- HEALTHCARE TRANSACTION ANALYSIS ---`);
    const healthcareTransactions = filteredTransactions.filter(t =>
      (t.type === 'purchase' || t.type === 'expense') &&
      (t.category?.toLowerCase().includes('health') ||
       t.category?.toLowerCase().includes('medical') ||
       t.category?.toLowerCase().includes('doctor') ||
       t.category?.toLowerCase().includes('pharmacy') ||
       t.category?.toLowerCase().includes('dental') ||
       t.category?.toLowerCase().includes('vision') ||
       t.category?.toLowerCase().includes('therapy') ||
       t.category?.toLowerCase().includes('urgent') ||
       t.category?.toLowerCase().includes('care'))
    );

    if (healthcareTransactions.length > 0) {
      console.log(`Found ${healthcareTransactions.length} healthcare-related transactions:`);
      healthcareTransactions.forEach(t => {
        console.log(`  - ${t.date}: $${t.amount.toFixed(2)} | ${t.vendor} | Category: "${t.category}" | Type: ${t.type}`);
      });
    } else {
      console.log(`No healthcare-related transactions found in current time frame.`);
    }

    // Check for any transactions with null/undefined categories
    const uncategorizedTransactions = filteredTransactions.filter(t =>
      (t.type === 'purchase' || t.type === 'expense') &&
      (!t.category || t.category === 'Uncategorized')
    );

    if (uncategorizedTransactions.length > 0) {
      console.log(`\nFound ${uncategorizedTransactions.length} uncategorized transactions:`);
      uncategorizedTransactions.forEach(t => {
        console.log(`  - ${t.date}: $${t.amount.toFixed(2)} | ${t.vendor} | Category: "${t.category}" | Type: ${t.type}`);
      });
    }

    // Show category standardization mapping
    console.log(`\n--- CATEGORY STANDARDIZATION MAPPING ---`);
    const uniqueCategories = new Set(filteredTransactions.map(t => t.category).filter(Boolean));
    uniqueCategories.forEach(originalCategory => {
      const standardized = this.standardizeCategory(originalCategory);
      if (originalCategory !== standardized) {
        console.log(`"${originalCategory}" → "${standardized}"`);
      }
    });

    console.log(`=== END CATEGORY DEBUGGING ===\n`);
  }

  // Process data for yearly view (monthly trends)
  processYearlyTrendData() {
    const monthlyData = new Map<string, Map<string, number>>();
    const monthlyTransactionCounts = new Map<string, number>();
    const monthlyTransactions = new Map<string, Transaction[]>();
    let total = 0;

    // Get filtered transactions for the year
    const filteredTransactions = this.getFilteredTransactionsByTimeFrame();
    const timeFrameRange = this.getCurrentTimeFrameRange();

    console.log(`DEBUG: Processing yearly trend data`);
    console.log(`DEBUG: Time frame range: ${timeFrameRange.start} to ${timeFrameRange.end}`);
    console.log(`DEBUG: Filtered transactions count: ${filteredTransactions.length}`);

    // Get current date to determine which months to show
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth(); // 0-11 (January = 0)

    console.log(`DEBUG: Current date: ${currentDate.toLocaleDateString()}`);
    console.log(`DEBUG: Current year: ${currentYear}, Current month: ${currentMonth}`);

    // Only initialize months up to the current month
    for (let i = 0; i <= currentMonth; i++) {
      const monthName = new Date(currentYear, i, 1).toLocaleString('default', { month: 'short' });
      monthlyData.set(monthName, new Map());
      monthlyTransactionCounts.set(monthName, 0);
      monthlyTransactions.set(monthName, []);
    }

    // Process transactions by month and category
    filteredTransactions.forEach(transaction => {
      if (transaction.type === 'purchase' || transaction.type === 'expense') {
        const transactionDate = new Date(transaction.date);
        const transactionMonth = transactionDate.getMonth();
        const transactionYear = transactionDate.getFullYear();

        // Only process transactions from the current year and up to current month
        if (transactionYear === currentYear && transactionMonth <= currentMonth) {
          const monthName = transactionDate.toLocaleString('default', { month: 'short' });
          const category = transaction.category || 'Uncategorized';

          const monthMap = monthlyData.get(monthName);
          if (monthMap) {
            const currentTotal = monthMap.get(category) || 0;
            monthMap.set(category, currentTotal + transaction.amount);

            // Count transactions per month
            const currentCount = monthlyTransactionCounts.get(monthName) || 0;
            monthlyTransactionCounts.set(monthName, currentCount + 1);

            // Store transactions for each month
            const monthTransactionList = monthlyTransactions.get(monthName) || [];
            monthTransactionList.push(transaction);
            monthlyTransactions.set(monthName, monthTransactionList);

            total += transaction.amount;
          }
        }
      }
    });

    // Convert to chart data format for time series
    const months = Array.from(monthlyData.keys());
    const categories = this.getUniqueCategories(filteredTransactions);

    this.categoryData = months.map(month => {
      const monthMap = monthlyData.get(month) || new Map();
      const monthTotal = Array.from(monthMap.values()).reduce((sum, amount) => sum + amount, 0);
      const monthTransactionCount = monthlyTransactionCounts.get(month) || 0;
      const monthTransactionList = monthlyTransactions.get(month) || [];

      return {
        category: month,
        total: monthTotal,
        count: monthTransactionCount, // Now properly counts transactions per month
        percentage: total > 0 ? (monthTotal / total) * 100 : 0,
        monthlyBreakdown: categories.map(cat => ({
          category: cat,
          amount: monthMap.get(cat) || 0
        })),
        // Add transactions for this month so they can be displayed
        transactions: monthTransactionList
      };
    });

    this.totalSpending = total;

    console.log(`DEBUG: Final yearly trend data:`, this.categoryData);
    console.log(`DEBUG: Monthly transaction counts:`, Array.from(monthlyTransactionCounts.entries()));
    console.log(`DEBUG: Total spending: $${total.toFixed(2)}`);
    console.log(`DEBUG: Months shown: ${months.join(', ')}`);

    // Update chart if it exists
    if (this.currentChart) {
      this.updateChart();
    }
  }

  // Get unique categories from transactions
  getUniqueCategories(transactions: Transaction[]): string[] {
    const categories = new Set<string>();
    transactions.forEach(transaction => {
      if (transaction.type === 'purchase' || transaction.type === 'expense') {
        categories.add(transaction.category || 'Uncategorized');
      }
    });
    return Array.from(categories).sort();
  }

  // Create or update chart
  createChart() {
    if (this.currentChart) {
      this.currentChart.destroy();
    }

    // Don't create chart if no data
    if (this.categoryData.length === 0) {
      console.log('DEBUG: No category data available, skipping chart creation');
      return;
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) {
      console.error('DEBUG: Could not get chart context');
      return;
    }

    console.log('DEBUG: Creating chart with data:', this.categoryData);

    let chartData: ChartData;
    let chartOptions: any;

    if (this.timeFrame === 'year') {
      // Yearly view: Stacked bar chart showing monthly trends by category
      chartData = this.createYearlyTrendChartData();
      chartOptions = this.createYearlyTrendChartOptions();
    } else {
      // Weekly/Monthly view: Pie or bar chart showing category breakdown
      chartData = this.createCategoryChartData();
      chartOptions = this.createCategoryChartOptions();
    }

    const config: ChartConfiguration = {
      type: this.timeFrame === 'year' ? 'bar' : this.chartType,
      data: chartData,
      options: chartOptions
    };

    this.currentChart = new Chart(ctx, config);
  }

  // Create chart data for category breakdown (weekly/monthly views)
  createCategoryChartData(): ChartData {
    return {
      labels: this.categoryData.map(d => d.category),
      datasets: [{
        data: this.categoryData.map(d => d.total),
        backgroundColor: this.getChartColors(),
        borderColor: this.getChartColors(),
        borderWidth: 1
      }]
    };
  }

  // Create chart data for yearly trends (monthly breakdown)
  createYearlyTrendChartData(): ChartData {
    const categories = this.getUniqueCategories(this.allTransactions);
    const months = this.categoryData.map(d => d.category);

    const datasets = categories.map((category, index) => ({
      label: category,
      data: months.map(month => {
        const monthData = this.categoryData.find(d => d.category === month);
        if (monthData && monthData.monthlyBreakdown) {
          const categoryData = monthData.monthlyBreakdown.find(c => c.category === category);
          return categoryData ? categoryData.amount : 0;
        }
        return 0;
      }),
      backgroundColor: this.getChartColor(index),
      borderColor: this.getChartColor(index),
      borderWidth: 1,
      stack: 'Stack 0'
    }));

    return {
      labels: months,
      datasets: datasets
    };
  }

  // Create chart options for category breakdown
  createCategoryChartOptions(): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      onClick: (event: any, elements: any[]) => this.onChartClick(event, elements),
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true,
            font: { size: 12 }
          }
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const data = this.categoryData[context.dataIndex];
              return `${data.category}: ${data.total.toFixed(2)} (${data.percentage.toFixed(1)}%)`;
            }
          }
        }
      }
    };
  }

  // Create chart options for yearly trends
  createYearlyTrendChartOptions(): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      onClick: (event: any, elements: any[]) => this.onChartClick(event, elements),
      scales: {
        x: {
          stacked: true,
          title: {
            display: true,
            text: 'Month'
          }
        },
        y: {
          stacked: true,
          title: {
            display: true,
            text: 'Amount ($)'
          }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true,
            font: { size: 12 }
          }
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const dataset = context.dataset;
              const value = context.parsed.y;
              return `${dataset.label}: $${value.toFixed(2)}`;
            }
          }
        }
      }
    };
  }

  // Get a specific chart color by index
  getChartColor(index: number): string {
    const colors = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4',
      '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
      '#FF8A80', '#80CBC4', '#81C784', '#FFB74D',
      '#BA68C8', '#4FC3F7', '#FFD54F', '#A1887F'
    ];
    return colors[index % colors.length];
  }

  // Update existing chart
  updateChart() {
    if (!this.currentChart) {
      return;
    }

    // Determine what to show based on time frame
    if (this.timeFrame === 'year') {
      // For yearly view, show months as categories
      this.updateYearlyChart();
    } else {
      // For weekly/monthly view, show categories
      this.updateCategoryChart();
    }
  }

  // Update chart for yearly view (months as categories)
  updateYearlyChart() {
    if (!this.currentChart) return;

    const months = this.categoryData.map(item => item.category);
    const amounts = this.categoryData.map(item => item.total);
    const transactionCounts = this.categoryData.map(item => item.count);

    this.currentChart.data.labels = months;
    this.currentChart.data.datasets[0].data = amounts;
    this.currentChart.data.datasets[0].label = 'Monthly Spending';

    // Update tooltip to show transaction counts
    if (this.currentChart.options && this.currentChart.options.plugins && this.currentChart.options.plugins.tooltip) {
      this.currentChart.options.plugins.tooltip.callbacks = {
        label: (context: any) => {
          const month = months[context.dataIndex];
          const amount = amounts[context.dataIndex];
          const count = transactionCounts[context.dataIndex];
          return `${month}: $${amount.toFixed(2)} (${count} transactions)`;
        }
      };
    }

    this.currentChart.update();
  }

  // Update chart for weekly/monthly view (categories)
  updateCategoryChart() {
    if (!this.currentChart) return;

    const categories = this.categoryData.map(item => item.category);
    const amounts = this.categoryData.map(item => item.total);
    const percentages = this.categoryData.map(item => item.percentage);

    this.currentChart.data.labels = categories;
    this.currentChart.data.datasets[0].data = amounts;
    this.currentChart.data.datasets[0].label = 'Category Spending';

    // Update tooltip to show percentages
    if (this.currentChart.options && this.currentChart.options.plugins && this.currentChart.options.plugins.tooltip) {
      this.currentChart.options.plugins.tooltip.callbacks = {
        label: (context: any) => {
          const category = categories[context.dataIndex];
          const amount = amounts[context.dataIndex];
          const percentage = percentages[context.dataIndex];
          return `${category}: $${amount.toFixed(2)} (${percentage.toFixed(1)}%)`;
        }
      };
    }

    this.currentChart.update();
  }

  // Get chart colors
  getChartColors(): string[] {
    return [
      '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
      '#FF9F40', '#FF6384', '#C9CBCF', '#4BC0C0', '#FF6384'
    ];
  }

  // Set time frame for spending overview
  setTimeFrame(timeFrame: 'week' | 'month' | 'year') {
    console.log(`\n🔄 TIME FRAME CHANGE: ${this.timeFrame} → ${timeFrame}`);
    console.log(`DEBUG: Setting time frame to: ${timeFrame}`);
    console.log(`DEBUG: This will trigger a backend API call to fetch new data`);

    this.timeFrame = timeFrame;

    // Fetch new data from backend for the selected time frame
    console.log(`DEBUG: Fetching new data for time frame: ${timeFrame}`);
    console.log(`DEBUG: Backend call will be made with date range parameters`);
    this.fetchTransactions(timeFrame);

    // Note: processChartData() will be called automatically after fetchTransactions completes
    // No need to call it here since fetchTransactions handles it
  }

  // Get filtered transactions based on selected time frame and budget
  // Note: Data is now pre-filtered by time frame from the backend
  getFilteredTransactionsByTimeFrame(): Transaction[] {
    console.log(`\n=== TIME FRAME FILTERING DEBUG ===`);
    console.log(`DEBUG: Data already filtered by time frame: ${this.timeFrame} from backend`);
    console.log(`DEBUG: Total transactions available: ${this.allTransactions.length}`);
    console.log(`DEBUG: Budget filter: ${this.selectedBudgetId || 'None'}`);

    // Get the expected date range for the current time frame
    const timeFrameRange = this.getCurrentTimeFrameRange();
    const startDate = new Date(timeFrameRange.start);
    const endDate = new Date(timeFrameRange.end);

    console.log(`DEBUG: Expected date range: ${timeFrameRange.start} to ${timeFrameRange.end}`);
    console.log(`DEBUG: Start date object: ${startDate.toLocaleDateString()}`);
    console.log(`DEBUG: End date object: ${endDate.toLocaleDateString()}`);
    console.log(`DEBUG: Start date timestamp: ${startDate.getTime()}`);
    console.log(`DEBUG: End date timestamp: ${endDate.getTime()}`);

    // Since data is pre-filtered by time frame from backend, we only need to filter by budget
    // BUT add date filtering as a safety measure in case backend filtering isn't working
    let filteredTransactions = [...this.allTransactions];

    // SAFETY CHECK: Filter by date range to ensure we only get transactions within the time frame
    const beforeDateFilter = filteredTransactions.length;
    const filteredOutTransactions: Transaction[] = [];

    filteredTransactions = filteredTransactions.filter(transaction => {
      const transactionDate = new Date(transaction.date);
      const transactionTimestamp = transactionDate.getTime();

      // More precise date comparison
      const isInRange = transactionTimestamp >= startDate.getTime() && transactionTimestamp <= endDate.getTime();

      if (!isInRange) {
        console.log(`DEBUG: Filtering out transaction outside range: ${transaction.date} (${transactionDate.toLocaleDateString()}) - ${transaction.vendor} - $${transaction.amount}`);
        console.log(`DEBUG: Transaction timestamp: ${transactionTimestamp}, Start: ${startDate.getTime()}, End: ${endDate.getTime()}`);
        filteredOutTransactions.push(transaction);
      }

      return isInRange;
    });

    console.log(`DEBUG: Date filter: ${beforeDateFilter} → ${filteredTransactions.length} transactions`);
    console.log(`DEBUG: Filtered out ${filteredOutTransactions.length} transactions outside date range`);

    if (filteredOutTransactions.length > 0) {
      console.log(`DEBUG: Sample filtered out transactions:`);
      filteredOutTransactions.slice(0, 5).forEach(t => {
        console.log(`  - ${t.date} (${new Date(t.date).toLocaleDateString()}) | ${t.vendor} | $${t.amount}`);
      });
    }

    // Filter by budget if one is selected
    if (this.selectedBudgetId) {
      console.log(`DEBUG: Applying budget filter for budget ID: ${this.selectedBudgetId}`);

      // Get the selected budget to check its categories
      const selectedBudget = this.availableBudgets.find(b => b.id.toString() === this.selectedBudgetId);
      if (selectedBudget && selectedBudget.categories) {
        // Check if transaction category matches any of the budget's categories
        const budgetCategoryNames = selectedBudget.categories.map((c: any) => c.name);
        console.log(`DEBUG: Budget categories: [${budgetCategoryNames.join(', ')}]`);

        const beforeBudgetFilter = filteredTransactions.length;
        filteredTransactions = filteredTransactions.filter(transaction =>
          budgetCategoryNames.includes(transaction.category)
        );

        console.log(`DEBUG: Budget filter: ${beforeBudgetFilter} → ${filteredTransactions.length} transactions`);
      }
    }

    console.log(`\n=== FILTERING SUMMARY ===`);
    console.log(`DEBUG: Final filtered transactions: ${filteredTransactions.length}`);
    console.log(`DEBUG: Sample filtered transaction dates:`, filteredTransactions.slice(0, 5).map(t => ({ date: t.date, amount: t.amount, vendor: t.vendor })));
    console.log(`=== END TIME FRAME FILTERING DEBUG ===\n`);

    // Sort by date (most recent first) to ensure consistent ordering
    return filteredTransactions.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime(); // Descending order (newest first)
    });
  }

  // Handle chart click to show category details
  onChartClick(event: any, elements: any[]) {
    if (elements.length > 0) {
      const index = elements[0].index;
      const category = this.categoryData[index].category;

      if (this.selectedCategory === category) {
        // If same category clicked, deselect it
        this.selectedCategory = null;
        this.filteredTransactions = [];
      } else {
        // Select new category and filter transactions
        this.selectedCategory = category;
        this.filterTransactionsByCategory(category);
      }
    }
  }

  // Filter transactions by selected category
  filterTransactionsByCategory(category: string) {
    const filteredTransactions = this.allTransactions.filter(transaction =>
      (transaction.type === 'purchase' || transaction.type === 'expense') &&
      (transaction.category || 'Uncategorized') === category
    );

    // Sort by date (most recent first) to ensure consistent ordering
    this.filteredTransactions = filteredTransactions.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime(); // Descending order (newest first)
    });
  }

  // Clear category selection
  clearCategorySelection() {
    this.selectedCategory = null;
    this.filteredTransactions = [];
  }

  // Fetch transactions from backend based on time frame
  fetchTransactions(timeFrame?: 'week' | 'month' | 'year') {
    this.isLoading = true;
    this.isTimeFrameLoading = true; // Set time frame loading state
    this.error = null;

    // Use provided time frame or current time frame
    const targetTimeFrame = timeFrame || this.timeFrame;

    // Calculate date range for the time frame - ALWAYS relative to TODAY
    const today = new Date();
    today.setHours(23, 59, 59, 999); // End of today

    let startDate: Date;
    switch (targetTimeFrame) {
      case 'week':
        // Last 7 days from today (including today)
        startDate = new Date(today.getTime() - (6 * 24 * 60 * 60 * 1000)); // 6 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      case 'month':
        // Last 30 days from today (including today)
        startDate = new Date(today.getTime() - (29 * 24 * 60 * 60 * 1000)); // 29 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      case 'year':
        // Last 365 days from today (including today)
        startDate = new Date(today.getTime() - (364 * 24 * 60 * 60 * 1000)); // 364 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      default:
        startDate = today;
        startDate.setHours(0, 0, 0, 0);
    }

    // Format dates for backend query (YYYY-MM-DD)
    const startDateStr = startDate.toISOString().split('T')[0];
    const endDateStr = today.toISOString().split('T')[0];

    // Build URL with date range parameters
    const url = `${this.apiUrl}?userId=${this.userId}&startDate=${startDateStr}&endDate=${endDateStr}&sort=date,desc`;

    console.log(`\n🔄 FETCHING TRANSACTIONS FOR ${targetTimeFrame.toUpperCase()}:`);
    console.log(`Today's date: ${today.toLocaleDateString()}`);
    console.log(`Start date: ${startDateStr} (${startDate.toLocaleDateString()})`);
    console.log(`End date: ${endDateStr} (${today.toLocaleDateString()})`);
    console.log(`Date range: ${startDate.toLocaleDateString()} to ${today.toLocaleDateString()}`);
    console.log(`URL: ${url}`);
    console.log(`DEBUG: Backend API call initiated for time frame: ${targetTimeFrame}`);

    // Add headers to try to work around CORS
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    this.http.get<Transaction[]>(url, { headers })
      .subscribe({
        next: (transactions) => {
          console.log(`✅ Successfully fetched ${transactions.length} transactions for ${targetTimeFrame}`);
          console.log(`Date range: ${startDateStr} to ${endDateStr}`);
          console.log(`Sample transactions:`, transactions.slice(0, 3).map(t => ({ date: t.date, amount: t.amount, vendor: t.vendor, type: t.type })));

          // Sort transactions by date (most recent first)
          this.allTransactions = transactions.sort((a, b) => {
            const dateA = new Date(a.date);
            const dateB = new Date(b.date);
            return dateB.getTime() - dateA.getTime(); // Descending order (newest first)
          });

          this.isLoading = false;
          this.isTimeFrameLoading = false; // Clear time frame loading state
          // Reset to first page when new data is loaded
          this.currentPage = 1;
          // Process data for charts
          this.processChartData();
          // Create chart after data is processed
          setTimeout(() => this.createChart(), 100);
        },
        error: (error) => {
          console.error(`❌ Error fetching transactions for ${targetTimeFrame}:`, error);
          console.error('Error details:', {
            status: error.status,
            statusText: error.statusText,
            url: error.url,
            message: error.message
          });

          // Better error handling for CORS issues
          if (error.status === 0) {
            this.error = 'CORS error: Backend is not allowing cross-origin requests. Please check CORS configuration.';
          } else if (error.status === 404) {
            this.error = 'Endpoint not found. Please check if the backend is running and the endpoint exists.';
          } else if (error.status === 500) {
            this.error = 'Server error: Backend encountered an issue. Please check server logs.';
          } else {
            this.error = `Failed to load transactions for ${targetTimeFrame}. Status: ${error.status}. Please try again.`;
          }

          this.isLoading = false;
          this.isTimeFrameLoading = false; // Clear time frame loading state
          // Fallback to empty array
          this.allTransactions = [];

          // Clear chart data
          this.categoryData = [];
          this.totalSpending = 0;

          // Destroy chart if exists
          if (this.currentChart) {
            this.currentChart.destroy();
            this.currentChart = null;
          }
        }
      });
  }

  // Getter for current page transactions
  get recentTransactions(): Transaction[] {
    const startIndex = (this.currentPage - 1) * this.transactionsPerPage;
    const endIndex = startIndex + this.transactionsPerPage;
    return this.allTransactions.slice(startIndex, endIndex);
  }

  // Getter for total pages
  get totalPages(): number {
    return Math.ceil(this.allTransactions.length / this.transactionsPerPage);
  }

  // Getter for page numbers array with smart pagination display
  get pageNumbers(): number[] {
    const pages: number[] = [];
    const totalPages = this.totalPages;

    if (totalPages <= 5) {
      // If 5 or fewer pages, show all pages
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // If more than 5 pages, show smart pagination
      const currentPage = this.currentPage;

      if (currentPage <= 3) {
        // Show first 5 pages
        for (let i = 1; i <= 5; i++) {
          pages.push(i);
        }
      } else if (currentPage >= totalPages - 2) {
        // Show last 5 pages
        for (let i = totalPages - 4; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        // Show current page with 2 pages on each side
        for (let i = currentPage - 2; i <= currentPage + 2; i++) {
          pages.push(i);
        }
      }
    }

    return pages;
  }

  // Check if we need to show ellipsis before current page range
  get showEllipsisBefore(): boolean {
    const totalPages = this.totalPages;
    if (totalPages <= 5) return false;

    const currentPage = this.currentPage;
    if (currentPage <= 3) return false;

    return currentPage > 3;
  }

  // Check if we need to show ellipsis after current page range
  get showEllipsisAfter(): boolean {
    const totalPages = this.totalPages;
    if (totalPages <= 5) return false;

    const currentPage = this.currentPage;
    if (currentPage >= totalPages - 2) return false;

    return currentPage < totalPages - 2;
  }

  // Get the page number to jump to when clicking ellipsis before
  get ellipsisBeforePage(): number {
    const currentPage = this.currentPage;
    return Math.max(1, currentPage - 5);
  }

  // Get the page number to jump to when clicking ellipsis after
  get ellipsisAfterPage(): number {
    const currentPage = this.currentPage;
    const totalPages = this.totalPages;
    return Math.min(totalPages, currentPage + 5);
  }

  syncData() {
    this.isSyncing = true;

    // Fetch fresh data from backend for current time frame
    this.fetchTransactions(this.timeFrame);

    // Simulate sync operation
    setTimeout(() => {
      this.isSyncing = false;
      // Show success message
      alert('Data synchronized successfully!');
    }, 2000);
  }

  setChartType(type: 'pie' | 'bar') {
    this.chartType = type;
    // Recreate chart when type changes
    this.createChart();
  }

  viewAllTransactions(event: Event) {
    event.preventDefault();
    // TODO: Navigate to transactions page or open modal
    alert('Navigate to all transactions page');
  }

  // Pagination methods
  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  goToNextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  isCurrentPage(page: number): boolean {
    return this.currentPage === page;
  }

  // Retry loading data
  retryLoad() {
    this.fetchTransactions();
  }

  // Load standardized categories from backend
  loadCategories() {
    this.http.get<string[]>(`${this.apiUrl}categories`)
      .subscribe({
        next: (categories) => {
          this.availableCategories = categories;
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          // Fallback to empty array
          this.availableCategories = [];
        }
      });
  }

  // Load standardized transaction types from backend
  loadTransactionTypes() {
    this.http.get<string[]>(`${this.apiUrl}types`)
      .subscribe({
        next: (types) => {
          this.availableTransactionTypes = types;
        },
        error: (error) => {
          console.error('Error loading transaction types:', error);
          // Fallback to empty array
          this.availableTransactionTypes = [];
        }
      });
  }

  // Check if there are transactions in the current time frame
  hasTransactionsInTimeFrame(): boolean {
    const filteredTransactions = this.getFilteredTransactionsByTimeFrame();
    return filteredTransactions.filter(t => t.type === 'purchase' || t.type === 'expense').length > 0;
  }

  // Get current time frame description
  getTimeFrameDescription(): string {
    switch (this.timeFrame) {
      case 'week':
        return 'Last 7 days';
      case 'month':
        return 'Last 30 days';
      case 'year':
        return 'Last 365 days';
      default:
        return 'Unknown time frame';
    }
  }

  // Get current time frame date range for debugging
  getCurrentTimeFrameRange(): { start: string; end: string } {
    // Calculate date range for the time frame - ALWAYS relative to TODAY
    const today = new Date();
    today.setHours(23, 59, 59, 999); // End of today

    let startDate: Date;
    switch (this.timeFrame) {
      case 'week':
        // Last 7 days from today (including today)
        startDate = new Date(today.getTime() - (6 * 24 * 60 * 60 * 1000)); // 6 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      case 'month':
        // Last 30 days from today (including today)
        startDate = new Date(today.getTime() - (29 * 24 * 60 * 60 * 1000)); // 29 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      case 'year':
        // Last 365 days from today (including today)
        startDate = new Date(today.getTime() - (364 * 24 * 60 * 60 * 1000)); // 364 days ago
        startDate.setHours(0, 0, 0, 0); // Start of that day
        break;
      default:
        startDate = today;
        startDate.setHours(0, 0, 0, 0);
    }

    // Add detailed debugging for date calculations
    console.log(`\n=== DATE RANGE CALCULATION DEBUG ===`);
    console.log(`DEBUG: Today: ${today.toLocaleDateString()} (${today.toISOString()})`);
    console.log(`DEBUG: Time frame: ${this.timeFrame}`);
    console.log(`DEBUG: Start date calculated: ${startDate.toLocaleDateString()} (${startDate.toISOString()})`);
    console.log(`DEBUG: End date: ${today.toLocaleDateString()} (${today.toISOString()})`);

    // Verify the calculation makes sense
    const daysDiff = Math.round((today.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
    console.log(`DEBUG: Days difference: ${daysDiff} days`);

    if (this.timeFrame === 'week' && daysDiff !== 7) {
      console.warn(`WARNING: Week calculation shows ${daysDiff} days, expected 7`);
    } else if (this.timeFrame === 'month' && daysDiff !== 30) {
      console.warn(`WARNING: Month calculation shows ${daysDiff} days, expected 30`);
    } else if (this.timeFrame === 'year' && daysDiff !== 365) {
      console.warn(`WARNING: Year calculation shows ${daysDiff} days, expected 365`);
    }
    console.log(`=== END DATE RANGE CALCULATION DEBUG ===\n`);

    return {
      start: startDate.toISOString().split('T')[0],
      end: today.toISOString().split('T')[0]
    };
  }

  // Get sample data suggestions
  getSampleDataSuggestions(): string[] {
    switch (this.timeFrame) {
      case 'week':
        return [
          'Add a coffee purchase from today',
          'Record lunch expenses from this week',
          'Add grocery shopping from yesterday'
        ];
      case 'month':
        return [
          'Add utility bills from this month',
          'Record entertainment expenses',
          'Add transportation costs'
        ];
      case 'year':
        return [
          'Add annual subscriptions',
          'Record major purchases',
          'Add travel expenses'
        ];
      default:
        return [];
    }
  }

  // Get transaction date range for debugging
  getTransactionDateRange(): { earliest: string | null, latest: string | null } {
    if (this.allTransactions.length === 0) {
      return { earliest: null, latest: null };
    }

    const dates = this.allTransactions
      .map(t => new Date(t.date))
      .sort((a, b) => a.getTime() - b.getTime());

    return {
      earliest: dates[0].toISOString().split('T')[0],
      latest: dates[dates.length - 1].toISOString().split('T')[0]
    };
  }

  // Force refresh chart
  refreshChart() {
    console.log('DEBUG: Force refreshing chart');
    this.processChartData();

    if (this.categoryData.length > 0) {
      if (this.currentChart) {
        this.updateChart();
      } else {
        setTimeout(() => this.createChart(), 100);
      }
    } else {
      if (this.currentChart) {
        this.currentChart.destroy();
        this.currentChart = null;
      }
    }
  }

  // Check backend connectivity
  checkBackendConnectivity(): Promise<boolean> {
    return new Promise((resolve) => {
      const testUrl = `${this.apiUrl.replace('/transactions/', '')}/actuator/health`;
      this.http.get(testUrl, { observe: 'response' })
        .subscribe({
          next: (response) => {
            console.log('DEBUG: Backend connectivity check successful:', response.status);
            resolve(true);
          },
          error: (error) => {
            console.error('DEBUG: Backend connectivity check failed:', error);
            resolve(false);
          }
        });
    });
  }

  // Get data state summary for debugging
  getDataStateSummary(): string {
    const totalTransactions = this.allTransactions.length;
    const spendingTransactions = this.allTransactions.filter(t =>
      t.type === 'purchase' || t.type === 'expense'
    ).length;
    const dateRange = this.getTransactionDateRange();

    return `Total: ${totalTransactions}, Spending: ${spendingTransactions}, Date Range: ${dateRange.earliest || 'N/A'} to ${dateRange.latest || 'N/A'}`;
  }

  // Toggle category dropdown expansion
  toggleCategoryExpansion(category: string) {
    if (this.expandedCategories.has(category)) {
      this.expandedCategories.delete(category);
    } else {
      this.expandedCategories.add(category);
      this.resetDropdownPage(category);
    }
  }

  // Check if a category is expanded
  isCategoryExpanded(category: string): boolean {
    return this.expandedCategories.has(category);
  }

  // Get transactions for a specific category
  getCategoryTransactions(category: string): Transaction[] {
    const filteredTransactions = this.allTransactions.filter(transaction =>
      (transaction.type === 'purchase' || transaction.type === 'expense') &&
      (transaction.category || 'Uncategorized') === category
    );

    // Sort by date (most recent first) to ensure consistent ordering
    return filteredTransactions.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime(); // Descending order (newest first)
    });
  }

  // Get paginated transactions for dropdown with lazy loading
  getPaginatedCategoryTransactions(category: string): Transaction[] {
    // For first page, return cached transactions if available
    const currentPage = this.getDropdownPage(category);

    if (currentPage === 1) {
      // Return cached transactions for first page
      return this.getCachedCategoryTransactions(category);
    } else {
      // For subsequent pages, return transactions from allTransactions
      const allTransactions = this.getCategoryTransactions(category);
      const startIndex = (currentPage - 1) * this.dropdownPageSize;
      const endIndex = startIndex + this.dropdownPageSize;
      return allTransactions.slice(startIndex, endIndex);
    }
  }

  // Get cached transactions for first page (last 5 transactions)
  getCachedCategoryTransactions(category: string): Transaction[] {
    const allTransactions = this.getCategoryTransactions(category);
    // Return last 5 transactions for first page
    return allTransactions.slice(-this.dropdownPageSize);
  }

  // Get current page for a category dropdown
  getDropdownPage(category: string): number {
    return this.dropdownPages.get(category) || 1;
  }

  // Set page for a category dropdown
  setDropdownPage(category: string, page: number): void {
    this.dropdownPages.set(category, page);
  }

  // Get total pages for a category dropdown
  getDropdownTotalPages(category: string): number {
    const totalTransactions = this.getCategoryTransactions(category).length;
    // If we have more than 5 transactions, we need pagination
    if (totalTransactions > this.dropdownPageSize) {
      return Math.ceil(totalTransactions / this.dropdownPageSize);
    }
    return 1; // Only one page if 5 or fewer transactions
  }

  // Go to next page in dropdown
  nextDropdownPage(category: string): void {
    const currentPage = this.getDropdownPage(category);
    const totalPages = this.getDropdownTotalPages(category);
    if (currentPage < totalPages) {
      this.setDropdownPage(category, currentPage + 1);
      // Fetch more data if needed for this page
      this.ensureCategoryDataLoaded(category, currentPage + 1);
    }
  }

  // Go to previous page in dropdown
  previousDropdownPage(category: string): void {
    const currentPage = this.getDropdownPage(category);
    if (currentPage > 1) {
      this.setDropdownPage(category, currentPage - 1);
    }
  }

  // Reset dropdown page when expanding
  resetDropdownPage(category: string): void {
    this.setDropdownPage(category, 1);
  }

  // Get current page transactions count
  getCurrentPageTransactionsCount(category: string): number {
    return this.getPaginatedCategoryTransactions(category).length;
  }

  // Get total transactions count for a category
  getTotalTransactionsCount(category: string): number {
    return this.getCategoryTransactions(category).length;
  }

  // Ensure category data is loaded for pagination
  ensureCategoryDataLoaded(category: string, page: number): void {
    // Fetch more data if needed for this page
    this.fetchMoreTransactions(category, page);
  }

  // Fetch more transactions for pagination
  fetchMoreTransactions(category: string, page: number): void {
    // Calculate how many transactions we need
    const neededTransactions = page * this.dropdownPageSize;
    const currentTransactions = this.getCategoryTransactions(category).length;

    if (neededTransactions > currentTransactions) {
      // Fetch more transactions from backend
      const url = `${this.apiUrl}?userId=${this.userId}&category=${encodeURIComponent(category)}&limit=${neededTransactions}&sort=date,desc`;
      console.log(`Fetching more transactions for ${category} page ${page}`);

      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      };

      this.http.get<Transaction[]>(url, { headers })
        .subscribe({
          next: (transactions) => {
            console.log(`Fetched ${transactions.length} transactions for ${category}`);
            // Merge new transactions with existing ones
            this.mergeTransactions(transactions);
          },
          error: (error) => {
            console.error(`Error fetching more transactions for ${category}:`, error);
          }
        });
    }
  }

  // Merge new transactions with existing ones
  mergeTransactions(newTransactions: Transaction[]): void {
    // Create a map of existing transactions by ID to avoid duplicates
    const existingMap = new Map<number | undefined, Transaction>();
    this.allTransactions.forEach(t => {
      if (t.id !== undefined) {
        existingMap.set(t.id, t);
      }
    });

    // Add only new transactions
    newTransactions.forEach(transaction => {
      if (transaction.id && !existingMap.has(transaction.id)) {
        this.allTransactions.push(transaction);
      }
    });

    // Re-sort by date
    this.allTransactions.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime();
    });
  }

  // Debug method to be called from browser console
  debugCurrentState() {
    console.log(`\n=== MANUAL DEBUG STATE ===`);
    console.log(`Current time frame: ${this.timeFrame}`);
    console.log(`Current budget ID: ${this.selectedBudgetId}`);
    console.log(`Total transactions loaded: ${this.allTransactions.length}`);

    const timeFrameRange = this.getCurrentTimeFrameRange();
    console.log(`Time frame range: ${timeFrameRange.start} to ${timeFrameRange.end}`);

    const filteredTransactions = this.getFilteredTransactionsByTimeFrame();
    console.log(`Filtered transactions count: ${filteredTransactions.length}`);
    console.log(`Total spending amount: $${this.totalSpending.toFixed(2)}`);

    console.log(`\nAll transactions (first 10):`);
    this.allTransactions.slice(0, 10).forEach((t, i) => {
      console.log(`${i + 1}. ${t.date}: $${t.amount} | ${t.vendor} | ${t.category} | ${t.type}`);
    });

    if (this.allTransactions.length > 10) {
      console.log(`... and ${this.allTransactions.length - 10} more transactions`);
    }

    console.log(`\nFiltered transactions (first 10):`);
    filteredTransactions.slice(0, 10).forEach((t, i) => {
      console.log(`${i + 1}. ${t.date}: $${t.amount} | ${t.vendor} | ${t.category} | ${t.type}`);
    });

    if (filteredTransactions.length > 10) {
      console.log(`... and ${filteredTransactions.length - 10} more transactions`);
    }

    console.log(`=== END MANUAL DEBUG STATE ===\n`);
  }

  // Debug method to identify future transactions that need database cleanup
  identifyFutureTransactions() {
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    console.log(`\n🔍 IDENTIFYING FUTURE TRANSACTIONS FOR DATABASE CLEANUP ===`);
    console.log(`Current date: ${now.toISOString().split('T')[0]}`);

    const futureTransactions = this.allTransactions.filter(transaction => {
      const [year, month, day] = transaction.date.split('-').map(Number);
      const transactionDate = new Date(year, month - 1, day);
      transactionDate.setHours(0, 0, 0, 0);
      return transactionDate > now;
    });

    if (futureTransactions.length === 0) {
      console.log(`✅ No future transactions found. Database is clean!`);
    } else {
      console.log(`⚠️  Found ${futureTransactions.length} future transactions that need database cleanup:`);
      console.log(`These transactions have dates after ${now.toISOString().split('T')[0]} and should not exist in real data.`);

      futureTransactions.forEach((transaction, index) => {
        const [year, month, day] = transaction.date.split('-').map(Number);
        const transactionDate = new Date(year, month - 1, day);
        const daysInFuture = Math.ceil((transactionDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

        console.log(`\n${index + 1}. Future Transaction (${daysInFuture} days ahead):`);
        console.log(`   ID: ${transaction.id || 'No ID'}`);
        console.log(`   Date: ${transaction.date} (${daysInFuture} days in future)`);
        console.log(`   Amount: $${transaction.amount}`);
        console.log(`   Vendor: ${transaction.vendor}`);
        console.log(`   Category: ${transaction.category}`);
        console.log(`   Type: ${transaction.type}`);
        console.log(`   Notes: ${transaction.notes || 'No notes'}`);
        console.log(`   Action needed: Update date in database to valid past date`);
      });

      console.log(`\n📋 DATABASE CLEANUP REQUIRED:`);
      console.log(`1. These transactions have invalid future dates`);
      console.log(`2. They should be updated to have realistic past dates`);
      console.log(`3. Or deleted if they are test data`);
      console.log(`4. This will fix the weekly/monthly filtering issues`);
    }

    console.log(`=== END FUTURE TRANSACTIONS IDENTIFICATION ===\n`);
  }

  // Show current backend query parameters
  showCurrentBackendQuery() {
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    let startDate: Date;
    switch (this.timeFrame) {
      case 'week':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      case 'month':
        startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        break;
      case 'year':
        startDate = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000);
        break;
      default:
        startDate = now;
    }

    startDate.setHours(0, 0, 0, 0);

    const startDateStr = startDate.toISOString().split('T')[0];
    const endDateStr = now.toISOString().split('T')[0];

    console.log(`\n🔍 CURRENT BACKEND QUERY PARAMETERS ===`);
    console.log(`Time frame: ${this.timeFrame.toUpperCase()}`);
    console.log(`Start date: ${startDateStr}`);
    console.log(`End date: ${endDateStr}`);
    console.log(`User ID: ${this.userId}`);
    console.log(`\nBackend URL: ${this.apiUrl}?userId=${this.userId}&startDate=${startDateStr}&endDate=${endDateStr}&sort=date,desc`);
    console.log(`\nEquivalent SQL: SELECT * FROM transactions WHERE date::DATE >= '${startDateStr}' AND date::DATE <= '${endDateStr}' AND user_id = ${this.userId} ORDER BY date DESC`);
    console.log(`=== END BACKEND QUERY PARAMETERS ===\n`);
  }

}
