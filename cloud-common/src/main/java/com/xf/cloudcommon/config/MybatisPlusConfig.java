package com.xf.cloudcommon.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xf.cloudcommon.utils.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @Description: MyBatis-Plus 配置类 (包含分页插件和审计字段自动填充)
 * @ClassName: MybatisPlusConfig
 */
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * 配置 MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件 (设置数据库类型为 MySQL)
        // 如果不加这个，调用 mapper.selectPage 会导致全表查询，并且 Page 对象的 total 属性查不出来
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间和更新时间
        // 注意：这里的属性名 "createTime" 对应的是实体类中的属性名，而不是数据库字段名 "create_time"
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 自动填充创建人和更新人
        String userId = UserContextHolder.getUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", String.class, userId);
            this.strictInsertFill(metaObject, "updateBy", String.class, userId);
        }
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 自动填充更新人
        String userId = UserContextHolder.getUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, userId);
        }
    }
}
