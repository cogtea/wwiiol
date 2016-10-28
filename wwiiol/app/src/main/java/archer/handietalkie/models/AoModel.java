package archer.handietalkie.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AoModel implements Parcelable {

    public static final Creator<AoModel> CREATOR = new Creator<AoModel>() {
        @Override
        public AoModel createFromParcel(Parcel in) {
            return new AoModel(in);
        }

        @Override
        public AoModel[] newArray(int size) {
            return new AoModel[size];
        }
    };
    private String cpId;
    private int own;
    private int side;
    private String aoId;
    private String name;


    public AoModel() {

    }

    protected AoModel(Parcel in) {
        cpId = in.readString();
        own = in.readInt();
        side = in.readInt();
        aoId = in.readString();
        name = in.readString();
    }

    public static Creator<AoModel> getCreator() {
        return CREATOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cpId);
        dest.writeInt(own);
        dest.writeInt(side);
        dest.writeString(aoId);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpId() {
        return cpId;
    }

    public void setCpId(String cpId) {
        this.cpId = cpId;
    }

    public int getOwn() {
        return own;
    }

    public void setOwn(int own) {
        this.own = own;
    }

    public String getAoId() {
        return aoId;
    }

    public void setAoId(String aoId) {
        this.aoId = aoId;
    }

}
