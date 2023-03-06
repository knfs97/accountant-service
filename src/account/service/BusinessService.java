package account.service;

import account.entity.Payment;
import account.entity.SecurityEvent;
import account.entity.User;
import account.repository.PaymentRepository;
import account.repository.SecurityEventRepository;
import account.repository.UserRepository;
import account.response.SuccessResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityEventRepository securityEventRepository;
    public SuccessResponse uploadsPayrolls(List<Payment> payrolls) {

        String notFoundUsers = filterNonExistingUsers(payrolls);
        if (notFoundUsers.length() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User(s) : <" + notFoundUsers + "> not found");
        }
        String wrongPeriods = checkWrongPeriods(payrolls);
        if (wrongPeriods.length() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Following duplicated periods were found : <" + wrongPeriods + ">");
        }

        savePayrolls(payrolls);

        return SuccessResponse.builder()
                .status("Added successfully!")
                .build();
    }
    @Transactional
    public void savePayrolls(List<Payment> payrolls) {
        payrolls.forEach(payment -> {
            String employeeEmail = payment.getEmail();
            Optional<User> user = userRepository.findByEmail(employeeEmail);
            user.ifPresent(payment::setUser);
        });
        paymentRepository.saveAll(payrolls);
    }
    public SuccessResponse changeSalary(Payment payment) {
        Optional<User> employeeOpt = userRepository.findByEmail(payment.getEmail());
        if (employeeOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        User employee = employeeOpt.get();
        List<Payment> payments = paymentRepository.findByUserAndPeriod(employee, payment.getPeriod());
        if (payments.size() == 0) {
            payment.setUser(employee);
            paymentRepository.save(payment);
        } else {
            Payment oldPayment = payments.get(0);
            oldPayment.setSalary(payment.getSalary());
            oldPayment.setEmail(payment.getEmail());
            paymentRepository.save(oldPayment);
        }
        return SuccessResponse.builder()
                .status("Updated successfully!")
                .build();
    }
    public List<SuccessResponse> getPayroll(User user, String period) {

        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        List<Payment> payrolls;

        if (period == null) {
            payrolls = paymentRepository.findAllByUserOrderByIdDesc(user);
        } else {
            payrolls = paymentRepository.findByUserAndPeriod(user, period);
        }
        return formatPaymentsList(payrolls, user);
    }
    public List<SuccessResponse> formatPaymentsList(List<Payment> payrolls, User user) {

        List<SuccessResponse> payrollsFormatted = new ArrayList<>();

        if (payrolls.size() == 0) return payrollsFormatted;
        for (Payment payment : payrolls) {
            payrollsFormatted.add(SuccessResponse.builder()
                    .name(user.getName())
                    .lastname(user.getLastname())
                    .period(formatDate(payment.getPeriod()))
                    .salary(formatSalary(payment.getSalary()))
                    .build());
        }
        return payrollsFormatted;
    }
    public String filterNonExistingUsers(List<Payment> payrolls) {
        return payrolls.stream()
                .map(Payment::getEmail)
                .collect(Collectors.toSet())
                .stream().filter(email -> userRepository.findByEmail(email).isEmpty())
                .collect(Collectors.joining(", "));
    }
    public String formatSalary(long salary) {
        return String.format("%d dollar(s) %d cent(s)", salary / 100, salary % 100);
    }
    public String formatDate(String period) {
        String[] parts = period.split("-");

        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        return new SimpleDateFormat("MMMM-yyyy").format(cal.getTime());
    }
    public String checkWrongPeriods(List<Payment> payrolls) {
        Map<String, List<String>> emailsPeriods = new HashMap<>();

        // each key: email, value: ArrayList
        payrolls.forEach(payment ->
                emailsPeriods.putIfAbsent(
                        payment.getEmail(), new ArrayList<>()
                )
        );

        // add each period into corresponding array list with email as key
        payrolls.forEach(payment ->
                emailsPeriods
                        .get(payment.getEmail()) // arraylist
                        .add(payment.getPeriod())); // add period

        emailsPeriods.forEach((key, value) -> {
            List<String> duplicatesPeriods = value.stream()
                    .filter(i -> Collections.frequency(value, i) > 1)
                    .distinct()
                    .toList();
            emailsPeriods.put(key, duplicatesPeriods);
        });

        StringBuilder duplicates = new StringBuilder();
        emailsPeriods.forEach((email, listOfPeriod) -> {
            if (listOfPeriod.size() != 0) {
                duplicates.append(email)
                        .append(": <")
                        .append(String.join(" ,", listOfPeriod))
                        .append("> ");
            }
        });
        return duplicates.toString();
    }
    public List<SecurityEvent> getSecurityEvents() {
        return securityEventRepository.findAll();
    }
}
