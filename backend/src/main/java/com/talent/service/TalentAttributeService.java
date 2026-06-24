package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.talent.entity.TalentAttribute;
import com.talent.mapper.TalentAttributeMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 人才指标服务
 * <p>
 * 封装评价指标的查询、保存、删除、状态切换等业务逻辑。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class TalentAttributeService {

    private final TalentAttributeMapper mapper;

    /**
     * 构造方法
     *
     * @param mapper 指标 Mapper
     */
    public TalentAttributeService(TalentAttributeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询所有启用的指标（按排序序号升序）
     *
     * @return 指标列表
     */
    @Cacheable(value = "attributes")
    public List<TalentAttribute> listAll() {
        return mapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>()
                        .eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));
    }

    /**
     * 分页查询指标（含禁用的）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<TalentAttribute> page(int pageNum, int pageSize) {
        return mapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TalentAttribute>()
                        .orderByAsc(TalentAttribute::getSortOrder));
    }

    /**
     * 根据 ID 查询指标
     *
     * @param id 指标 ID
     * @return 指标实体
     */
    public TalentAttribute getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 保存指标（新增或更新）
     *
     * @param attr 指标实体
     */
    @CacheEvict(value = "attributes", allEntries = true)
    public void save(TalentAttribute attr) {
        if (attr.getId() == null) {
            mapper.insert(attr);
        } else {
            mapper.updateById(attr);
        }
    }

    /**
     * 删除指标（逻辑删除）
     *
     * @param id 指标 ID
     */
    @CacheEvict(value = "attributes", allEntries = true)
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 切换指标启用/禁用状态
     *
     * @param id 指标 ID
     */
    @CacheEvict(value = "attributes", allEntries = true)
    public void toggleStatus(Long id) {
        TalentAttribute attr = mapper.selectById(id);
        if (attr != null) {
            attr.setStatus(attr.getStatus() == 1 ? 0 : 1);
            mapper.updateById(attr);
        }
    }
}
