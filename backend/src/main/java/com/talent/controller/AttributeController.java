package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.TalentAttribute;
import com.talent.service.TalentAttributeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价指标管理接口
 * <p>
 * 提供指标的增删改查、分组查询、状态切换功能。
 * </p>
 *
 * @author talent-hr
 */
@RestController
@RequestMapping("/api/attribute")
public class AttributeController {

    private final TalentAttributeService service;

    /**
     * 构造方法
     *
     * @param service 指标服务
     */
    public AttributeController(TalentAttributeService service) {
        this.service = service;
    }

    /**
     * 查询所有启用的指标
     *
     * @return 指标列表
     */
    @GetMapping("/list")
    public R<List<TalentAttribute>> list() {
        return R.ok(service.listAll());
    }

    /**
     * 查询所有指标分组名称
     *
     * @return 分组名称列表
     */
    @GetMapping("/groups")
    public R<List<String>> groups() {
        List<String> gs = service.listAll().stream()
                .map(a -> a.getGroupName() == null || a.getGroupName().isBlank()
                        ? "未分组" : a.getGroupName())
                .distinct()
                .collect(Collectors.toList());
        return R.ok(gs);
    }

    /**
     * 按分组查询指标
     *
     * @param group 分组名称（为空则返回全部）
     * @return 指标列表
     */
    @GetMapping("/list-by-group")
    public R<List<TalentAttribute>> listByGroup(@RequestParam(required = false) String group) {
        List<TalentAttribute> all = service.listAll();
        if (group == null || group.isBlank()) {
            return R.ok(all);
        }
        String g = "未分组".equals(group) ? "" : group;
        List<TalentAttribute> filtered = all.stream()
                .filter(a -> {
                    String gn = a.getGroupName() == null ? "" : a.getGroupName();
                    return g.isEmpty() ? gn.isEmpty() : g.equals(gn);
                })
                .collect(Collectors.toList());
        return R.ok(filtered);
    }

    /**
     * 分页查询指标（含禁用的）
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页条数（默认 20）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") int pageNum,
                     @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.page(pageNum, pageSize));
    }

    /**
     * 根据 ID 查询指标
     *
     * @param id 指标 ID
     * @return 指标实体
     */
    @GetMapping("/{id}")
    public R<TalentAttribute> getById(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    /**
     * 新增指标
     *
     * @param attr 指标实体
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@RequestBody TalentAttribute attr) {
        service.save(attr);
        return R.ok();
    }

    /**
     * 更新指标
     *
     * @param attr 指标实体
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@RequestBody TalentAttribute attr) {
        service.save(attr);
        return R.ok();
    }

    /**
     * 删除指标（逻辑删除）
     *
     * @param id 指标 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    /**
     * 切换指标启用/禁用状态
     *
     * @param id 指标 ID
     * @return 操作结果
     */
    @PutMapping("/{id}/toggle")
    public R<?> toggle(@PathVariable Long id) {
        service.toggleStatus(id);
        return R.ok();
    }
}
