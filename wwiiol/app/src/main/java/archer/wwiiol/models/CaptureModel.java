package archer.wwiiol.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CaptureModel implements Parcelable {

    private int id;
    private long at;
    private String facilityId;
    private int from;
    private String by;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String date;

    public static Creator<CaptureModel> getCreator() {
        return CREATOR;
    }

    public CaptureModel() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAt() {
        return at;
    }

    public void setAt(long at) {
        this.at = at;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CaptureModel(Parcel in) {
        by = in.readString();
        at = in.readLong();
        id = in.readInt();
        facilityId = in.readString();
        from = in.readInt();
        name=in.readString();
        date=in.readString();
    }


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<CaptureModel> CREATOR = new Creator<CaptureModel>() {
        @Override
        public CaptureModel createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new CaptureModel(source);
        }

        @Override
        public CaptureModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return new CaptureModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(by);
        dest.writeLong(at);
        dest.writeInt(id);
        dest.writeString(facilityId);
        dest.writeInt(from);
        dest.writeString(name);
        dest.writeString(date);
    }
}
