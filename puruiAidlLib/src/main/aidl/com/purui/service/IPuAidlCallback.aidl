// IPuAidlCallback.aidl
package com.purui.service;

// Declare any non-default types here with import statements

interface IPuAidlCallback {
    void setNoCamImage();
    void setCamPhoto(in Bitmap bitmap);
    void setCamPhotoBytes(in byte[] bytes);
}