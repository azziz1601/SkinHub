package zeroends.skinhub.model;

public class SkinData {

    private final int id;
    private final String name;
    private final String value;
    private final String signature;

    public SkinData(int id, String name, String value, String signature) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }
}