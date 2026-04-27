package com.itzx.chat.mapper;

import com.itzx.chat.entity.UserChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserChatMessageMapper {

    int insert(UserChatMessage message);

    List<UserChatMessage> findByBuyerUsername(@Param("buyerUsername") String buyerUsername);
}
