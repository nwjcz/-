package com.itzx.signin.mapper;

import com.itzx.signin.entity.SignInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface SignInMapper {
    SignInRecord findByUserIdAndDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    SignInRecord findLatestByUserId(@Param("userId") Integer userId);

    int insert(SignInRecord record);
}
