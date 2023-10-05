package com.hmdp.service.impl;



import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    /**
     * 发送手机验证码
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存验证码到session
        session.setAttribute("code",code);
        //5.发送验证码——暂时先不做了，要调第三方平台,使用日志假设调用成功了
        //log.info("发送短信验证码成功，验证码：{}",code);
        System.out.println("发送短信验证码成功，验证码为" + code);
        //返回OK
        return Result.ok();
    }


    /**
     * 实现登录功能
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号【格式】
        // (如何防止你发短信的时候手机号填正确的，登录的时候，把手机号给改了)
        // 这个应该前端去做，发送验证码之后，手机号就不允许重新输入了
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //2.校验验证码
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.toString().equals(code)){
            //cacheCode == null表示你从来没发过验证码，或者验证码过期
            //cacheCode.toString().equals(code)表示验证码对不上，输错了
            //3.不一致，报错
            return Result.fail("验证码错误");
        }

        //4.一致，根据手机号查用户
        User user = query().eq("phone", phone).one();
        //5.判断用户是否存在
        if(user == null){
            //6.不存在，创建新用户并保存(重头凭空创造)
            //创建完用户，也要返回，把信息也要保存在session中
            user = createUserWithPhone(phone);
        }
        //7.存在，保存用户信息到session中——只存基本信息,user里面的信息太多了
        //只给UserDTO即可
        //session.setAttribute("user",user);
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        //创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //保存用户
        save(user);
        return user;
    }
}
