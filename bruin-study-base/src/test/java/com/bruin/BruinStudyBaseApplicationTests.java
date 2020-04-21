package com.bruin;

import com.bruin.dependecy.loockup.DependecyLookupDemo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BruinStudyBaseApplicationTests {

    @Autowired
    DependecyLookupDemo dependecyLookupDemo;


    @Test
    void contextLoads() {
        dependecyLookupDemo.test();
    }

}
