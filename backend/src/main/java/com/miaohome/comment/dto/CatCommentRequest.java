package com.miaohome.comment.dto;

/**
 * 发表评论请求
 * 用户 ID 从 Session 获取，无需在请求中传递。
 *
 * @author weibang kong
 */
public class CatCommentRequest {

    /** 评论内容 */
    private String content;

    /** 父评论 ID，用于回复 */
    private Long parentId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
