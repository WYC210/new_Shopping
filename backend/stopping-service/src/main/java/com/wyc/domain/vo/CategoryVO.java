package com.wyc.domain.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CategoryVO {
    private Long categoryId;
    private String name;
    private Long parentId;
    private Long sortOrder;
    private Boolean isVisible;
    private Date createdAt;
    private Date updatedAt;

    // 子分类列表
    private List<CategoryVO> children;
}