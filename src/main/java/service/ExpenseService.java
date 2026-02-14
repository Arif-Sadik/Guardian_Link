package service;

import model.entity.Expense;
import repository.ExpenseRepository;

import java.util.List;

/**
 * Business logic for expense management.
 */
public class ExpenseService {

    private final ExpenseRepository repository = new ExpenseRepository();

    public List<Expense> getByChildId(int childId) {
        return repository.findByChildId(childId);
    }

    public double getTotalByChildId(int childId) {
        return repository.getTotalByChildId(childId);
    }

    public boolean save(Expense expense) {
        return repository.save(expense);
    }
}
