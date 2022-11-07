package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zs.reggie.common.R;
import com.zs.reggie.pojo.User;
import com.zs.reggie.service.UserService;
import com.zs.reggie.utils.SendMailUtil;
import com.zs.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 发送验证码
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request){
        //获取手机号
        String email = user.getEmail();
        if(email!=null){
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            //发送给该手机号
            log.info("您的验证码为{},请妥善保管",code);
            //SendMailUtil.sendEmail(email,code);
            //保存到session
            //request.getSession().setAttribute(email,code);
            //将生成的验证码放入到redis中，并设置有效期为1分钟
            redisTemplate.opsForValue().set(email,code,1, TimeUnit.MINUTES);
            return R.success("发送成功");
        }
        return R.error("发送失败");
    }

    /**
     * 用户登录
     * @param map
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> map, HttpServletRequest request){
        //log.info("phone:{},code:{}",map.get("phone"),map.get("code"));
        String email = map.get("email");
        if(email!=null){
            //获取验证码
            //开始为session中获取
            //String code = (String) request.getSession().getAttribute(email);
            //改为从reids中获取
            String code = (String) redisTemplate.opsForValue().get(email);
            if(code!=null && code.equals(map.get("code"))){
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getEmail,email);
                User user = userService.getOne(queryWrapper);
                if(user==null){
                    user = new User();
                    user.setEmail(email);
                    String userName = ValidateCodeUtils.generateValidateCode4String(8);
                    user.setName(userName);
                    user.setStatus(1);
                    userService.save(user);
                }
                Long id = user.getId();
                request.getSession().setAttribute("user",id);
                return R.success(user);
            }
        }
        return R.error("登陆失败");
    }

    /**
     * 用户退出登录
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
