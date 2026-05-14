package com.itzx.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_coupon")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "use_time")
    private LocalDateTime useTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;
}
