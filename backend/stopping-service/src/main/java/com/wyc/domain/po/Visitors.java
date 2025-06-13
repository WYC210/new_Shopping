package com.wyc.domain.po;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 访客表对象 visitors
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Visitors
{
  

    /** 游客ID */
    private Long visitorId;

    /** 浏览器指纹 */
    private String fingerprint;

    /** 首次访问时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date firstVisitedAt;

    /** 最后访问时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastVisitedAt;

    /** 关联用户ID（若后续登录） */
    private Long userId;

}
