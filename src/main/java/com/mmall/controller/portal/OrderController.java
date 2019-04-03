package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService orderService;

    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return orderService.create(user.getId(),shippingId);
    }

    //取消订单
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return orderService.cancel(user.getId(),orderNo);
    }

    //获取购物车中已经选中的商品详情
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return orderService.getOrderCartProduct(user.getId());
    }

    //获取订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return orderService.getOrderDetail(user.getId(),orderNo);
    }

    //用户查看订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1")
            int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        return orderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pat(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(orderNo,user.getId(),path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    //支付回调
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for (Iterator iterator = requestParams.keySet().iterator();iterator.hasNext();){
            String name = (String) iterator.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0;i < values.length;i++){
                valueStr = (i == values.length - 1)?valueStr + values[i]:valueStr + values[i] +",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调：sign:{},trade_status:{},参数：{}",params.get("sign"),params.get("trade_status"),params.toString());

        //验证回调正确性是否是支付宝发的，避免重复通知
        params.remove("sign_type");
        try {
            boolean alipaySignature = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!alipaySignature){
                return ServerResponse.createByErrorMessage("非法请求，验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常",e);

        }

        //TODO 验证数据

        ServerResponse serverResponse = orderService.aliCallback(params);
        if (serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //付款成功后的订单指引（查询支付是否成功/支付状态）
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登陆");
        }
        ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(),orderNo);
        if (serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
