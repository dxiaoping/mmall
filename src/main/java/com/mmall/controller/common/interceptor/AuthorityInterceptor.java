package com.mmall.controller.common.interceptor;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    //进入方法之前的处理逻辑
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            Object obj = entry.getValue();
            if (obj instanceof String[]){
                String[] strs = (String[])obj;
                mapValue = Arrays.toString(strs);

            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

//        if (StringUtils.equals(className,"UserManageController") && StringUtils.equals(methodName,"login")){
//            log.info("权限拦截器拦截请求。className:{},method:{}",className,methodName);
//            return true;
//        }
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr,User.class);
        }
        if (user == null || (user.getRole()).intValue() != Const.Role.ROLE_ADMIN){
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();

            if (user == null){
                out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
            }else {
                out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户非管理员")));
            }
            out.flush();
            out.close();
            return  false;
        }
        return true;
    }

    @Override

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
//    进入方法之后的处理逻辑
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
//        所有业务逻辑结束
        log.info("afterCompletion");
    }
}
