package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.Talent;
import com.talent.service.TalentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 人才管理接口
 * <p>
 * 提供人才的增删改查及分页查询功能。
 * </p>
 *
 * @author talent-hr
 */
@RestController
@RequestMapping("/api/talent")
public class TalentController {

    private final TalentService service;

    /**
     * 构造方法
     *
     * @param service 人才服务
     */
    public TalentController(TalentService service) {
        this.service = service;
    }

    /**
     * 分页查询人才列表
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页条数（默认 10）
     * @param keyword  搜索关键词（姓名或部门）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") int pageNum,
                     @RequestParam(defaultValue = "10") int pageSize,
                     @RequestParam(required = false) String keyword) {
        return R.ok(service.page(pageNum, pageSize, keyword));
    }

    /**
     * 查询所有人才列表（含指标值）
     *
     * @return 人才列表
     */
    @GetMapping("/list-with-attrs")
    public R<List<Map<String, Object>>> listWithAttrs() {
        return R.ok(service.listWithAttrs());
    }

    /**
     * 根据 ID 查询人才详情（含指标值）
     *
     * @param id 人才 ID
     * @return 人才详情
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        return R.ok(service.getDetail(id));
    }

    /**
     * 新增人才
     *
     * @param body 人才信息（含 attrValues 指标值）
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@RequestBody Map<String, Object> body) {
        Talent talent = parseTalent(body);
        @SuppressWarnings("unchecked")
        Map<String, Object> attrMap = (Map<String, Object>) body.get("attrValues");
        Map<Long, String> attrValues = null;
        if (attrMap != null) {
            attrValues = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, Object> e : attrMap.entrySet()) {
                attrValues.put(Long.valueOf(e.getKey()),
                        e.getValue() != null ? e.getValue().toString() : "");
            }
        }
        service.save(talent, attrValues);
        return R.ok();
    }

    /**
     * 更新人才信息
     *
     * @param body 人才信息（含 attrValues 指标值）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@RequestBody Map<String, Object> body) {
        return save(body);
    }

    /**
     * 删除人才（逻辑删除）
     *
     * @param id 人才 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    /**
     * 从请求体解析人才实体
     *
     * @param body 请求体
     * @return 人才实体
     */
    private Talent parseTalent(Map<String, Object> body) {
        Talent t = new Talent();
        if (body.get("id") != null) {
            t.setId(Long.valueOf(body.get("id").toString()));
        }
        t.setName((String) body.get("name"));
        t.setGender((String) body.get("gender"));
        if (body.get("birthDate") != null) {
            t.setBirthDate(java.time.LocalDate.parse(body.get("birthDate").toString()));
        }
        t.setDept((String) body.get("dept"));
        t.setPosition((String) body.get("position"));
        t.setEmail((String) body.get("email"));
        t.setPhone((String) body.get("phone"));
        t.setRemark((String) body.get("remark"));
        if (body.get("status") != null) {
            t.setStatus(Integer.valueOf(body.get("status").toString()));
        }
        return t;
    }
}
