package cloud.hytora.modules.dashboard.intern.util;

public class WebPermission {

    private String permissionName;
    private PermissionType permissionType;

    public WebPermission(PermissionType value) {
        this.permissionType = value;
        this.permissionName = this.permissionType.name();
    }

    public static enum PermissionType {

        USER_OVERVIEW,
        USER_VIEW,
        USER_CREATE,
        USER_DELETE,
        USER_EDIT,
        USER_PASSWORD_RESET,

        SERVER_OVERVIEW,
        SERVER_VIEW,
        SERVER_START,
        SERVER_STOP,

        TEMPLATE_VIEW,
        TEMPLATE_CREATE,
        TEMPLATE_DELETE,
        TEMPLATE_EDIT,

        LOG_VIEW,


        ;

    }

}
