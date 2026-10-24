package edu.dlnu.health_platform.controller;

import edu.dlnu.health_platform.service.UserService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.LoginReqVo;
import edu.dlnu.health_platform.vo.request.RegisterReqVo;
import edu.dlnu.health_platform.vo.response.LoginRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块接口（/auth）：注册、登录、用户名查重、连接测试
 * RestFul风格：增加使用POST请求，删除使用DELETE请求，修改使用PUT请求，查询使用GET请求
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    //注册
    @PostMapping("/register")
    public Result<LoginRespVo> register(@RequestBody RegisterReqVo vo) {
        return userService.register(vo);
    }

    //登录
    @PostMapping("/login")
    public Result<LoginRespVo> login(@RequestBody LoginReqVo vo) {
        return userService.login(vo);
    }

    //检查用户名是否存在
    @GetMapping("/check/{userName}")
    public Result<Boolean> checkUsername(@PathVariable String userName) {
        return userService.checkUsername(userName);
    }

    //测试前后端连接
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端连接成功");
    }
}
