package com.hmdp.service.impl;



import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        //4.保存验证码到session——写法1：不好
        //session.setAttribute("code",code);
        //4.保存验证码到redis——写法2：好
        //设置了2分钟有效期
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
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
        // 这个应该前端去做，发送验证码之后，手机号就不允许重新输入了——使用redis问题解决了
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3.校验验证码
        //方法1：从session获取
        //Object cacheCode = session.getAttribute("code");
        //方法2：从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)){
        //if(cacheCode == null || !cacheCode.toString().equals(code)){
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
        //7.存在，保存用户信息到session中——只存基本信息,user里面的信息太多了——方法1
        //只给UserDTO即可
        //session.setAttribute("user",user);

        //7.存在，保存用户信息到redis中——方法2
        //7.1 随机生成token 作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //7.2 将User对象转为hashMap存储(redis中存储的就是hashMap)
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //将 user 对象的属性复制到 userDTO 对象中。
        //session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        //Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        String tokenKey = LOGIN_USER_KEY + token;
        //7.3 存储
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //token只是当作key在redis中进行保存
        //7.4设置token有效期
        //当前效果：登录以后开始算起，只要过了有效期(如30min)，不管你访没访问，redis都会把你踢出......
        //我们应该像session那样，只要用户不断地访问我，我就不断更新redis中token的有效期——每访问一次，更新一次TTL
        //问题：我怎么知道用户什么时候访问了我——登录拦截校验
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);

        //8.返回token
        return Result.ok(token);
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
