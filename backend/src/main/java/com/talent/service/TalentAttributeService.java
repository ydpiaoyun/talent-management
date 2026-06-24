package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.talent.entity.TalentAttribute;
import com.talent.mapper.TalentAttributeMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TalentAttributeService {

    private final TalentAttributeMapper mapper;

    public TalentAttributeService(TalentAttributeMapper mapper) {
        this.mapper = mapper;
    }

    @Cacheable(value = "attributes")
    public List<TalentAttribute> listAll() {
        return mapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>()
                        .eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));
    }

    public Page<TalentAttribute> page(int pageNum, int pageSize) {
        return mapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TalentAttribute>()
                        .orderByAsc(TalentAttribute::getSortOrder));
    }

    public TalentAttribute getById(Long id) {
        return mapper.selectById(id);
    }

    @CacheEvict(value = "attributes", allEntries = true)
    public void save(TalentAttribute attr) {
        if (attr.getId() == null) {
            mapper.insert(attr);
        } else {
            mapper.updateById(attr);
        }
    }

    @CacheEvict(value = "attributes", allEntries = true)
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @CacheEvict(value = "attributes", allEntries = true)
    public void toggleStatus(Long id) {
        TalentAttribute attr = mapper.selectById(id);
        if (attr != null) {
            attr.setStatus(attr.getStatus() == 1 ? 0 : 1);
            mapper.updateById(attr);
        }
    }
}
