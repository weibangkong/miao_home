package com.miaohome.comment.dto;

import java.util.List;

public class CatCommentResponse {

    private Long id;
    private Long catId;
    private Long tenantId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Boolean likedByCurrentUser;
    private String createdAt;
    private String updatedAt;

    /** 子回复列表 */
    private List<CatCommentResponse> replies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Boolean getLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(Boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<CatCommentResponse> getReplies() { return replies; }
    public void setReplies(List<CatCommentResponse> replies) { this.replies = replies; }
}
