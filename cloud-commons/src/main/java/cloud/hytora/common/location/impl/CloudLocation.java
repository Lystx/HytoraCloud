package cloud.hytora.common.location.impl;

import cloud.hytora.document.Document;

public class CloudLocation extends CloudEntityLocation<Double, Float> {

    public CloudLocation(Document document) {
        this.fromDocument(document);
    }

    public CloudLocation(Double x, Double y, Double z, String world) {
        super(x, y, z, 0F, 0F, world);
    }
    public CloudLocation(double x, double y, double z, String world) {
        super(x, y, z, 0F, 0F, world);
    }

    public CloudLocation(Double x, Double y, Double z, Float yaw, Float pitch, String world) {
        super(x, y, z, yaw, pitch, world);
    }

    public CloudLocation(double x, double y, double z, float yaw, float pitch, String world) {
        super(x, y, z, yaw, pitch, world);
    }

    public Document toDocument() {
        return Document.gson()
                .set("x", this.x)
                .set("y", this.y)
                .set("z", this.z)
                .set("yaw", this.yaw)
                .set("pitch", this.pitch)
                .set("world", this.world);
    }


    public void fromDocument(Document document) {
        this.x = document.getDouble("x");
        this.y = document.getDouble("y");
        this.z = document.getDouble("z");
        this.yaw = document.getFloat("yaw");
        this.pitch = document.getFloat("pitch");
        this.world = document.getString("pitch");
    }
}
