package com.dongdong.animal.spider;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpiderBus {

    static volatile SpiderBus defaultInstance;

    //这样设计有助于注销时候清除

    private Map<Class<?>, Map<Class<?>, Set<String>>> AllSubInfo;
    //存储事件类型为key，按注册对象为key，所有此事件相关方法等set
    private Map<Class<?>, Object> AllRegistObj;  //存储所有注册对象


    private Handler mainHandler;

    public static SpiderBus getInstance() {
        if (defaultInstance == null) {
            synchronized (SpiderBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new SpiderBus();
                }
            }
        }
        return defaultInstance;
    }

    private SpiderBus() {
        if (AllSubInfo != null) {
            AllSubInfo.clear();
        } else {
            AllSubInfo = new HashMap<>();
        }

        if (AllRegistObj != null) {
            AllRegistObj.clear();
        } else {
            AllRegistObj = new HashMap<>();
        }

        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setMsg(msg.obj);
            }
        };
    }


    public void register(Object obj) {
        if (obj == null) {
            throw new NullPointerException("Object to register must not be null.");
        }
        AllRegistObj.put(obj.getClass(), obj);
        Map<Class<?>, List<SpiderEntity>> map = getListenerList(obj);
        bindRegister(map);


    }


    public void unregister(Object obj) {

        if (obj == null) {
            throw new NullPointerException("Object to unregister must not be null.");
        }

        AllRegistObj.remove(obj.getClass());
        Class<?> listener = obj.getClass();
        for (Class<?> event : AllSubInfo.keySet()) {
            AllSubInfo.get(event).remove(listener);
        }

    }

    public void post(Object event) {

        if (event == null) {
            throw new NullPointerException("Object to event must not be null.");
        }

        if (Looper.myLooper() != Looper.getMainLooper()) {
            Message msg = mainHandler.obtainMessage();
            msg.obj = event;
            mainHandler.sendMessage(msg);

        } else {


            setMsg(event);


        }


    }


    /**
     * 获取监听列表
     *
     * @param obj 注册对象
     * @return
     */
    private Map<Class<?>, List<SpiderEntity>> getListenerList(Object obj) {
        Map<Class<?>, List<SpiderEntity>> map = new HashMap<>();
        List<SpiderEntity> list = new ArrayList<>();
        Class<?> classz = obj.getClass();
        Method[] methods = classz.getMethods();
        if (methods.length == 0) {
            return map;
        }
        for (Method m : methods) {
            if (m.isBridge()) {
                continue;
            }

            if (m.isAnnotationPresent(Subscribe.class)) {

                SpiderEntity entity = new SpiderEntity();
                entity.setClassz(classz.getName());
                entity.setMldule(m.getName());
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + m + " has @Subscribe " +
                            "annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }
                Class<?> eventType = parameterTypes[0];
                if (eventType.isInterface()) {
                    throw new IllegalArgumentException("Method " + m + " has @Subscribe annotation on " + eventType
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                entity.setEvent(eventType);
                list.add(entity);
            }


        }
        map.put(classz, list);
        return map;

    }


    /**
     * 缓存注册信息
     *
     * @param map
     */
    private void bindRegister(Map<Class<?>, List<SpiderEntity>> map) {

        for (Class<?> listenerClsaa : map.keySet()) {
            List<SpiderEntity> entityList = map.get(listenerClsaa);
            for (SpiderEntity entity : entityList) {
                Map<Class<?>, Set<String>> mapMethod = new HashMap<>();
                Class<?> event = entity.getEvent();
                if (AllSubInfo.containsKey(event)) {
                    mapMethod.putAll(AllSubInfo.get(event));
                }
                Set<String> methodSet = new HashSet<>();
                if (mapMethod.containsKey(listenerClsaa)) {
                    methodSet.addAll(mapMethod.get(listenerClsaa));
                }
                methodSet.add(entity.getMldule());
                mapMethod.put(listenerClsaa, methodSet);
                AllSubInfo.put(event, mapMethod);
            }

        }

    }


    /**
     * 消息下发操作
     *
     * @param event
     */
    private void setMsg(Object event) {

        Class<?> clazz = event.getClass();
        if (AllSubInfo.containsKey(clazz)) {
            Map<Class<?>, Set<String>> map = new HashMap<>();
            map.putAll(AllSubInfo.get(clazz));
            for (Class<?> obj : map.keySet()) {
                Set<String> methods = map.get(obj);
                for (String method : methods) {
                    try {
                        Method m = obj.getMethod(method, event.getClass());
                        m.invoke(AllRegistObj.get(obj), event);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }


}
