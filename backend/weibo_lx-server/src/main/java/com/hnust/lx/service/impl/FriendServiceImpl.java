package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.entity.Friend;
import com.hnust.lx.entity.FriendRequest;
import com.hnust.lx.entity.User;
import com.hnust.lx.mapper.FriendMapper;
import com.hnust.lx.mapper.FriendRequestMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.FriendService;
import com.hnust.lx.service.WebSocketNotificationService;
import com.hnust.lx.vo.FriendRequestVO;
import com.hnust.lx.vo.FriendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final WebSocketNotificationService notificationService;

    @Override
    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        if (friendMapper.findByUserAndFriend(fromUserId, toUserId) != null) {
            throw new RuntimeException("你们已经是好友了");
        }
        if (friendRequestMapper.findPending(fromUserId, toUserId) != null) {
            throw new RuntimeException("已经发送过好友申请了");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setStatus(0);
        friendRequestMapper.insert(request);
        
        User fromUser = userMapper.findById(fromUserId);
        notificationService.notifyFriendRequest(toUserId, java.util.Map.of(
            "requestId", request.getId(),
            "fromUserId", fromUserId,
            "fromUsername", fromUser != null ? fromUser.getUsername() : null,
            "fromAvatar", fromUser != null ? fromUser.getAvatar() : null
        ));
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        FriendRequest request = new FriendRequest();
        request.setId(requestId);
        List<FriendRequest> pendingRequests = friendRequestMapper.findPendingByUserId(userId);
        
        FriendRequest targetRequest = null;
        for (FriendRequest req : pendingRequests) {
            if (req.getId().equals(requestId)) {
                targetRequest = req;
                break;
            }
        }
        
        if (targetRequest == null) {
            throw new RuntimeException("请求不存在");
        }

        friendRequestMapper.updateStatus(requestId, 1);

        Friend friend1 = new Friend();
        friend1.setUserId(targetRequest.getFromUserId());
        friend1.setFriendId(userId);
        friendMapper.insert(friend1);

        Friend friend2 = new Friend();
        friend2.setUserId(userId);
        friend2.setFriendId(targetRequest.getFromUserId());
        friendMapper.insert(friend2);
    }

    @Override
    public void rejectRequest(Long userId, Long requestId) {
        friendRequestMapper.updateStatus(requestId, 2);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        friendMapper.delete(userId, friendId);
        friendMapper.delete(friendId, userId);
    }

    @Override
    public PageResult getFriendList(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Friend> friends = friendMapper.findByUserId(userId);
        PageInfo<Friend> pageInfo = new PageInfo<>(friends);

        List<FriendVO> records = new ArrayList<>();
        for (Friend f : friends) {
            User user = userMapper.findById(f.getFriendId());
            if (user != null) {
                FriendVO vo = new FriendVO();
                vo.setUserId(user.getUserId());
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
                vo.setFriendTime(f.getFriendTime());
                records.add(vo);
            }
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public PageResult getPendingRequests(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<FriendRequest> requests = friendRequestMapper.findPendingByUserId(userId);
        PageInfo<FriendRequest> pageInfo = new PageInfo<>(requests);

        List<FriendRequestVO> records = new ArrayList<>();
        for (FriendRequest req : requests) {
            User user = userMapper.findById(req.getFromUserId());
            if (user != null) {
                FriendRequestVO vo = new FriendRequestVO();
                vo.setRequestId(req.getId());
                vo.setFromUserId(user.getUserId());
                vo.setFromUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
                vo.setCreateTime(req.getRequestTime());
                records.add(vo);
            }
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public PageResult getUserFriends(Long userId, Long page, Long pageSize) {
        return getFriendList(userId, page, pageSize);
    }

    @Override
    public Integer getPendingCount(Long userId) {
        return friendRequestMapper.countPending(userId);
    }

    @Override
    public boolean isFriend(Long userId, Long otherUserId) {
        return friendMapper.findByUserAndFriend(userId, otherUserId) != null;
    }
}