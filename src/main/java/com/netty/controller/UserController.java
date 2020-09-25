package com.netty.controller;

import com.netty.service.UserService;
import com.netty.util.SpringContextUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController {

    private UserService userService;

    /**
     * 以get请求显示 login success
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    @ResponseBody
    public Object login() {
        userService = SpringContextUtil.getBean("userServiceImpl");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", "200");
        map.put("message", "login success!");
        map.put("content", userService.hashCode());
        return map;
    }
}
