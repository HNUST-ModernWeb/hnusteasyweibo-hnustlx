package com.hnust.lx.service;

import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.FriendVO;

public interface FriendService {

    void sendRequest(Long fromUserId, Long toUserId);

    void acceptRequest(Long userId, Long requestId);

    void rejectRequest(Long userId, Long requestId);

    void deleteFriend(Long userId, Long friendId);

    PageResult getFriendList(Long userId, Long page, Long pageSize);

    PageResult getPendingRequests(Long userId, Long page, Long pageSize);

    PageResult getUserFriends(Long userId, Long page, Long pageSize);

    Integer getPendingCount(Long userId);

    boolean isFriend(Long userId, Long otherUserId);
}