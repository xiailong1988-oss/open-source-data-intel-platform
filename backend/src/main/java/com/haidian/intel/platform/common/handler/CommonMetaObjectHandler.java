package com.haidian.intel.platform.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.haidian.intel.platform.common.auth.UserContextHolder;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * Common audit field auto-fill handler.
 */
@Component
public class CommonMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContextHolder.getUserIdOrDefault(0L);
        strictInsertFill(metaObject, "createdBy", Long.class, userId);
        strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedBy", UserContextHolder.getUserIdOrDefault(0L), metaObject);
        setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
    }
}
