package com.bruin.service.impl;

import com.bruin.mapper.LogMapper;
import com.bruin.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:57
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public String getLog(Integer id) {

        return logMapper.selectLogMsg(id);
    }
}
