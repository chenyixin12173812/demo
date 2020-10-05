import com.cheniixin.Son;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Test {

    public static void main(String[] args) {

        try {

            //获得class 对象
            //方法1  或者static Class forName(String name, boolean initialize, ClassLoader loader)
            //Class<?> son = Class.forName("com.cheniixin.Son");
            //方法2
            //Class<?> son = Son.class;
            //方法3 通过对象
            Class<?> son = new Son("","").getClass();

             // 通过class获得 对象
            // 方法1 无参数
           // Object sonObject = son.newInstance();

            // 方法2 有参数
            //Constructor constructor = son.getConstructor(String.class,String.class);
            // 若构造器为私有
            //constructor.setAccessible(true);
            //Object sonObject = constructor.newInstance("111","22");



           //获得mehod 对象
            //方法1 获得可访问的所有方法mehod
            Method[] method1s = son.getMethods();
            //方法2 获得可访问某一方法mehod
            Method method2 = son.getMethod("setName");
           //方法3 获得声明的所有方法mehod
            Method[] method2s = son.getDeclaredMethods();
            //方法4 获得声明的某一mehod
            Method method3 = son.getDeclaredMethod("setName");




            // 获得字段
            // 方法1 获得可访问所有的字段
            for (Field f :son.getFields()){
                System.out.println("getField is " + f.getName());
            }

            // 方法2 通过名字可访问的获得某一字段
            son.getField("name");


            // 方法3 获得声明所有的字段
            for (Field f :son.getDeclaredFields()){
                System.out.println("getDeclaredField is " + f.getName());
            }
            // 方法4 获得声明某一的字段
            son.getDeclaredField("name");

//            Field field = son.getDeclaredField("name");
//            field.setAccessible(true);
//            field.set(sonObject,"ccc");
//            System.out.println("name is" + field.get(sonObject));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
