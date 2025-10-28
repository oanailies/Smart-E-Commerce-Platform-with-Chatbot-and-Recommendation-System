package order_service.application.service;

import order_service.domain.model.Coupon;
import order_service.domain.model.DiscountType;
import order_service.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon createCoupon(Coupon coupon) {
        coupon.setUsedCount(0);
        coupon.setActive(true);
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive");
        }
        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Coupon expired");
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new RuntimeException("Coupon usage limit reached");
        }

        return coupon;
    }

    public double applyDiscount(Coupon coupon, double total) {
        double discount;

        if (coupon.getType() == DiscountType.PERCENTAGE) {
            discount = total * (coupon.getValue() / 100);
        } else {
            discount = coupon.getValue();
        }

        double newTotal = total - discount;
        double minimumOrderValue = 10.0;

        if (newTotal < minimumOrderValue) {
            newTotal = minimumOrderValue;
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        return newTotal;
    }

    public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
        return couponRepository.findById(id)
                .map(existing -> {
                    existing.setCode(updatedCoupon.getCode());
                    existing.setType(updatedCoupon.getType());
                    existing.setValue(updatedCoupon.getValue());
                    existing.setStartDate(updatedCoupon.getStartDate());
                    existing.setEndDate(updatedCoupon.getEndDate());
                    existing.setMaxUses(updatedCoupon.getMaxUses());
                    existing.setPerUserLimit(updatedCoupon.getPerUserLimit());
                    existing.setActive(updatedCoupon.getActive());
                    return couponRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
    }
}
