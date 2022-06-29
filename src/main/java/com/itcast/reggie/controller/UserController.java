package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcast.reggie.Utils.ValidateCodeUtils;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.User;
import com.itcast.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        //1.获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //2.随机生成一个四位的验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}", code);

            //3.调用阿里云提供的短信服务API来发送短息
            //SMSUtils.sendMessage("signature", "", phone, code);

            //4.将生成的验证码保存到Session中 与用户输入的验证码进行比对
            session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("手机验证码短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map) {
        log.info(map.toString());
        log.info(session.toString());

        //1.从map集合中获取信息
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //2.从session中获取保存的信息
        Object codeInSession = session.getAttribute(phone);

        //3.进行数据的比对（页面中提交的code与session中保存的code是否相同？）
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果比对成功则成功登录，判断该用户是否注册（若为新用户则自动注册）
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                //在数据库中没有查到该用户信息。进行用户注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //4.登录成功需要将用户id设置到session中（使访问请求能够成功通过过滤器）
            session.setAttribute("user", user.getId());

            //5.登录成功 将该用户信息返回一份给浏览器
            return R.success(user);
        }

        return R.error("登录失败");
    }
}
