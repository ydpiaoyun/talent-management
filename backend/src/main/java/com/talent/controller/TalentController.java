package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.Talent;
import com.talent.service.TalentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/talent")
public class TalentController {

    private final TalentService service;

    public TalentController(TalentService service) {
        this.service = service;
    }

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") int pageNum,
                     @RequestParam(defaultValue = "10") int pageSize,
                     @RequestParam(required = false) String keyword) {
        return R.ok(service.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/list-with-attrs")
    public R<List<Map<String, Object>>> listWithAttrs() {
        return R.ok(service.listWithAttrs());
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        return R.ok(service.getDetail(id));
    }

    @PostMapping
    public R<?> save(@RequestBody Map<String, Object> body) {
        Talent talent = parseTalent(body);
        @SuppressWarnings("unchecked")
        Map<String, Object> attrMap = (Map<String, Object>) body.get("attrValues");
        Map<Long, String> attrValues = null;
        if (attrMap != null) {
            attrValues = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, Object> e : attrMap.entrySet()) {
                attrValues.put(Long.valueOf(e.getKey()), e.getValue() != null ? e.getValue().toString() : "");
            }
        }
        service.save(talent, attrValues);
        return R.ok();
    }

    @PutMapping
    public R<?> update(@RequestBody Map<String, Object> body) {
        return save(body);
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    private Talent parseTalent(Map<String, Object> body) {
        Talent t = new Talent();
        if (body.get("id") != null) t.setId(Long.valueOf(body.get("id").toString()));
        t.setName((String) body.get("name"));
        t.setGender((String) body.get("gender"));
        if (body.get("birthDate") != null) t.setBirthDate(java.time.LocalDate.parse(body.get("birthDate").toString()));
        t.setDept((String) body.get("dept"));
        t.setPosition((String) body.get("position"));
        t.setEmail((String) body.get("email"));
        t.setPhone((String) body.get("phone"));
        t.setRemark((String) body.get("remark"));
        if (body.get("status") != null) t.setStatus(Integer.valueOf(body.get("status").toString()));
        return t;
    }
}
