package com.hmdp.utils;


import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取session
        HttpSession session = request.getSession();
        //2.获取session中的用户
        Object user = session.getAttribute("user");
        System.out.println(user + "拦截器里面的user");
        //3.判断用户是否存在
        if(user == null){
            //4.不存在，拦截:返回401状态码
            response.setStatus(401);
            return false;

        }

        //5.存在，保存用户信息到ThreadLocal
        //视频写的是User——后来改了
        UserHolder.saveUser((UserDTO) user);
        //6.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
