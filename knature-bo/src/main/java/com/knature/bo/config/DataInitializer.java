package com.knature.bo.config;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.admin.AdminRole;
import com.knature.common.domain.coupon.Coupon;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberGrade;
import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderItem;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.order.PaymentMethod;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductCategory;
import com.knature.common.domain.product.ProductStatus;
import com.knature.common.repository.AdminRepository;
import com.knature.common.repository.CouponRepository;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.OrderRepository;
import com.knature.common.repository.ProductCategoryRepository;
import com.knature.common.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final ProductCategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategories();
        seedMembers();
        seedProducts();
        seedOrders();
        seedWelcomeCoupon();
    }

    /** 가입 자동발급용 WELCOME 쿠폰 (20% 할인, 상한 3만원) — FO 가입 시 issueByCode("WELCOME") */
    private void seedWelcomeCoupon() {
        if (couponRepository.findByCode("WELCOME").isPresent()) return;
        couponRepository.save(Coupon.builder()
                .code("WELCOME")
                .name("신규가입 20% 할인쿠폰")
                .discountType(Coupon.DiscountType.PERCENT)
                .amount(20L)
                .maxDiscount(30_000L)
                .minOrderAmount(0L)
                .build());
        log.info("WELCOME 쿠폰 생성 완료");
    }

    private void seedAdmin() {
        if (adminRepository.count() > 0) return;
        adminRepository.save(Admin.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1004"))
                .name("최고관리자")
                .role(AdminRole.SUPER_ADMIN)
                .build());
        log.info("기본 관리자 계정 생성: admin / admin1004");
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;
        categoryRepository.save(ProductCategory.builder().name("HYDRA CALMING LINE").slug("hydra-calming-line").sortOrder(1).build());
        categoryRepository.save(ProductCategory.builder().name("VOLUME LINE").slug("volume-line").sortOrder(2).build());
        categoryRepository.save(ProductCategory.builder().name("AMPOULE LINE").slug("ampoule-line").sortOrder(3).build());
        categoryRepository.save(ProductCategory.builder().name("CREAM LINE").slug("cream-line").sortOrder(4).build());
        log.info("기본 상품 카테고리 4개 생성 완료");
    }

    private void seedMembers() {
        if (memberRepository.count() > 0) return;
        saveMember("hong", "홍길동", "hong@test.com", "010-1111-2222", MemberGrade.GOLD, 1_800_000L);
        saveMember("kim", "김영희", "kim@test.com", "010-2222-3333", MemberGrade.SILVER, 720_000L);
        saveMember("lee", "이철수", "lee@test.com", "010-3333-4444", MemberGrade.RUBY, 85_000L);
        saveMember("park", "박민지", "park@test.com", "010-4444-5555", MemberGrade.NEW, 0L);
        saveMember("choi", "최지우", "choi@test.com", "010-5555-6666", MemberGrade.DIAMOND, 3_200_000L);
        log.info("샘플 회원 5명 생성 완료");
    }

    private void saveMember(String username, String name, String email, String phone,
                            MemberGrade grade, long totalPurchase) {
        Member m = Member.builder()
                .username(username)
                .password(passwordEncoder.encode("test1234"))
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        m.setGrade(grade);
        m.setTotalPurchaseAmount(totalPurchase);
        memberRepository.save(m);
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;
        List<ProductCategory> cats = categoryRepository.findAll();
        ProductCategory hydra = cats.get(0), volume = cats.get(1), ampoule = cats.get(2), cream = cats.get(3);

        saveProduct(hydra, "수분진정 토너 300ml", "KN-HC-001", 32_000L, 27_200L, "민감 피부용 저자극 토너", null);
        saveProduct(hydra, "수분진정 크림 50ml", "KN-HC-002", 38_000L, null, "장벽 강화 진정 크림", null);
        saveProduct(volume, "볼륨 에센스 50ml", "KN-VL-001", 45_000L, 39_000L, "탄력 개선 에센스", null);
        saveProduct(ampoule, "집중 앰플 30ml", "KN-AM-001", 52_000L, 46_800L, "고농축 집중 케어 앰플", null);
        Product soldOut = saveProduct(cream, "리페어 나이트 크림 60ml", "KN-CR-001", 41_000L, null, "밤사이 집중 리페어", null);
        soldOut.setStatus(ProductStatus.SOLD_OUT);
        productRepository.save(soldOut);
        saveProduct(volume, "볼륨 마스크팩 10매", "KN-VL-002", 28_000L, 22_400L, "탄력 시트 마스크", null);
        log.info("샘플 상품 6개 생성 완료");
    }

    private Product saveProduct(ProductCategory cat, String name, String code, Long price,
                                Long salePrice, String desc, String detail) {
        return productRepository.save(Product.builder()
                .category(cat).name(name).code(code)
                .price(price).salePrice(salePrice)
                .description(desc).detailContent(detail)
                .build());
    }

    private void seedOrders() {
        if (orderRepository.count() > 0) return;
        List<Member> members = memberRepository.findAll();
        List<Product> products = productRepository.findAll();
        Member hong = members.get(0), kim = members.get(1), lee = members.get(2), park = members.get(3), choi = members.get(4);

        int seq = 1;
        orderRepository.save(buildOrder(nextNo(seq++), hong, products.get(0), 2, PaymentMethod.CREDIT_CARD, OrderStatus.PREPARING));
        orderRepository.save(buildOrder(nextNo(seq++), kim, products.get(2), 1, PaymentMethod.KAKAO_PAY, OrderStatus.SHIPPING));
        orderRepository.save(buildOrder(nextNo(seq++), lee, products.get(3), 1, PaymentMethod.BANK_TRANSFER, OrderStatus.PENDING_PAYMENT));
        orderRepository.save(buildOrder(nextNo(seq++), choi, products.get(1), 3, PaymentMethod.NAVER_PAY, OrderStatus.DELIVERED));
        orderRepository.save(buildOrder(nextNo(seq++), park, products.get(5), 2, PaymentMethod.CREDIT_CARD, OrderStatus.CANCEL_REQUESTED));
        orderRepository.save(buildOrder(nextNo(seq++), hong, products.get(0), 1, PaymentMethod.CREDIT_CARD, OrderStatus.EXCHANGE_REQUESTED));
        orderRepository.save(buildOrder(nextNo(seq++), kim, products.get(2), 2, PaymentMethod.KAKAO_PAY, OrderStatus.RETURN_REQUESTED));
        log.info("샘플 주문 7건 생성 완료");
    }

    private String nextNo(int seq) {
        return String.format("ORD-20260705-%03d", seq);
    }

    private Order buildOrder(String no, Member m, Product p, int qty, PaymentMethod pm, OrderStatus status) {
        long unit = p.getDisplayPrice();
        long itemTotal = unit * qty;
        long fee = itemTotal >= 50_000 ? 0L : 3_000L;
        Order o = Order.builder()
                .orderNumber(no)
                .member(m)
                .ordererName(m.getName()).ordererEmail(m.getEmail()).ordererPhone(m.getPhone())
                .receiverName(m.getName()).receiverPhone(m.getPhone())
                .receiverZipcode("06236").receiverAddress("서울시 강남구 테헤란로 123").receiverAddressDetail("4층")
                .deliveryMemo("문 앞에 놓아주세요")
                .totalAmount(itemTotal).deliveryFee(fee).paymentAmount(itemTotal + fee)
                .paymentMethod(pm)
                .build();
        o.setStatus(status);
        OrderItem item = OrderItem.builder()
                .order(o).product(p)
                .productName(p.getName())
                .quantity(qty).price(unit)
                .build();
        o.getItems().add(item);
        return o;
    }
}
