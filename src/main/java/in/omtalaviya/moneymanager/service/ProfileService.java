package in.omtalaviya.moneymanager.service;

import in.omtalaviya.moneymanager.config.SecurityConfig;
import in.omtalaviya.moneymanager.dto.AuthDTO;
import in.omtalaviya.moneymanager.dto.ProfileDTO;
import in.omtalaviya.moneymanager.entity.ProfileEntity;
import in.omtalaviya.moneymanager.repository.ProfileRepository;
import in.omtalaviya.moneymanager.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        //        send activation email
        String activationLink = activationURL+"/api/v1.0/activate?token="+ newProfile.getActivationToken();
        String subject = "Activate Your Money Manager Account";
        String body = "Click on the following link to activate your account"+activationLink;
        emailService.sendEmail(newProfile.getEmail(),subject,body);
        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .email(profileDTO.getEmail())
                .fullName(profileDTO.getFullName())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .updatedAt(profileDTO.getUpdatedAt())
                .createdAt(profileDTO.getCreatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .email(profileEntity.getEmail())
                .fullName(profileEntity.getFullName())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .updatedAt(profileEntity.getUpdatedAt())
                .createdAt(profileEntity.getCreatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                  profile.setIsActive(true);
                  profileRepository.save(profile);
                  return true;
                }).orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return profileRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Profile not found with email: "+email));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email==null) {
            currentUser = getCurrentProfile();
        }
        else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("Profile not found with email: "+email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
           /**
             you’re building an unauthenticated token that contains:
            Principal → email (username)
            Credentials → password
            This token is then sent to the AuthenticationManager for verification.
            **/
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(),authDTO.getPassword()));

            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token",token,
                    "user",getPublicProfile(authDTO.getEmail())
            );
        }

        catch (Exception e) {
            throw new RuntimeException("Invalid Username and Password");
        }
    }
}
