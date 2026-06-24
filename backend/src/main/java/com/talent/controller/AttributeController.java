package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.TalentAttribute;
import com.talent.service.TalentAttributeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attribute")
public class AttributeController {

    private final TalentAttributeService service;

    public AttributeController(TalentAttributeService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public R<List<TalentAttribute>> list() {
        return R.ok(service.listAll());
    }

    @GetMapping("/groups")
    public R<List<String>> groups() {
        List<String> gs = service.listAll().stream()
                .map(a -> a.getGroupName() == null || a.getGroupName().isBlank() ? "未分组" : a.getGroupName())
                .distinct()
                .collect(Collectors.toList());
        return R.ok(gs);
    }

    @GetMapping("/list-by-group")
    public R<List<TalentAttribute>> listByGroup(@RequestParam(required = false) String group) {
        List<TalentAttribute> all = service.listAll();
        if (group == null || group.isBlank()) return R.ok(all);
        String g = "未分组".equals(group) ? "" : group;
        List<TalentAttribute> filtered = all.stream()
                .filter(a -> {
                    String gn = a.getGroupName() == null ? "" : a.getGroupName();
                    return g.isEmpty() ? gn.isEmpty() : g.equals(gn);
                })
                .collect(Collectors.toList());
        return R.ok(filtered);
    }

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") int pageNum,
                     @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.page(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public R<TalentAttribute> getById(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @PostMapping
    public R<?> save(@RequestBody TalentAttribute attr) {
        service.save(attr);
        return R.ok();
    }

    @PutMapping
    public R<?> update(@RequestBody TalentAttribute attr) {
        service.save(attr);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    public R<?> toggle(@PathVariable Long id) {
        service.toggleStatus(id);
        return R.ok();
    }
}
