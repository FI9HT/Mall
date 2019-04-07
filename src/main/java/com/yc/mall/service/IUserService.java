package com.yc.mall.service;

import com.yc.mall.common.ServerResponse;
import com.yc.mall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);
}
