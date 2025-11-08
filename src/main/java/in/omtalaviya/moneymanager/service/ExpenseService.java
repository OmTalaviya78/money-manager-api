package in.omtalaviya.moneymanager.service;

import in.omtalaviya.moneymanager.dto.ExpenseDTO;
import in.omtalaviya.moneymanager.entity.CategoryEntity;
import in.omtalaviya.moneymanager.entity.ExpenseEntity;
import in.omtalaviya.moneymanager.entity.ProfileEntity;
import in.omtalaviya.moneymanager.repository.CategoryRepository;
import in.omtalaviya.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.type.ListType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

//    helper method
    public ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .icon(expenseDTO.getIcon())
                .name(expenseDTO.getName())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    public ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .categoryName(entity.getCategory()!=null?entity.getCategory().getName():"N/A")
                .categoryId(entity.getCategory()!=null ? entity.getCategory().getId():null)
                .name(entity.getName())
                .date(entity.getDate())
                .createdAt(entity.getCreateAt())
                .updatedAt(entity.getUpdateAt())
                .icon(entity.getIcon())
                .build();
    }

//    add Expense
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()->new RuntimeException("Category not found"));
        ExpenseEntity entity = toEntity(dto,profile,category);
        entity = expenseRepository.save(entity);
        return toDTO(entity);
    }

//    Retrieves all expenses for the current month/bases on start date and end date
    public List<ExpenseDTO> getCurrentMonthExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(),
                startDate,endDate);
        return list.stream().map(this::toDTO).toList();
    }

//    delete expense by id for current user
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(()->new RuntimeException("Expense not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(entity);
    }

//    Get latest 5 expenses for current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

//    Get total expenses of current user
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total!=null? total:BigDecimal.ZERO;
    }

//    filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate,
                                           String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list =
                expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate,endDate,keyword,sort
                );
        return list.stream().map(this::toDTO).toList();
    }

//    Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId,LocalDate date) {
        List<ExpenseEntity> entity = expenseRepository.findByProfileIdAndDate(profileId,date);
        return entity.stream().map(this::toDTO).toList();
    }



}
