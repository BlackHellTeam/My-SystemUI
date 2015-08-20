LOCAL_PATH := $(call my-dir)
LIBRARY_PATH := $(LOCAL_PATH)/libs
SLIDINGUP_LIBRARY := $(LOCAL_PATH)/../slidingupLibrary
LOCAL_JAVA_LIBRARIES := services telephony-common lewa-framework com.mediatek.hotknot.sdk com.mediatek.hotknot
BUILD_LIBRARY := true
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(filter-out src/com/android/systemui/%, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/lewa/systemuiext/LewaPhoneStatusBar.java, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out src/com/lewa/systemuiext/dragdemo/DragDemoService.java, $(LOCAL_SRC_FILES))

LOCAL_STATIC_JAVA_LIBRARIES := slidingupLibrary mock lewa-support-v7-appcompat com.lewa.themes
#LOCAL_CERTIFICATE := platform
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.sothree.slidinguppanel.library 
LOCAL_AAPT_FLAGS += --extra-packages lewa.support.v7.appcompat 
LOCAL_RESOURCE_DIR := \
    vendor/lewa/apps/LewaSupportLib/actionbar_4.4/res \
    $(SLIDINGUP_LIBRARY)/res \
    $(LOCAL_PATH)/res
ifeq ($(BUILD_LIBRARY),true)
    LOCAL_MODULE := lewa-systemui-ext
    include $(BUILD_STATIC_JAVA_LIBRARY)
else
    LOCAL_PACKAGE_NAME := LewaSystemUIExt
    LOCAL_PRIVILEGED_MODULE := true
    include $(BUILD_PACKAGE)
endif


include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := mock:libs/droidMocks.jar
include $(BUILD_MULTI_PREBUILT)

include $(SLIDINGUP_LIBRARY)/Android.mk
#include $(CLEAR_VARS)
#include $(call all-makefiles-under,$(SLIDINGUP_LIBRARY))
