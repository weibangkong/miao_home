package com.miaohome.comment.dto;

/**
 * 点赞结果响应
 * <p>猫咪点赞、评论点赞切换及点赞状态查询的统一返回结构。</p>
 *
 * @author weibang kong
 */
public class LikeResultResponse {

    /** 是否已点赞（true 为点赞状态，false 为取消点赞状态） */
    private Boolean liked;

    /** 点赞总数（点赞状态查询接口不返回，可为空） */
    private Long likeCount;

    public Boolean getLiked() { return liked; }


    public void setLiked(Boolean liked) { this.liked = liked; }


    public Long getLikeCount() { return likeCount; }


    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
}
