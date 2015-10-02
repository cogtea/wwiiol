package archer.handietalkie.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CpModel implements Parcelable {

    public static final Creator<CpModel> CREATOR = new Creator<CpModel>() {
        @Override
        public CpModel createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new CpModel(source);
        }

        @Override
        public CpModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return new CpModel[size];
        }
    };
    private int id;
    private int orig;
    private int type;
    private String name;

    public CpModel() {

    }

    public CpModel(Parcel in) {
        name = in.readString();
        id = in.readInt();
        type = in.readInt();
        orig = in.readInt();
    }

    public static Creator<CpModel> getCreator() {
        return CREATOR;
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

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(orig);
    }
}
