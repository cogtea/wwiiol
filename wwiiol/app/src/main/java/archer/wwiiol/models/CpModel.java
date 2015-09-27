package archer.wwiiol.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CpModel implements Parcelable {

    private int id;
    private int orig;
    private int type;
    private String name;


    public static Creator<CpModel> getCreator() {
        return CREATOR;
    }

    public CpModel() {

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

    public CpModel(Parcel in) {
        name = in.readString();
        id = in.readInt();
        type = in.readInt();
        orig = in.readInt();
    }


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(orig);
    }
}
