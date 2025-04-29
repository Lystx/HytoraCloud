package cloud.hytora.modules.npc.spigot.entity.npc.types;

public enum ClickType {


    RIGHT,

    LEFT,

    DEFAULT;

    public static ClickType forName(String clickName) {
        if (clickName.startsWith("INTERACT")) {
            return RIGHT;
        } else if (clickName.startsWith("ATTACK")) {
            return LEFT;
        }
        return DEFAULT;
    }
}
