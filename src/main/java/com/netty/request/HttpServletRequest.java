package com.netty.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * httpServlet请求实现处理
 */
@Slf4j
public class HttpServletRequest implements WebRequest {
    private final FullHttpRequest req;
    private Map<String, List<String>> cacheGetParams;// 缓存本次Get请求的所有参数
    private List<InterfaceHttpData> paramList;// 缓存本次post请求参数
    private Set<Cookie> cookies;// 缓存本次请求所有cookie

    public HttpServletRequest(FullHttpRequest req) {
        super();
        this.req = req;
    }

    @Override
    public String getHeader(String headerName) {
        HttpHeaders headers = this.req.headers();
        return headers.get(headerName);
    }

    @Override
    public String[] getHeaderValues(String headerName) {
        List<String> res = new ArrayList<String>();
        HttpHeaders headers = this.req.headers();
        for (Map.Entry<String, String> e : headers) {
            if (e.getKey().equals(headerName)) {
                res.add(e.getValue());
            }
        }
        return (String[]) res.toArray(new String[res.size()]);
    }

    @Override
    public String getParameter(String paramName) {
        try {
            Map<String, List<String>> getParams = getCacheParams();
            String value = null;
            if (getParams != null && getParams.containsKey(paramName)) {
                value = getParams.get(paramName).get(0);
            }
            if (value == null && this.getHttpMethod() == RequestMethod.POST) {
                List<InterfaceHttpData> paramList = this.getParamList();
                for (InterfaceHttpData parm : paramList) {
                    Attribute data = (Attribute) parm;
                    if (data.getName().equals(paramName)) {
                        value = data == null ? null : data.getValue();
                        break;
                    }
                }
            }
            return value;
        } catch (IOException e) {
            log.error("HttpServletRequest中getParameter方法出错：" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String[] getParameterValues(String paramName) {
        try {
            Map<String, List<String>> getParams = getCacheParams();
            List<String> res = null;
            if (getParams != null) {// get参数
                res = getParams.get(paramName);//
            }
            if (res == null) {
                res = new ArrayList<String>();
            }
            if (this.getHttpMethod() == RequestMethod.POST) {
                res = new ArrayList<String>(res);// 将Arrays$ArrayList转成ArrayList
                // post参数
                List<InterfaceHttpData> paramList = this.getParamList();
                for (InterfaceHttpData parm : paramList) {
                    Attribute data = (Attribute) parm;
                    if (data.getName().equals(paramName)) {
                        res.add(data == null ? null : data.getValue());
                    }
                }
            }
            return (String[]) res.toArray(new String[res.size()]);
        } catch (IOException e) {
            log.error("HttpServletRequest中getParameterValues方法出错：" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        try {
            Map<String, List<String>> params = getCacheParams();// get参数
            if (params == null) {
                params = new HashMap<String, List<String>>();
            }
            String key = null;
            if (this.getHttpMethod() == RequestMethod.POST) {
                params = new HashMap<String, List<String>>(params);// 防止post参数污染get参数
                // post参数
                List<InterfaceHttpData> parmList = this.getParamList();
                for (InterfaceHttpData parm : parmList) {
                    Attribute data = (Attribute) parm;
                    key = data.getName();
                    if (params.containsKey(key)) {
                        params.get(key).add(data.getValue());
                    } else {
                        params.put(key, Arrays.asList(data.getValue()));
                    }
                }
            }
            Map<String, String[]> returnMap = new HashMap<String, String[]>();
            Iterator<String> it = params.keySet().iterator();
            List<String> list = null;
            while (it.hasNext()) {
                key = it.next();
                list = params.get(key);
                returnMap.put(key, (String[]) list.toArray(new String[list.size()]));
            }
            return returnMap;
        } catch (IOException e) {
            log.error("HttpServletRequest中getParameterMap方法出错：" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public RequestMethod getHttpMethod() {
        HttpMethod method = this.req.method();
        if (method == HttpMethod.GET) {
            return RequestMethod.GET;
        }
        return RequestMethod.POST;
    }

    private Map<String, List<String>> getCacheParams() {
        if (cacheGetParams == null) {
            QueryStringDecoder decoder = new QueryStringDecoder(this.req.uri());
            cacheGetParams = decoder.parameters();
        }
        return cacheGetParams;
    }

    // 只能在post请求中调用该方法
    public List<InterfaceHttpData> getParamList() {
        if (paramList == null) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
            paramList = decoder.getBodyHttpDatas();
        }
        return paramList;
    }

    private Set<Cookie> getCookies() {
        String cookieString = this.req.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString == null) {
            return null;
        }
        cookies = ServerCookieDecoder.LAX.decode(cookieString);
        return cookies;
    }

    @Override
    public String getCookie(String cookieName) {
        Set<Cookie> cookies = this.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        Iterator<Cookie> it = cookies.iterator();
        Cookie cookie = null;
        while (it.hasNext()) {
            cookie = it.next();
            if (cookie.name().equals(cookieName)) {
                return cookie.value();
            }
        }
        return null;
    }

    @Override
    public FullHttpRequest getNativeRequest() {
        return this.req;
    }

    @Override
    public String getMethod() {
        return this.req.method().name();
    }

    @Override
    public String getRequestURI() {
        String uri = this.req.uri();
        int index = uri.lastIndexOf("?");
        if (index > 0) {
            uri = uri.substring(0, index);// 去掉uri中的请求参数
        }
        return uri;
    }

}
