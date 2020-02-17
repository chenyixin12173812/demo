import java.lang.reflect.Field;

public class Test {

    public static void main(String[] args) {

        try {
            Class<?> son = Class.forName("com.cheniixin.Son");
            System.out.println("method"+son.getFields());
            Object sonObject = son.newInstance();

            for (Field f :son.getFields()){
                System.out.println("getField is " + f.getName());
            }
            for (Field f :son.getDeclaredFields()){
                System.out.println("getDeclaredField is " + f.getName());
            }

//            Field field = son.getDeclaredField("name");
//            field.setAccessible(true);
//            field.set(sonObject,"ccc");
//            System.out.println("name is" + field.get(sonObject));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
