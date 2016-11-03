package archer.handietalkie.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ramy on 11/3/16.
 */

public class FacilityModel implements Parcelable {

    public static final Creator<FacilityModel> CREATOR = new Creator<FacilityModel>() {
        @Override
        public FacilityModel createFromParcel(Parcel in) {
            return new FacilityModel(in);
        }

        @Override
        public FacilityModel[] newArray(int size) {
            return new FacilityModel[size];
        }
    };
    private int id;
    private int ctry;
    private String side;
    private String open;
    private String name;

    protected FacilityModel(Parcel in) {
        id = in.readInt();
        ctry = in.readInt();
        side = in.readString();
        open = in.readString();
        name = in.readString();
    }

    public FacilityModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ctry);
        dest.writeString(side);
        dest.writeString(open);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCtry() {
        return ctry;
    }

    public void setCtry(int ctry) {
        this.ctry = ctry;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

}
