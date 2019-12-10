package com.bruin.common;

import lombok.Data;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:33
 */
@Data
public class ThreadHolder {
    private String runningAccountId;

    private Integer runningAccountFlag;

    private Integer requestLogFlag;

    private String remark;

    private String ip;
}
