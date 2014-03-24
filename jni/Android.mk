LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../OpenCV-2.4.8-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := TopView
LOCAL_SRC_FILES := topview.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)



# second lib
#
include $(CLEAR_VARS)

LOCAL_MODULE    := Utis
LOCAL_SRC_FILES := utis_utils.cpp
LOCAL_LDLIBS +=  -llog -ldl
include $(BUILD_SHARED_LIBRARY)