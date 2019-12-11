package com.bruin.controller;

import com.bruin.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:54
 */
@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public void log(Integer id){

        logService.getLog(id);
    }
}
