package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author liming
 * @Date 2023/10/5 12:20
 **/

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
        //这个拦截器拦部分请求——后执行(order调为1)
        //登录拦截器
        registry.addInterceptor(new LoginInterceptor())
            //配置拦截哪些路径
            .excludePathPatterns(
                    "/shop/**",
                    "/voucher/**",
                    "/shop-type/**",
                    "/upload/**",
                    "/blog/hot",
                    "/user/code",
                    "/user/login"
            ).order(1);

        //token刷新拦截器
        //一般这些放行路径存在数据库中，方便管理
        //这个拦截器拦所有请求——先执行(order调为0)
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
