package com.bruin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 11:32
 */
@Mapper
public interface LogMapper {

    @Select("select msg from log where id = #{id}")
    String selectLogMsg(@Param("id") Integer id);
}
