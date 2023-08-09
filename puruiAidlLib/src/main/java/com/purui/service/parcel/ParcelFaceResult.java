package com.purui.service.parcel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelFaceResult implements Parcelable {
    private Boolean success;
    private Bitmap face;
    private String name;

    public Boolean getSuccess() {
        return success;
    }
    public Bitmap getFace() {
        return face;
    }

    public String getName() {
        return name;
    }

    public ParcelFaceResult(Boolean success, Bitmap face, String name) {
        this.success = success;
        this.face = face;
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (success == null ? 0 : success ? 1 : 2));
        dest.writeParcelable(face, flags);
        dest.writeString(name);
    }
    protected ParcelFaceResult(Parcel in) {
        byte tmpSuccess = in.readByte();
        success = tmpSuccess == 0 ? null : tmpSuccess == 1;
        face = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
    }

    public static final Creator<ParcelFaceResult> CREATOR = new Creator<ParcelFaceResult>() {
        @Override
        public ParcelFaceResult createFromParcel(Parcel in) {
            return new ParcelFaceResult(in);
        }

        @Override
        public ParcelFaceResult[] newArray(int size) {
            return new ParcelFaceResult[size];
        }
    };
}
