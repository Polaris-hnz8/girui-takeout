package com.itcast.reggie.common;

//基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    /**
     * 以线程为作用域进行id的set操作
     * @param id
     */
    public static void setCurrentId(Long id) {//工具方法需要设置为static静态
        threadLocal.set(id);
    }

    /**
     * 以线程为作用域进行id的get操作
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
