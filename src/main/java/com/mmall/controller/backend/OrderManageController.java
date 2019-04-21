package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IOrderService orderService;
    //管理员查看订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1")
            int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize,HttpServletRequest httpServletRequest){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return orderService.manageList(pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员，无权操作");
        }
    }

    //
    @RequestMapping("order_detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo,HttpServletRequest httpServletRequest){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return orderService.manageDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员，无权操作");
        }
    }

    //按订单号搜索
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo, @RequestParam(value = "pageNum",defaultValue = "1")
            int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize, HttpServletRequest httpServletRequest){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return orderService.manageSearch(orderNo,pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员，无权操作");
        }
    }

    //发货
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> ordsendGoods(HttpSession session,Long orderNo,HttpServletRequest httpServletRequest){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            return orderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员，无权操作");
        }
    }

}
