package com.hmdp.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * @Author liming
 * @Date 2023/10/5 11:07
 **/

//把用户信息存储在session中，则需要在跨各种处理组件或页面时都要从session中去取得用户信息，
// 这增加了访问session的开销，所以我们这里用ThreadLocal
//threadlocal防止并发，他存储在每个线程内，跟其他线程隔离

//没有提示重写哪些方法的，可以使用Ctrl+O快捷键

/**
 * 在Java中，当一个类实现（implements）一个接口时，该类必须实现接口中声明的所有方法。
 * 接口中声明的方法都是抽象方法，因此必须在实现类中对它们进行实现（重写）。
 *
 * 如果一个类实现了多个接口，那么它必须实现所有接口中声明的方法。
 * 如果多个接口中有相同的方法签名，那么实现类只需要实现一次该方法即可。
 *
 * 需要注意的是，接口中只能包含抽象方法（没有方法体的方法），因此实现类必须对所有抽象方法进行实现，
 * 否则该类必须声明为抽象类。另外，接口中可以包含默认方法（有方法体的方法），实现类可以选择是否重写默认方法，如果不重写，则继承接口的默认实现。
 */
public class LoginInterceptor implements HandlerInterceptor {
    ////这里的注入不能使用@AutoWired,@Resource等注入，因为这个类是我们手动new出来的,不是通过@Component注解构建的
    //
    ////可以给这个类手动加一个@Component嘛？
    ////回答：不能加入spring容器 拦截器是在spring容器初始化之前执行的
    ////拦截器是在spring容器初始之前执行的，加什么Component注解都没用！！
    //private StringRedisTemplate stringRedisTemplate;

    //public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
    //    this.stringRedisTemplate = stringRedisTemplate;
    //}

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //之前的拦截器做完了，下面的事情就不用做了
        ////1.获取session——方法1
        ////HttpSession session = request.getSession();
        //
        ////1.获取请求头中的token——方法2
        ////上节课讲的，前端里面把token放到请求头里面的。。。。。。。
        ////前端放在请求头中的anthorization了啊 之前看了
        ////前端axios里每次请求把token添加到请求头里
        //String token = request.getHeader("authorization");
        //if(StrUtil.isBlank(token)){
        //    //token不存在，拦截，返回401状态码
        //    response.setStatus(401);
        //    return false;
        //}
        ////2.获取session中的用户——方法1
        ////Object user = session.getAttribute("user");
        ////2.基于token获取redis中的用户——方法2
        //String key = LOGIN_USER_KEY + token;
        //Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        ////System.out.println(user + "拦截器里面的user");
        //System.out.println(userMap + "拦截器里面的user");
        ////3.判断用户是否存在
        ////if(user == null){
        //if(userMap.isEmpty()){
        //    //4.不存在，拦截:返回401状态码
        //    response.setStatus(401);
        //    return false;
        //}
        //
        ////4.将查询到的Hash数据转回UserDTO对象
        ////false表示不忽略转换过程中的错误
        //UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //
        ////5.存在，保存用户信息到ThreadLocal
        ////视频写的是User——后来改了
        ////UserHolder.saveUser((UserDTO) user);
        //UserHolder.saveUser((UserDTO) userDTO);
        //
        ////6.刷新token有效期——只要用户不断地访问我，我就不断更新redis中token的有效期——每访问一次，更新一次TTL
        ////不然的话：登录以后开始算起，只要过了有效期(如30min)，不管你访没访问，redis都会把你踢出......
        //stringRedisTemplate.expire(key,LOGIN_USER_TTL, TimeUnit.MINUTES);
        ////7.放行

        //1.判断是否需要拦截(ThreadLocal中是否有用户)
        //果然 从登录拦截器的名字LoginInterceptor就能看出  其实人家只需要做一件事  就是判断线程中有没有用户就可以了  其他事情交给其他类做
        if(UserHolder.getUser() == null){
            //没有，需要拦截
            response.setStatus(401);
            //拦截
            return false;
        }
        //有用户，则放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
