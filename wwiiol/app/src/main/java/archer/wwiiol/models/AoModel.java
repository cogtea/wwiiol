package archer.wwiiol.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AoModel implements Parcelable {

    private String cpId;
    private int own;
    private String aoId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;


    public static Creator<AoModel> getCreator() {
        return CREATOR;
    }

    public AoModel() {

    }

    public AoModel(Parcel in) {
        cpId = in.readString();
        own = in.readInt();
        aoId = in.readString();
        name=in.readString();
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

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<AoModel> CREATOR = new Creator<AoModel>() {
        @Override
        public AoModel createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new AoModel(source);
        }

        @Override
        public AoModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return new AoModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cpId);
        dest.writeInt(own);
        dest.writeString(aoId);
        dest.writeString(name);
    }
}
