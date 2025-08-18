import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { BudgetPlannerComponent } from './budget-planner/budget-planner.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'budget-planner', component: BudgetPlannerComponent }
];
