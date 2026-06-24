package com.talent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.JwtUtil;
import com.talent.common.R;
import com.talent.entity.SysUser;
import com.talent.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, SysUserMapper sysUserMapper,
                          PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return R.fail(400, "用户名和密码不能为空");
        }

        // 从数据库查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1));

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return R.fail(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(username);
        return R.ok(Map.of("token", token, "username", username,
                "realName", user.getRealName() != null ? user.getRealName() : username));
    }
}
