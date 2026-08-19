import { useState, useEffect, useCallback } from "react";
import {
  Input,
  Button,
  List,
  Popconfirm,
  message,
  Select,
  Typography,
} from "antd";
import {
  LikeOutlined,
  LikeFilled,
  DeleteOutlined,
  MessageOutlined,
} from "@ant-design/icons";
import { Link } from "react-router-dom";
import type { CatComment } from "../types";
import {
  getComments,
  createComment,
  deleteComment,
  toggleCommentLike,
} from "../api";
import { useAuth } from "../contexts/AuthContext";

const { TextArea } = Input;
const { Text } = Typography;

interface CommentSectionProps {
  catId: number;
}

export default function CommentSection({ catId }: CommentSectionProps) {
  const { user, isAuthenticated } = useAuth();
  const [comments, setComments] = useState<CatComment[]>([]);
  const [loading, setLoading] = useState(false);
  const [sortBy, setSortBy] = useState<"created_at" | "like_count">("created_at");
  const [order, setOrder] = useState<"desc" | "asc">("desc");
  const [inputValue, setInputValue] = useState("");
  const [replyTo, setReplyTo] = useState<{ id: number; nickname: string } | null>(null);

  const loadComments = useCallback(() => {
    if (!catId) return;
    setLoading(true);
    getComments(catId, sortBy, order)
      .then((res) => {
        if (res.code === 200) setComments(res.data);
      })
      .finally(() => setLoading(false));
  }, [catId, sortBy, order]);

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  const handleSubmit = async () => {
    if (!inputValue.trim() || !isAuthenticated) return;
    await createComment(catId, {
      content: inputValue.trim(),
      parentId: replyTo?.id,
    });
    message.success(replyTo ? "回复成功" : "评论成功");
    setInputValue("");
    setReplyTo(null);
    loadComments();
  };

  const handleDelete = async (commentId: number) => {
    if (!isAuthenticated) return;
    await deleteComment(commentId);
    message.success("已删除");
    loadComments();
  };

  const handleToggleLike = async (commentId: number) => {
    if (!isAuthenticated) {
      message.info("请先登录");
      return;
    }
    await toggleCommentLike(commentId);
    loadComments();
  };

  const handleReply = (comment: CatComment) => {
    if (!isAuthenticated) {
      message.info("请先登录");
      return;
    }
    setReplyTo({ id: comment.id, nickname: comment.nickname });
  };

  const renderComment = (comment: CatComment, isReply = false) => (
    <div key={comment.id} style={{ marginBottom: isReply ? 0 : 0 }}>
      <div className="comment-item" style={{ paddingLeft: isReply ? 8 : 0, borderBottom: isReply ? "none" : undefined }}>
        <div className="comment-avatar">
          {comment.nickname?.charAt(0) || "?"}
        </div>
        <div className="comment-body">
          <div className="comment-header">
            <span className="comment-author">{comment.nickname}</span>
            <span className="comment-time">
              {comment.createdAt?.replace("T", " ").substring(0, 16)}
            </span>
          </div>
          <div className="comment-content">{comment.content}</div>
          <div className="comment-actions">
            <button
              className="comment-action-btn"
              onClick={() => handleToggleLike(comment.id)}
            >
              {comment.likedByCurrentUser ? (
                <LikeFilled style={{ color: "#1C85E8" }} />
              ) : (
                <LikeOutlined />
              )}
              {comment.likeCount || 0}
            </button>
            {!isReply && (
              <button
                className="comment-action-btn"
                onClick={() => handleReply(comment)}
              >
                <MessageOutlined />
                回复
              </button>
            )}
            {user && user.id === comment.userId && (
              <Popconfirm title="确定删除？" onConfirm={() => handleDelete(comment.id)}>
                <button className="comment-action-btn danger">
                  <DeleteOutlined />
                </button>
              </Popconfirm>
            )}
          </div>
        </div>
      </div>
      {/* 子回复 */}
      {!isReply && comment.replies && comment.replies.length > 0 && (
        <div className="comment-replies">
          {comment.replies.map((reply) => renderComment(reply, true))}
        </div>
      )}
    </div>
  );

  const sortOptions = [
    { value: "created_at_desc", label: "最新" },
    { value: "created_at_asc", label: "最早" },
    { value: "like_count_desc", label: "最热" },
  ];

  const currentSortValue = `${sortBy}_${order}`;

  const handleSortChange = (value: string) => {
    const [sb, od] = value.split("_");
    setSortBy(sb as "created_at" | "like_count");
    setOrder(od as "desc" | "asc");
  };

  return (
    <div>
      {/* 发表评论 */}
      <div style={{ marginBottom: 24 }}>
        {replyTo && (
          <div className="comment-reply-banner">
            <span>回复 <Text strong>{replyTo.nickname}</Text></span>
            <Button type="link" size="small" onClick={() => setReplyTo(null)}>
              取消
            </Button>
          </div>
        )}
        {isAuthenticated ? (
          <>
            <TextArea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder={replyTo ? `回复 ${replyTo.nickname}...` : "说点什么..."}
              rows={3}
              maxLength={500}
              showCount
              style={{ borderRadius: 8, resize: "none" }}
            />
            <Button
              type="primary"
              onClick={handleSubmit}
              disabled={!inputValue.trim()}
              className="btn-primary"
              style={{ marginTop: 8, borderRadius: 8 }}
            >
              {replyTo ? "回复" : "发表评论"}
            </Button>
          </>
        ) : (
          <div
            style={{
              padding: "16px 20px",
              background: "#FAFAFA",
              borderRadius: 8,
              textAlign: "center",
            }}
          >
            <Text type="secondary" style={{ fontSize: 14 }}>
              <Link to="/login" style={{ color: "#1C85E8", fontWeight: 600 }}>
                登录
              </Link>{" "}
              后即可评论互动
            </Text>
          </div>
        )}
      </div>

      {/* 排序 */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 8,
          paddingBottom: 12,
          borderBottom: "1px solid #F0F0F0",
        }}
      >
        <Text strong style={{ fontSize: 15 }}>
          评论 ({comments.length})
        </Text>
        <Select
          size="small"
          value={currentSortValue}
          onChange={handleSortChange}
          options={sortOptions}
          style={{ width: 100 }}
        />
      </div>

      {/* 评论列表 */}
      <List
        loading={loading}
        dataSource={comments}
        renderItem={(c) => renderComment(c)}
        locale={{ emptyText: "暂无评论，来抢沙发吧~" }}
        style={{ border: "none" }}
      />
    </div>
  );
}
