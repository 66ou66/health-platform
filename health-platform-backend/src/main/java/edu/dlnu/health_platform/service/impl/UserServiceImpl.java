package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.PatientProfileMapper;
import edu.dlnu.health_platform.dal.UserMapper;
import edu.dlnu.health_platform.dal.VolunteerProfileMapper;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.service.UserService;
import edu.dlnu.health_platform.utils.JwtUtils;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.LoginReqVo;
import edu.dlnu.health_platform.vo.request.RegisterReqVo;
import edu.dlnu.health_platform.vo.response.LoginRespVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PatientProfileMapper patientProfileMapper;
    @Autowired
    private VolunteerProfileMapper volunteerProfileMapper;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Transactional
    public Result<LoginRespVo> register(RegisterReqVo vo) {
        if (vo == null || vo.getUserName() == null || vo.getUserName().isBlank()) {
            return Result.fail("用户名不能为空");
        }
        if (vo.getPassword() == null || vo.getPassword().length() < 6) {
            return Result.fail("密码至少 6 位");
        }
        if (vo.getRole() == null || (vo.getRole() != 0 && vo.getRole() != 1 && vo.getRole() != 2)) {
            return Result.fail("请选择正确的角色");
        }
        if (vo.getRealName() == null || vo.getRealName().isBlank()) {
            return Result.fail("真实姓名不能为空");
        }
        if (userMapper.findByUserName(vo.getUserName()) != null) {
            return Result.fail("用户名已存在");
        }
        User user = new User();
        user.setUsername(vo.getUserName());
        user.setPassword(vo.getPassword());
        user.setRealName(vo.getRealName());
        user.setPhone(vo.getPhone());
        user.setRole(vo.getRole());
        userMapper.insert(user);
        //患者/志愿者注册时同步创建空档案
        if (vo.getRole() == 1) {
            patientProfileMapper.insert(user.getId());
        } else if (vo.getRole() == 2) {
            volunteerProfileMapper.insert(user.getId());
        }
        return Result.success();
    }

    @Override
    public Result<LoginRespVo> login(LoginReqVo vo) {
        if (vo == null || vo.getUserName() == null || vo.getUserName().isBlank()) {
            return Result.fail("用户名不能为空");
        }
        User user = userMapper.findByUserName(vo.getUserName());
        if (user == null || !user.getPassword().equals(vo.getPassword())) {
            return Result.fail("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail("账号已被冻结，请联系管理员");
        }
        if (vo.getRole() != null && !vo.getRole().equals(user.getRole())) {
            return Result.fail("角色选择错误");
        }
        //生成令牌并组装用户信息
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        return Result.success(new LoginRespVo(token, userInfo));
    }

    @Override
    public Result<Boolean> checkUsername(String userName) {
        return Result.success(userMapper.findByUserName(userName) != null);
    }

    @Override
    public Result<PageResult<User>> getAllUsers(Integer role, Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(userMapper.findAll(new Page<>(pageNum, pageSize), role)));
    }

    @Override
    public Result<Void> updateStatus(Long userId, Integer status) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail("状态值不合法");
        }
        userMapper.updateStatus(userId, status);
        return Result.success();
    }

    @Override
    public long countAll() {
        return userMapper.countAll();
    }

    @Override
    public long countByRole(Integer role) {
        return userMapper.countByRole(role);
    }
}
