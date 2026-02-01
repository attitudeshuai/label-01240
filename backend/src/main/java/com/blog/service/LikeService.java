package com.blog.service;

import com.blog.entity.Blog;
import com.blog.entity.BlogLike;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogLikeMapper;
import com.blog.mapper.BlogMapper;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞服务
 */
@Slf4j
@Service
public class LikeService {

    @Autowired
    private BlogLikeMapper blogLikeMapper;

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 点赞/取消点赞
     */
    @Transactional
    public Map<String, Object> toggleLike(Long blogId) {
        Long userId = UserContext.getUserId();

        // 验证博客是否存在
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        BlogLike existLike = blogLikeMapper.findByBlogIdAndUserId(blogId, userId);
        boolean liked;

        if (existLike != null) {
            // 取消点赞
            blogLikeMapper.delete(blogId, userId);
            blogMapper.updateLikeCount(blogId, -1);
            liked = false;
            log.info("用户取消点赞: userId={}, blogId={}", userId, blogId);
        } else {
            // 点赞
            BlogLike like = new BlogLike();
            like.setBlogId(blogId);
            like.setUserId(userId);
            blogLikeMapper.insert(like);
            blogMapper.updateLikeCount(blogId, 1);
            liked = true;
            log.info("用户点赞: userId={}, blogId={}", userId, blogId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", blogLikeMapper.countByBlogId(blogId));
        return result;
    }

    /**
     * 获取点赞状态
     */
    public Map<String, Object> getLikeStatus(Long blogId) {
        Long userId = UserContext.getUserId();

        BlogLike like = blogLikeMapper.findByBlogIdAndUserId(blogId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", like != null);
        result.put("likeCount", blogLikeMapper.countByBlogId(blogId));
        return result;
    }
}
