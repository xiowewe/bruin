package ${package.Controller};

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import com.bruin.starter.core.model.vo.BasePageVO;
import com.bruin.starter.core.result.BaseResult;

/**
 * <p>
 * ${table.comment!} 前端控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@RestController
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
public class ${table.controllerName} {

    private final ${table.serviceName} ${table.serviceName?uncap_first};

    public ${table.controllerName}(${table.serviceName} ${table.serviceName?uncap_first}) {
        this.${table.serviceName?uncap_first} = ${table.serviceName?uncap_first};
    }

    @GetMapping("/{id}")
    public BaseResult<${entity}> detail(@PathVariable("id") Integer id) {
        return BaseResult.success(${table.serviceName?uncap_first}.getById(id));
    }

    @GetMapping
    public BaseResult<List<${entity}>> list() {
        return BaseResult.success(${table.serviceName?uncap_first}.list());
    }

    @GetMapping("/page")
    public BaseResult<BasePageVO<${entity}>> page(
        @RequestParam(value = "page", defaultValue = "1", required = false) Long page,
        @RequestParam(value = "size", defaultValue = "10", required = false) Long size) {
        return BaseResult.success(${table.serviceName?uncap_first}.page(new Page<>(page, size)));
    }

    @PostMapping("/save")
    public BaseResult<?>  save(@Valid @RequestBody ${entity} ${entity?uncap_first}) {
        ${table.serviceName?uncap_first}.save(${entity?uncap_first});
        return BaseResult.success();
    }

    @PostMapping("/update")
    public BaseResult<?> update(@Valid @RequestBody ${entity} ${entity?uncap_first}) {
        ${table.serviceName?uncap_first}.updateById(${entity?uncap_first});
        return BaseResult.success();
    }

}
