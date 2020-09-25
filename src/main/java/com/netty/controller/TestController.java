//package com.netty.controller;
//
//import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
//import java.util.HashMap;
//import java.util.Map;
//import com.netty.annotation.Controller;
//import com.netty.annotation.CookieValue;
//import com.netty.annotation.RequestHeader;
//import com.netty.annotation.RequestMapping;
//import com.netty.annotation.RequestMethod;
//import com.netty.annotation.RequestParam;
//import com.netty.annotation.ResponseBody;
//import com.netty.annotation.Value;
//import com.netty.annotation.ext.Shakehands;
//import com.netty.request.HttpServletRequest;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
//import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
//
///**
// * http接口测试，这里写法和spring mvc几乎一致 测试之前，请先运行TestWebSocketServer启动服务器
// * @author zhanglei
// * @since 2020-09-25 14:55:00
// */
//@Controller
//@RequestMapping("test")
//public class TestController {
//
//    /**
//     * 以get请求显示 helloWorld
//     */
//    @RequestMapping(value = "hello", method = RequestMethod.GET)
//    @ResponseBody
//    public Object test() {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("code", "200");
//        map.put("msg", "get hello world");
//        return map;
//    }
//
//    /**
//     * 以post请求显示 helloWorld
//     */
//    @RequestMapping(value = "hello", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test2() {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("code", "200");
//        map.put("msg", "post hello world");
//        return map;
//    }
//
//    /**
//     * 测试HttpServletRequest
//     */
//    @RequestMapping(value = "request", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test3(HttpServletRequest request) {
//        String name = request.getParameter("name");
//        String age = request.getParameter("age");
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试@RequestParam
//     */
//    @RequestMapping(value = "param", method = RequestMethod.GET)
//    @ResponseBody
//    public Object test3(@RequestParam("name") String name, @RequestParam("age") int age) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试@RequestParam默认值
//     */
//    @RequestMapping(value = "param/default", method = RequestMethod.GET)
//    @ResponseBody
//    public Object test4(@RequestParam(value = "name", defaultValue = "lisi") String name,
//                        @RequestParam(value = "age", defaultValue = "20") Integer age) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试直接入参
//     */
//    @RequestMapping(value = "param", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test5(String name, int age) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试@CookieValue
//     */
//    @RequestMapping(value = "cookie", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test6(@CookieValue(value = "name") String name, @CookieValue(value = "age") int age) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试@RequestHeader
//     */
//    @RequestMapping(value = "header", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test7(@RequestHeader(value = "name") String name, @RequestHeader(value = "age") int age) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("name", name);
//        map.put("age", age);
//        return map;
//    }
//
//    /**
//     * 测试@Value
//     */
//    @RequestMapping(value = "value", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test8(@Value(value = "web.package") String pack) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("package", pack);
//        return map;
//    }
//
//    /**
//     * 测试@Shakehands
//     */
//    @RequestMapping(value = "ws", method = RequestMethod.GET)
//    @Shakehands
//    public void test9(HttpServletRequest req, final ChannelHandlerContext ctx) {
//        // Handshake
//        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req),
//                null, true);
//        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req.getNativeRequest());
//        if (handshaker == null) {
//            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
//        } else {
//            ChannelFuture future = handshaker.handshake(ctx.channel(), req.getNativeRequest());
//            future.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (!future.isSuccess()) {
//                        ctx.fireExceptionCaught(future.cause());
//                    } else {
//                        System.out.println("握手成功");
//                    }
//                }
//            });
//        }
//    }
//
//    private static String getWebSocketLocation(HttpServletRequest req) {
//        String location = req.getHeader(HOST.toString()) + req.getRequestURI();
//        return "ws://" + location;
//    }
//
//    /**
//     * 测试统一的异常处理器
//     */
//    @RequestMapping(value = "except", method = RequestMethod.POST)
//    @ResponseBody
//    public Object test9() throws Exception{
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("code","200");
//        map.get("age").toString();
//        return map;
//    }
//}
