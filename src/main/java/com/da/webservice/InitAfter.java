package com.da.webservice;

/**
 * 可以在实现了Handler的类中注册后做些操作
 */
public interface InitAfter {
    void after(WebApp app);
}
