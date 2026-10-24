package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.LoginReqVo;
import edu.dlnu.health_platform.vo.request.RegisterReqVo;
import edu.dlnu.health_platform.vo.response.LoginRespVo;
import edu.dlnu.health_platform.vo.response.PageResult;

public interface UserService {

    //注册
    Result<LoginRespVo> register(RegisterReqVo vo);

    //登录
    Result<LoginRespVo> login(LoginReqVo vo);

    //检查用户名是否存在
    Result<Boolean> checkUsername(String userName);

    //查询用户列表（管理员，分页，可按角色过滤）
    Result<PageResult<User>> getAllUsers(Integer role, Integer pageNum, Integer pageSize);

    //冻结/解冻用户（管理员）
    Result<Void> updateStatus(Long userId, Integer status);

    //用户总数（看板统计）
    long countAll();

    //按角色统计人数（看板统计）
    long countByRole(Integer role);
}
