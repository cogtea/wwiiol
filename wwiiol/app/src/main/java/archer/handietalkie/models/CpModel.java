package archer.handietalkie.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CpModel implements Parcelable {

    public static final Creator<CpModel> CREATOR = new Creator<CpModel>() {
        @Override
        public CpModel createFromParcel(Parcel in) {
            return new CpModel(in);
        }

        @Override
        public CpModel[] newArray(int size) {
            return new CpModel[size];
        }
    };
    private int id;
    private int orig;
    private int type;
    private String name;
    private double ox;
    private double oy;
    private int controller;

    public CpModel() {

    }

    protected CpModel(Parcel in) {
        id = in.readInt();
        orig = in.readInt();
        type = in.readInt();
        name = in.readString();
        ox = in.readDouble();
        oy = in.readDouble();
        controller = in.readInt();
    }

    public static Creator<CpModel> getCreator() {
        return CREATOR;
    }

    public int getController() {
        return controller;
    }

    public void setController(int controller) {
        this.controller = controller;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(orig);
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeDouble(ox);
        dest.writeDouble(oy);
        dest.writeInt(controller);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getOx() {
        return ox;
    }

    public void setOx(double ox) {
        this.ox = ox;
    }

    public double getOy() {
        return oy;
    }

    public void setOy(double oy) {
        this.oy = oy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrig() {
        return orig;
    }

    public void setOrig(int orig) {
        this.orig = orig;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
