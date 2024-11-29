package ru.fruzz.extazyy.misc.event;




import ru.fruzz.extazyy.misc.event.events.Event;
import ru.fruzz.extazyy.misc.event.types.Priority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {

    private static final Map<Class<? extends Event>, List<MethodData>> REGISTRY_MAP = new HashMap<>();
    private static final List<Method> LISTENER_METHODS = new CopyOnWriteArrayList<>();

    public static void addListener(Object object){
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (isMethodBadListener(method)) {
                continue;
            }
            register(method, object,true);
        }
    }

    public static void register(Object object) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (isMethodBad(method)) {
                continue;
            }
            register(method, object,false);
        }
    }

    public static void unregister(Object object) {
        for (final List<MethodData> dataList : REGISTRY_MAP.values()) {
            dataList.removeIf(data -> data.source().equals(object) && !data.isListener);
        }
        cleanMap(true);
    }

    private static void register(Method method, Object object,boolean isListener) {
        Class<? extends Event> eventClazz = (Class<? extends Event>) method.getParameterTypes()[0];
        if (isListener && !LISTENER_METHODS.contains(method)) {
            LISTENER_METHODS.add(method);
        }
        MethodData data;
        if (isListener){
            data = new MethodData(object, method, method.getAnnotation(EventListener.class).value(),true);
        }else {
            data = new MethodData(object, method, method.getAnnotation(EventHandler.class).value(), false);
        }


        if (!data.target().isAccessible()) {
            data.target().setAccessible(true);
        }

        if (REGISTRY_MAP.containsKey(eventClazz)) {
            if (!REGISTRY_MAP.get(eventClazz).contains(data)) {
                REGISTRY_MAP.get(eventClazz).add(data);
                sortListValue(eventClazz);
            }
        } else {
            REGISTRY_MAP.put(eventClazz,new CopyOnWriteArrayList<MethodData>() {
                {
                    add(data);
                }
            });
        }
    }

    public static void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();
        while (mapIterator.hasNext()) {
            if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    private static void sortListValue(Class<? extends Event> indexClass) {
        List<MethodData> sortedList = new CopyOnWriteArrayList<>();

        for (final byte priority : Priority.VALUE_ARRAY) {
            for (final MethodData data : REGISTRY_MAP.get(indexClass)) {
                if (data.priority() == priority) {
                    sortedList.add(data);
                }
            }
        }

        REGISTRY_MAP.put(indexClass, sortedList);
    }

    private static boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventHandler.class);
    }

    private static boolean isMethodBadListener(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventListener.class) || LISTENER_METHODS.contains(method);
    }
    public static Event call(Event event) {
        List<MethodData> dataList = REGISTRY_MAP.get(event.getClass());

        if (dataList != null) {
                for (final MethodData data : dataList) {
                    invoke(data, event);
                    if (event.isStopped()) {
                        break;
                    }
                }
        }

        return event;
    }

    private static void invoke(MethodData data, Event argument) {
        try {
            data.target().invoke(data.source(), argument);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
        }
    }

    private record MethodData(Object source, Method target, byte priority,boolean isListener) {
    }
}
