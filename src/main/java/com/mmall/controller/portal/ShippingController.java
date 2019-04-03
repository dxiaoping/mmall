package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    //增加收货地址
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session , Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return shippingService.add(user.getId(),shipping);
    }

    //删除收货地址
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del(HttpSession session , Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return shippingService.del(user.getId(),shippingId);
    }

    //登陆状态下更新收货地址
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session , Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return shippingService.update(user.getId(),shipping);
    }

    //查看具体地址
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session , Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return shippingService.select(user.getId(),shippingId);
    }

    //地址列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session , int pageNum,int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return shippingService.list(user.getId(),pageNum,pageSize);
    }
}
