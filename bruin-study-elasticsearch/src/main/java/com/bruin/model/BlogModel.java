package com.bruin.model;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: xiongwenwen   2019/11/29 11:18
 */
@Data
public class BlogModel {
    private Integer id;
    private String name;
    private Date date;
}
