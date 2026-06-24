package com.talent.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据初始化器
 * <p>
 * 应用启动时自动初始化管理员用户、指标定义、测试人才数据、筛选方案和评分方案。
 * 仅在数据库为空时执行（管理员用户除外，每次启动都检查）。
 * </p>
 *
 * @author talent-hr
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TalentAttributeMapper attrMapper;
    private final TalentMapper talentMapper;
    private final TalentAttrValueMapper valueMapper;
    private final ScreeningPlanMapper planMapper;
    private final SelectionConditionMapper condMapper;
    private final ScoringPlanMapper scoringPlanMapper;
    private final TalentScoreMapper scoreMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造方法
     *
     * @param attrMapper         指标 Mapper
     * @param talentMapper       人才 Mapper
     * @param valueMapper        指标值 Mapper
     * @param planMapper         筛选方案 Mapper
     * @param condMapper         筛选条件 Mapper
     * @param scoringPlanMapper  评分方案 Mapper
     * @param scoreMapper        评分记录 Mapper
     * @param sysUserMapper      用户 Mapper
     * @param passwordEncoder    密码编码器
     */
    public DataInitializer(TalentAttributeMapper attrMapper, TalentMapper talentMapper,
                           TalentAttrValueMapper valueMapper, ScreeningPlanMapper planMapper,
                           SelectionConditionMapper condMapper, ScoringPlanMapper scoringPlanMapper,
                           TalentScoreMapper scoreMapper, SysUserMapper sysUserMapper,
                           PasswordEncoder passwordEncoder) {
        this.attrMapper = attrMapper;
        this.talentMapper = talentMapper;
        this.valueMapper = valueMapper;
        this.planMapper = planMapper;
        this.condMapper = condMapper;
        this.scoringPlanMapper = scoringPlanMapper;
        this.scoreMapper = scoreMapper;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 管理员用户始终检查并初始化
        initAdminUser();

        if (attrMapper.selectCount(null) > 0) {
            return;
        }
        log.info("===== 开始初始化数据 =====");
        initAttributes();
        initTalents();
        initScreeningPlans();
        initScoringPlan();
        log.info("===== 数据初始化完成 =====");
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        if (sysUserMapper.selectCount(null) > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setStatus(1);
        sysUserMapper.insert(admin);
        log.info("管理员用户已初始化: admin / admin123");
    }

    /**
     * 初始化指标定义
     */
    private void initAttributes() {
        List<TalentAttribute> attrs = new ArrayList<>();

        attrs.add(buildAttr("education", "学历", "ENUM", null,
                "[\"博士\",\"硕士\",\"本科\",\"大专\",\"高中\"]",
                "{\"博士\":100,\"硕士\":80,\"本科\":60,\"大专\":40,\"高中\":20}",
                25, 1, 1, 1));
        attrs.add(buildAttr("work_years", "工作年限", "NUMBER", "年", null, null, 20, 1, 1, 2));
        attrs.add(buildAttr("height", "身高", "NUMBER", "cm", null, null, 5, 1, 0, 3));
        attrs.add(buildAttr("english_level", "英语水平", "ENUM", null,
                "[\"专业八级\",\"CET-6\",\"CET-4\",\"无\"]",
                "{\"专业八级\":100,\"CET-6\":80,\"CET-4\":50,\"无\":0}",
                10, 1, 0, 4));
        attrs.add(buildAttr("hometown", "籍贯", "TEXT", null, null, null, 5, 1, 0, 5));
        attrs.add(buildAttr("perf_rating", "绩效评级", "ENUM", null,
                "[\"S\",\"A\",\"B\",\"C\",\"D\"]",
                "{\"S\":100,\"A\":85,\"B\":70,\"C\":50,\"D\":20}",
                20, 1, 1, 6));
        attrs.add(buildAttr("age", "年龄", "NUMBER", "岁", null, null, 5, -1, 0, 7));
        attrs.add(buildAttr("cert_count", "资格证书数", "NUMBER", "个", null, null, 5, 1, 0, 8));
        attrs.add(buildAttr("manage_years", "管理年限", "NUMBER", "年", null, null, 5, 1, 0, 9));

        attrs.forEach(attrMapper::insert);
        log.info("指标数据已初始化: {} 个", attrs.size());
    }

    /**
     * 构建指标实体
     *
     * @param code         指标编码
     * @param name         指标名称
     * @param type         数据类型
     * @param unit         单位
     * @param optionsJson  枚举选项 JSON
     * @param scoreMapping 分值映射 JSON
     * @param weight       权重
     * @param direction    方向（1 越高越好，-1 越低越好）
     * @param required     是否必填（0 否，1 是）
     * @param sortOrder    排序序号
     * @return 指标实体
     */
    private TalentAttribute buildAttr(String code, String name, String type, String unit,
                                       String optionsJson, String scoreMapping,
                                       int weight, int direction, int required, int sortOrder) {
        TalentAttribute attr = new TalentAttribute();
        attr.setCode(code);
        attr.setName(name);
        attr.setType(type);
        attr.setUnit(unit);
        attr.setOptionsJson(optionsJson);
        attr.setScoreMapping(scoreMapping);
        attr.setWeight(weight);
        attr.setDirection(direction);
        attr.setIsRequired(required);
        attr.setSortOrder(sortOrder);
        attr.setStatus(1);
        return attr;
    }

    /**
     * 初始化测试人才数据
     */
    private void initTalents() {
        String[][] rawTalents = {
            {"张伟", "男", "1990-03-15", "技术部", "高级工程师", "zhangwei@hr.com", "13800001001", "硕士", "8", "175", "CET-6", "北京", "A", "34", "3", "2"},
            {"李娜", "女", "1992-07-22", "市场部", "市场总监", "lina@hr.com", "13800001002", "本科", "6", "162", "专业八级", "上海", "S", "32", "4", "1"},
            {"王强", "男", "1988-11-08", "技术部", "技术经理", "wangqiang@hr.com", "13800001003", "博士", "12", "178", "CET-4", "深圳", "S", "37", "5", "4"},
            {"刘芳", "女", "1995-05-18", "人事部", "HRBP", "liufang@hr.com", "13800001004", "本科", "4", "160", "CET-6", "广州", "B", "29", "2", "1"},
            {"陈明", "男", "1985-01-30", "技术部", "架构师", "chenming@hr.com", "13800001005", "硕士", "15", "180", "CET-4", "杭州", "A", "41", "6", "6"},
            {"赵丽", "女", "1993-09-12", "财务部", "财务经理", "zhaoli@hr.com", "13800001006", "硕士", "7", "165", "CET-6", "成都", "A", "32", "3", "3"},
            {"孙磊", "男", "1991-06-25", "技术部", "高级工程师", "sunlei@hr.com", "13800001007", "本科", "9", "172", "CET-4", "南京", "B", "35", "4", "2"},
            {"周婷", "女", "1994-12-03", "运营部", "运营总监", "zhouting@hr.com", "13800001008", "硕士", "5", "163", "专业八级", "武汉", "A", "31", "3", "1"},
            {"吴刚", "男", "1987-04-20", "技术部", "研发总监", "wugang@hr.com", "13800001009", "博士", "14", "176", "CET-6", "北京", "S", "39", "7", "5"},
            {"郑雪", "女", "1996-08-16", "设计部", "UI设计师", "zhengxue@hr.com", "13800001010", "本科", "3", "158", "CET-4", "上海", "B", "28", "1", "1"},
            {"黄海", "男", "1983-02-28", "技术部", "CTO", "huanghai@hr.com", "13800001011", "博士", "18", "177", "CET-6", "北京", "S", "43", "8", "8"},
            {"马丽", "女", "1990-10-05", "人事部", "HR总监", "mali@hr.com", "13800001012", "硕士", "10", "161", "CET-6", "深圳", "A", "35", "5", "5"},
            {"朱伟", "男", "1994-03-18", "销售部", "销售经理", "zhuwei@hr.com", "13800001013", "本科", "7", "175", "CET-4", "广州", "A", "30", "2", "2"},
            {"林燕", "女", "1989-07-09", "技术部", "项目经理", "linyan@hr.com", "13800001014", "硕士", "11", "164", "CET-6", "杭州", "B", "36", "4", "4"},
            {"何涛", "男", "1992-11-22", "法务部", "法务总监", "hetao@hr.com", "13800001015", "博士", "8", "179", "CET-6", "北京", "A", "33", "3", "3"},
            {"谢芳", "女", "1995-04-14", "行政部", "行政主管", "xiefang@hr.com", "13800001016", "本科", "4", "159", "CET-4", "上海", "C", "29", "2", "1"},
            {"韩冰", "男", "1986-12-07", "技术部", "技术专家", "hanbing@hr.com", "13800001017", "硕士", "13", "181", "CET-6", "深圳", "A", "39", "6", "5"},
            {"唐静", "女", "1993-05-26", "市场部", "品牌经理", "tangjing@hr.com", "13800001018", "本科", "6", "163", "专业八级", "成都", "B", "32", "3", "2"},
            {"曹勇", "男", "1988-09-03", "运维部", "运维经理", "caoyong@hr.com", "13800001019", "硕士", "10", "173", "CET-4", "武汉", "B", "37", "4", "4"},
            {"彭慧", "女", "1991-01-19", "产品部", "产品总监", "penghui@hr.com", "13800001020", "硕士", "9", "162", "CET-6", "北京", "A", "34", "5", "5"},
        };

        // 构建指标 code -> id 映射
        List<TalentAttribute> allAttrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1));
        Map<String, Long> attrMap = new LinkedHashMap<>();
        for (TalentAttribute a : allAttrs) {
            attrMap.put(a.getCode(), a.getId());
        }

        for (String[] row : rawTalents) {
            Talent t = new Talent();
            t.setName(row[0]);
            t.setGender(row[1]);
            t.setBirthDate(LocalDate.parse(row[2]));
            t.setDept(row[3]);
            t.setPosition(row[4]);
            t.setEmail(row[5]);
            t.setPhone(row[6]);
            t.setStatus(1);
            talentMapper.insert(t);

            // 保存各指标值（索引 7~15 对应 9 个指标）
            saveValue(t.getId(), attrMap.get("education"), row[7]);
            saveValue(t.getId(), attrMap.get("work_years"), row[8]);
            saveValue(t.getId(), attrMap.get("height"), row[9]);
            saveValue(t.getId(), attrMap.get("english_level"), row[10]);
            saveValue(t.getId(), attrMap.get("hometown"), row[11]);
            saveValue(t.getId(), attrMap.get("perf_rating"), row[12]);
            saveValue(t.getId(), attrMap.get("age"), row[13]);
            saveValue(t.getId(), attrMap.get("cert_count"), row[14]);
            saveValue(t.getId(), attrMap.get("manage_years"), row[15]);
        }
        log.info("人才数据已初始化: {} 人", rawTalents.length);
    }

    /**
     * 保存单条指标值
     *
     * @param talentId 人才 ID
     * @param attrId   指标 ID
     * @param value    指标值
     */
    private void saveValue(Long talentId, Long attrId, String value) {
        TalentAttrValue v = new TalentAttrValue();
        v.setTalentId(talentId);
        v.setAttrId(attrId);
        v.setValueText(value);
        valueMapper.insert(v);
    }

    /**
     * 初始化筛选方案
     */
    private void initScreeningPlans() {
        // 构建指标 code -> id 映射
        List<TalentAttribute> allAttrs = attrMapper.selectList(null);
        Map<String, Long> am = new HashMap<>();
        for (TalentAttribute a : allAttrs) {
            am.put(a.getCode(), a.getId());
        }

        // 方案1：高级技术人才筛选（AND 逻辑）
        ScreeningPlan p1 = new ScreeningPlan();
        p1.setName("高级技术人才筛选");
        p1.setDescription("学历硕士及以上 + 工作年限>=8 + 绩效A及以上");
        p1.setLogicType("AND");
        p1.setStatus(1);
        planMapper.insert(p1);

        condMapper.insert(buildCond(p1.getId(), "学历>=硕士", am.get("education"), "IN", "硕士,博士", 1));
        condMapper.insert(buildCond(p1.getId(), "工作年限>=8", am.get("work_years"), "GTE", "8", 2));
        condMapper.insert(buildCond(p1.getId(), "绩效>=A", am.get("perf_rating"), "IN", "S,A", 3));

        // 方案2：年轻骨干筛选（AND 逻辑）
        ScreeningPlan p2 = new ScreeningPlan();
        p2.setName("年轻骨干筛选");
        p2.setDescription("年龄<=35 + 工作年限>=5 + 本科及以上 + 绩效B及以上");
        p2.setLogicType("AND");
        p2.setStatus(1);
        planMapper.insert(p2);

        condMapper.insert(buildCond(p2.getId(), "年龄<=35", am.get("age"), "LTE", "35", 1));
        condMapper.insert(buildCond(p2.getId(), "工作年限>=5", am.get("work_years"), "GTE", "5", 2));
        condMapper.insert(buildCond(p2.getId(), "学历>=本科", am.get("education"), "IN", "博士,硕士,本科", 3));
        condMapper.insert(buildCond(p2.getId(), "绩效>=B", am.get("perf_rating"), "IN", "S,A,B", 4));

        // 方案3：高学历或高绩效（OR 逻辑）
        ScreeningPlan p3 = new ScreeningPlan();
        p3.setName("高学历或高绩效");
        p3.setDescription("博士学历 或 绩效=S");
        p3.setLogicType("OR");
        p3.setStatus(1);
        planMapper.insert(p3);

        condMapper.insert(buildCond(p3.getId(), "博士学历", am.get("education"), "EQ", "博士", 1));
        condMapper.insert(buildCond(p3.getId(), "绩效=S", am.get("perf_rating"), "EQ", "S", 2));

        log.info("筛选方案已初始化: 3 个");
    }

    /**
     * 构建筛选条件实体
     *
     * @param planId   方案 ID
     * @param name     条件名称
     * @param attrId   指标 ID
     * @param operator 操作符
     * @param value    比较值
     * @param sort     排序序号
     * @return 条件实体
     */
    private SelectionCondition buildCond(Long planId, String name, Long attrId,
                                          String operator, String value, int sort) {
        SelectionCondition c = new SelectionCondition();
        c.setPlanId(planId);
        c.setName(name);
        c.setAttrId(attrId);
        c.setOperator(operator);
        c.setValue(value);
        c.setSortOrder(sort);
        return c;
    }

    /**
     * 初始化评分方案
     */
    private void initScoringPlan() {
        ScoringPlan sp = new ScoringPlan();
        sp.setName("综合能力评分");
        sp.setRemark("学历25% + 工作年限20% + 绩效20% + 英语10% + 管理年限5% + 资格证书5% + 年龄5% + 身高5% + 籍贯5%");
        sp.setExpression("education * 0.25 + work_years * 0.20 + perf_rating * 0.20 + english_level * 0.10 + manage_years * 0.05 + cert_count * 0.05 + age * 0.05 + height * 0.05 + hometown * 0.05");
        sp.setStatus(1);
        scoringPlanMapper.insert(sp);
        log.info("评分方案已初始化: 1 个");
    }
}
