package com.itzx.signin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sign_in_record")
public class SignInRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "sign_in_date", nullable = false)
    private LocalDate signInDate;

    @Column(name = "consecutive_days", nullable = false)
    private Integer consecutiveDays;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
