package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.entity.Comment;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.CommentMapper;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论服务
 */
@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 获取博客评论列表
     */
    public PageResult<Comment> getCommentsByBlogId(Long blogId, PageRequest request) {
        List<Comment> list = commentMapper.findByBlogId(blogId, request.getOffset(), request.getSize());
        Long total = commentMapper.countByBlogId(blogId);
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 发表评论
     */
    @Transactional
    public Comment createComment(CommentRequest request) {
        Long userId = UserContext.getUserId();

        // 验证博客是否存在
        Blog blog = blogMapper.findById(request.getBlogId());
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 内容合法性验证
        String content = request.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 强制纯文本：过滤HTML标签和脚本
        content = sanitizeContent(content);
        
        if (content.isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字符");
        }

        Comment comment = new Comment();
        comment.setBlogId(request.getBlogId());
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setStatus(1);

        commentMapper.insert(comment);

        // 更新博客评论数
        blogMapper.updateCommentCount(request.getBlogId(), 1);

        log.info("用户发表评论: userId={}, blogId={}, commentId={}", userId, request.getBlogId(), comment.getId());

        return commentMapper.findById(comment.getId());
    }

    /**
     * 过滤HTML标签和脚本，强制纯文本
     */
    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        // 移除所有HTML标签
        content = content.replaceAll("<[^>]*>", "");
        // 转义特殊HTML字符
        content = content.replace("&", "&amp;")
                         .replace("<", "&lt;")
                         .replace(">", "&gt;")
                         .replace("\"", "&quot;")
                         .replace("'", "&#x27;");
        // 移除潜在的脚本关键字
        content = content.replaceAll("(?i)javascript:", "")
                         .replaceAll("(?i)vbscript:", "")
                         .replaceAll("(?i)on\\w+\\s*=", "");
        return content.trim();
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long id) {
        Long userId = UserContext.getUserId();
        Comment comment = commentMapper.findById(id);

        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 验证权限：只有评论者或管理员可以删除
        if (!comment.getUserId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限删除此评论");
        }

        commentMapper.delete(id);

        // 更新博客评论数
        blogMapper.updateCommentCount(comment.getBlogId(), -1);

        log.info("用户删除评论: userId={}, commentId={}", userId, id);
    }

    /**
     * 获取全站评论列表（管理员）
     */
    public PageResult<Comment> getAllComments(PageRequest request) {
        List<Comment> list = commentMapper.findAllList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = commentMapper.countAllList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 管理员删除评论（带审核原因）
     */
    @Transactional
    public void adminDeleteComment(Long id, String reason, Long reviewerId) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 更新评论状态为违规隐藏，并记录原因
        commentMapper.updateStatusWithReason(id, 2, reason, reviewerId);

        // 更新博客评论数
        blogMapper.updateCommentCount(comment.getBlogId(), -1);

        log.info("管理员删除评论: commentId={}, reason={}", id, reason);
    }
}
