import cloud.hytora.context.ApplicationContext;
import cloud.hytora.context.IApplicationContext;

public class Test {


    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        IApplicationContext context = new ApplicationContext(this);

        Player player = context.getInstance(Player.class);



    }
}
