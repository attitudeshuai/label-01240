package com.blog.service;

import com.blog.dto.BlogRequest;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 博客服务
 */
@Slf4j
@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 获取博客列表（公开）
     */
    public PageResult<Blog> getBlogList(PageRequest request) {
        List<Blog> list = blogMapper.findList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取博客详情
     */
    public Blog getBlogDetail(Long id) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 增加浏览次数
        blogMapper.incrementViewCount(id);
        blog.setViewCount(blog.getViewCount() + 1);

        return blog;
    }

    /**
     * 发布博客
     */
    public Blog createBlog(BlogRequest request) {
        Long userId = UserContext.getUserId();

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setTags(request.getTags());
        blog.setAuthorId(userId);
        blog.setStatus(request.getStatus());
        blog.setViewCount(0);
        blog.setLikeCount(0);
        blog.setCommentCount(0);

        blogMapper.insert(blog);
        log.info("用户发布博客: userId={}, blogId={}, title={}", userId, blog.getId(), blog.getTitle());

        return blogMapper.findById(blog.getId());
    }

    /**
     * 更新博客
     */
    public Blog updateBlog(Long id, BlogRequest request) {
        Long userId = UserContext.getUserId();
        Blog blog = blogMapper.findById(id);

        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 验证权限：只有作者或管理员可以编辑
        if (!blog.getAuthorId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限编辑此博客");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setTags(request.getTags());
        blog.setStatus(request.getStatus());

        blogMapper.update(blog);
        log.info("用户更新博客: userId={}, blogId={}", userId, id);

        return blogMapper.findById(id);
    }

    /**
     * 删除博客（软删除）
     */
    public void deleteBlog(Long id) {
        Long userId = UserContext.getUserId();
        Blog blog = blogMapper.findById(id);

        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 验证权限：只有作者或管理员可以删除
        if (!blog.getAuthorId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限删除此博客");
        }

        blogMapper.softDelete(id);
        log.info("用户删除博客: userId={}, blogId={}", userId, id);
    }

    /**
     * 批量删除博客（软删除）
     */
    public void batchDeleteBlogs(List<Long> ids) {
        Long userId = UserContext.getUserId();
        boolean isAdmin = UserContext.isAdmin();

        for (Long id : ids) {
            Blog blog = blogMapper.findById(id);
            if (blog == null) {
                continue;
            }
            // 验证权限：只有作者或管理员可以删除
            if (!blog.getAuthorId().equals(userId) && !isAdmin) {
                throw new BusinessException(403, "无权限删除博客ID: " + id);
            }
        }

        blogMapper.batchSoftDelete(ids);
        log.info("用户批量删除博客: userId={}, ids={}", userId, ids);
    }

    /**
     * 获取用户博客列表
     */
    public PageResult<Blog> getUserBlogs(Long userId, PageRequest request) {
        List<Blog> list = blogMapper.findByAuthorId(userId, 1, request.getOffset(), request.getSize());
        Long total = blogMapper.countByAuthorId(userId, 1);
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取我的博客列表
     */
    public PageResult<Blog> getMyBlogs(PageRequest request) {
        Long userId = UserContext.getUserId();
        List<Blog> list = blogMapper.findByAuthorId(userId, request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countByAuthorId(userId, request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取全站博客列表（管理员）
     */
    public PageResult<Blog> getAllBlogs(PageRequest request) {
        List<Blog> list = blogMapper.findAllList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countAllList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 更新博客状态（管理员）
     */
    public void updateBlogStatus(Long id, Integer status) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        blogMapper.updateStatus(id, status);
        log.info("管理员更新博客状态: blogId={}, status={}", id, status);
    }

    /**
     * 更新博客状态（带审核原因）
     */
    public void updateBlogStatusWithReason(Long id, Integer status, String reason, Long reviewerId) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        blogMapper.updateStatusWithReason(id, status, reason, reviewerId);
        log.info("管理员审核博客: blogId={}, status={}, reason={}", id, status, reason);
    }
}
