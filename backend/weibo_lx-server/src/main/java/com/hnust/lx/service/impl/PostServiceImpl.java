package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.PostAddDTO;
import com.hnust.lx.dto.PostUpdateDTO;
import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.exception.ResourceNotFoundException;
import com.hnust.lx.json.JacksonObjectMapper;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.PostService;
import com.hnust.lx.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int MAX_IMAGES = 9;
    private static final String POST_IMAGE_PREFIX = "/upload/post/";
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final JacksonObjectMapper objectMapper;

    @Value("${weibo.web.upload-path}")
    private String uploadPath;

    @Override
    public PostVO addPost(PostAddDTO dto) {
        List<String> safeImages = normalizeAndValidateImageUrls(dto.getImages());

        Post post = Post.builder()
                .userId(dto.getUserId())
                .content(dto.getContent())
                .images(toJson(safeImages))
                .commentCount(0)
                .likeCount(0)
                .isDeleted(0)
                .build();
        postMapper.insert(post);

        Post saved = postMapper.findById(post.getPostId());
        return buildPostVO(saved != null ? saved : post);
    }

    @Override
    public PageResult getPostList(Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Post> posts = postMapper.findByPage();
        PageInfo<Post> pageInfo = new PageInfo<>(posts);

        List<PostVO> voList = posts.stream().map(this::buildPostVO).collect(Collectors.toList());
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        return buildPostVO(post);
    }

    @Override
    public PostVO updatePost(Long userId, PostUpdateDTO dto) {
        Post post = postMapper.findById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }

        List<String> finalImages = dto.getImages() == null
                ? parseImages(post.getImages())
                : normalizeAndValidateImageUrls(dto.getImages());

        Post updatePost = Post.builder()
                .postId(dto.getPostId())
                .content(dto.getContent())
                .images(toJson(finalImages))
                .build();

        postMapper.update(updatePost);
        return getPostDetail(dto.getPostId());
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }
        postMapper.delete(postId);
    }

    @Override
    public Post getPostById(Long postId) {
        return postMapper.findById(postId);
    }

    @Override
    public List<Post> getUserPosts(Long userId) {
        return postMapper.findByUserId(userId);
    }

    @Override
    public Long getUserTotalLikes(Long userId) {
        List<Post> userPosts = postMapper.findByUserId(userId);
        return userPosts.stream()
                .mapToLong(p -> p.getLikeCount() != null ? p.getLikeCount() : 0)
                .sum();
    }

    @Override
    public String uploadPostImage(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        if (file == null || file.isEmpty()) {
            throw new BaseException("图片文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getSafeExtension(originalFilename);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BaseException("仅支持图片文件");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BaseException("仅支持 jpg/jpeg/png/webp/gif 格式");
        }

        File uploadDir = new File(uploadPath + "post/");
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new BaseException("创建图片目录失败");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        File destFile = new File(uploadDir, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BaseException("保存图片失败");
        }

        return POST_IMAGE_PREFIX + fileName;
    }

    private PostVO buildPostVO(Post post) {
        List<String> images = parseImages(post.getImages());
        User user = userMapper.findById(post.getUserId());

        return PostVO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .content(post.getContent())
                .images(images)
                .likeCount(post.getLikeCount() == null ? 0L : Long.valueOf(post.getLikeCount()))
                .commentCount(post.getCommentCount() == null ? 0L : Long.valueOf(post.getCommentCount()))
                .createTime(post.getPostTime())
                .status((long) (post.getIsDeleted() == 0 ? 1 : 0))
                .build();
    }

    private List<String> normalizeAndValidateImageUrls(List<String> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        if (images.size() > MAX_IMAGES) {
            throw new BaseException("每条动态最多上传 9 张图片");
        }

        List<String> normalized = new ArrayList<>();
        for (String imageUrl : images) {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new BaseException("图片地址不能为空");
            }
            String trimmed = imageUrl.trim();
            if (!trimmed.startsWith(POST_IMAGE_PREFIX)) {
                throw new BaseException("图片地址不合法");
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    imagesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private String toJson(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? new ArrayList<>() : images);
        } catch (Exception ex) {
            throw new BaseException("图片数据格式错误");
        }
    }

    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BaseException("图片文件名不合法");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }
}
