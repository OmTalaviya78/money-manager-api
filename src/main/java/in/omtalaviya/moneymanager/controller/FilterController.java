package in.omtalaviya.moneymanager.controller;

import in.omtalaviya.moneymanager.dto.ExpenseDTO;
import in.omtalaviya.moneymanager.dto.FilterDTO;
import in.omtalaviya.moneymanager.dto.IncomeDTO;
import in.omtalaviya.moneymanager.service.ExpenseService;
import in.omtalaviya.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() :
                LocalDate.MIN;
        LocalDate lastDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() :
                LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction,sortField);

        if ("income".equalsIgnoreCase(filterDTO.getType())) {
            List<IncomeDTO> incomes = incomeService.filterIncomes(startDate,lastDate,keyword,sort);
            return ResponseEntity.ok(incomes);
        } else if ("expense".equalsIgnoreCase(filterDTO.getType())) {
            List<ExpenseDTO> expense = expenseService.filterExpenses(startDate,lastDate,keyword,sort);
            return ResponseEntity.ok(expense);
        }
        else {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'");
        }
    }
}
