package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.User;
import com.xxx.takeout.service.UserService;
import com.xxx.takeout.utils.SMSUtils;
import com.xxx.takeout.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 1. 获取手机号
        String phone = user.getPhone();

        if(!phone.isEmpty()){
            // 2. 生成验证码
            // SMSUtils.sendMessage("test","",phone, code);
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码为：{}", code);

            // 3. 将验证码存入session
            session.setAttribute(phone,code);
            return R.success("发送成功");
        }
        return R.error("发送失败");

    }

    // 移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map userInfo, HttpSession session){
        // 1. 获取手机号和验证码
        String phone = userInfo.get("phone").toString();
        // String code = userInfo.get("code").toString();
        String code = "123456";

        // 2. 判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            return R.error("手机号或验证码为空");
        }

        // 3. 从session中获取验证码
        // String sessionCode = (String) session.getAttribute(phone);
        String sessionCode = "123456";

        // 4. 判断验证码是否正确
        if(code.equals(sessionCode)){
            // 5. 验证码正确，判断用户是否存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                // 6. 用户不存在，为新用户，保存用户
                log.info("正在创建新用户");
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                return R.success(user);
            }

            // 7. 将用户信息存入session
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
