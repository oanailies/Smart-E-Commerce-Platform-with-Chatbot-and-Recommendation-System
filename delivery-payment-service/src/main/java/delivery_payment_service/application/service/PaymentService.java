package delivery_payment_service.application.service;

import delivery_payment_service.domain.model.Payment;
import delivery_payment_service.domain.model.PaymentStatus;
import delivery_payment_service.domain.repository.PaymentRepository;
import delivery_payment_service.infrastructure.client.OrderClient;
import delivery_payment_service.infrastructure.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserClient userClient;
    private final OrderClient orderClient;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserClient userClient, OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.userClient = userClient;
        this.orderClient = orderClient;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public Payment createPayment(Payment payment, String token) {
        boolean orderExists = orderClient.checkOrderExists(payment.getOrderId(), token);
        if (!orderExists) {
            throw new RuntimeException("Order does not exist!");
        }
        boolean clientExists = userClient.checkClientExists(payment.getClientId(), token);
        if (!clientExists) {
            throw new RuntimeException("Client does not exist or token is invalid!");
        }
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    public Payment updatePayment(Long id, Payment updatedPayment) {
        Optional<Payment> existingOpt = paymentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Payment not found!");
        }
        Payment existing = existingOpt.get();
        existing.setStatus(updatedPayment.getStatus());
        existing.setAmount(updatedPayment.getAmount());
        return paymentRepository.save(existing);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }
}
