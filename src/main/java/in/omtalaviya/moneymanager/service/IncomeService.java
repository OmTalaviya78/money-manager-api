package in.omtalaviya.moneymanager.service;

import in.omtalaviya.moneymanager.dto.ExpenseDTO;
import in.omtalaviya.moneymanager.dto.IncomeDTO;
import in.omtalaviya.moneymanager.entity.CategoryEntity;
import in.omtalaviya.moneymanager.entity.ExpenseEntity;
import in.omtalaviya.moneymanager.entity.IncomeEntity;
import in.omtalaviya.moneymanager.entity.ProfileEntity;
import in.omtalaviya.moneymanager.repository.CategoryRepository;
import in.omtalaviya.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    //    helper method
    public IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .icon(incomeDTO.getIcon())
                .name(incomeDTO.getName())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    public IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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

    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()->new RuntimeException("Category not found"));
        IncomeEntity entity = toEntity(dto,profile,category);
        entity = incomeRepository.save(entity);
        return toDTO(entity);
    }

    //    Retrieves all expenses for the current month/bases on start date and end date
    public List<IncomeDTO> getCurrentMonthExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate,endDate);
        return list.stream().map(this::toDTO).toList();
    }
    //    delete expense by id for current user
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(()->new RuntimeException("income not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(entity);
    }

    //    Get latest 5 incomes for current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    //    Get total incomes of current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return total!=null? total:BigDecimal.ZERO;
    }

    //    filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate,
                                           String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list =
                incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate,endDate,keyword,sort
                );
        return list.stream().map(this::toDTO).toList();
    }
}
