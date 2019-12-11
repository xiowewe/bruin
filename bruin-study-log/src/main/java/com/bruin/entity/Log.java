package com.bruin.entity;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 11:33
 */
@Data
public class Log {
    private Integer id;
    private String msg;
    private Date createTime;
}
