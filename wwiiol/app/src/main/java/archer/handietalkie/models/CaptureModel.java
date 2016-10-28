package archer.handietalkie.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CaptureModel implements Parcelable {

    public static final Creator<CaptureModel> CREATOR = new Creator<CaptureModel>() {
        @Override
        public CaptureModel createFromParcel(Parcel in) {
            return new CaptureModel(in);
        }

        @Override
        public CaptureModel[] newArray(int size) {
            return new CaptureModel[size];
        }
    };
    private int id;
    private long at;
    private String facilityId;
    private int from;
    private int to;
    private String by;
    private String name;
    private String date;

    public CaptureModel() {

    }

    protected CaptureModel(Parcel in) {
        id = in.readInt();
        at = in.readLong();
        facilityId = in.readString();
        from = in.readInt();
        to = in.readInt();
        by = in.readString();
        name = in.readString();
        date = in.readString();
    }

    public static Creator<CaptureModel> getCreator() {
        return CREATOR;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(at);
        dest.writeString(facilityId);
        dest.writeInt(from);
        dest.writeInt(to);
        dest.writeString(by);
        dest.writeString(name);
        dest.writeString(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}
