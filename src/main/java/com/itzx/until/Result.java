package com.itzx.until;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Result {
    private Integer code; // 200成功，401未授权，500异常
    private String msg;
    private Object data;

    public static Result success(Object data) {
        return new Result(200, "成功", data);
    }

    public static Result error(String msg) {
        return new Result(500, msg, null);
    }

    public static Result unAuth(String msg) {
        return new Result(401, msg, null);
    }
}