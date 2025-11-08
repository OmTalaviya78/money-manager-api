package in.omtalaviya.moneymanager.service;

import in.omtalaviya.moneymanager.dto.ExpenseDTO;
import in.omtalaviya.moneymanager.entity.ProfileEntity;
import in.omtalaviya.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *",zone = "IST")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            String body =
                    "<div style='font-family: Arial, sans-serif; background:#f4f6f8; padding:20px; border-radius:10px;'>"
                            + "<h3 style='color:#333;'>Hi " + profile.getFullName() + ", 👋</h3>"
                            + "<p style='color:#555;'>Hope you're doing well! Click below to open your dashboard:</p>"
                            + "<a href='" + frontendUrl + "' style='background:#007bff; color:#fff; padding:10px 20px; text-decoration:none; border-radius:5px;'>Go to Dashboard</a>"
                            + "<p style='color:#777; margin-top:20px;'>Best Regards,<br><b>Your Team</b></p>"
                            + "</div>";

            emailService.sendEmail(profile.getEmail(),"Daily Reminder: Add your income and expenses",body);
        }
    }

    @Scheduled(cron = "0 0 23 * * *",zone = "IST")
    public void sendDailyExpenseSummary() {
        log.info("Job started");

        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getExpensesForUserOnDate(profile.getId(),
                    LocalDate.now(ZoneId.of("Asia/Kolkata")));

            if (!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2;'><th style='border:1px solid #ddd;padding:8px;'>Category</th></tr>");

                int i=1;
                for (ExpenseDTO expenseDTO : todayExpenses) {
                    table.append("<tr>");
                    table.append("<td style='border:1px solid #ddd; padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style='border:1px solid #ddd; padding:8px;'>").append(expenseDTO.getCategoryId()).append("</td>");
                    table.append("<td style='border:1px solid #ddd; padding:8px;'>").append(expenseDTO.getAmount()).append("</td>");
                    table.append("<td style='border:1px solid #ddd; padding:8px;'>").
                            append(expenseDTO.getCategoryId()!=null ? expenseDTO.getCategoryName():"N/A").append("</td>");
                    table.append("</tr>");
                }
                table.append("/table");
                String body = "Hi "+profile.getFullName()+",<br/><br/> Here is a summary of your expenses for today:" +
                        "</br></br>"+table+"</br></br>Best Regards,</br>Money Manager Team";
                emailService.sendEmail(profile.getEmail(),"Your daily Expenses summary",body);
            }
        }
    }
}
