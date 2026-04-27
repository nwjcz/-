package com.itzx.chat.mapper;

import com.itzx.chat.entity.UserChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserChatSessionMapper {

    UserChatSession findByBuyerUsername(@Param("buyerUsername") String buyerUsername);

    List<UserChatSession> findAllSessions();

    int insert(UserChatSession session);

    int updateOnBuyerMessage(@Param("buyerUsername") String buyerUsername,
                             @Param("content") String content,
                             @Param("msgTime") String msgTime);

    int updateOnMerchantMessage(@Param("buyerUsername") String buyerUsername,
                                @Param("content") String content,
                                @Param("msgTime") String msgTime);

    int resetUnread(@Param("buyerUsername") String buyerUsername);
}
