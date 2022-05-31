package com.da.webservice;

/**
 * 可以在实现了Handler的类中注册前做些操作
 */
public interface InitBefore {
    void before(WebApp app);
}
