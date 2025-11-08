package in.omtalaviya.moneymanager.service;

import in.omtalaviya.moneymanager.dto.CategoryDTO;
import in.omtalaviya.moneymanager.entity.CategoryEntity;
import in.omtalaviya.moneymanager.entity.ProfileEntity;
import in.omtalaviya.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

//    helper method
    CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    CategoryDTO toDTO(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .profileId(categoryEntity.getProfile()!=null ?categoryEntity.getProfile().getId() : null)
                .build();
    }

//    save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();

        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with this name already exist");
        }

        CategoryEntity newCategory = toEntity(categoryDTO,profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

//    get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByProfileId(profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

//    get categories using type
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

//    update category
    public CategoryDTO updateCategory(Long categoryId,CategoryDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(()->new RuntimeException("Category non found or not accessible"));

        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());

        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }
}
