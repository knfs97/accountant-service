package account.repository;

import account.entity.Payment;
import account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByUserOrderByIdDesc(User user);
    List<Payment> findByUserAndPeriod(User user, String period);
}
