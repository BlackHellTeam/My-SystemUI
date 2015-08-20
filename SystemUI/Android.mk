LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LEWA_SYSTEMUI_DIR := vendor/lewa/apps/LewaSystemUIExt

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/android/systemui/EventLogTags.logtags \
    $(call all-java-files-under, ../../../../vendor/lewa/apps/LewaSystemUIExt/src/) \
    $(call all-java-files-under, ../../../../vendor/lewa/apps/LewaSystemUIExt/v18/) \
    $(call all-java-files-under, ../../../../vendor/lewa/apps/LewaKeyguardExt/ext/lollipop/)

LOCAL_STATIC_JAVA_LIBRARIES := Keyguard lewa-systemui-ext lewa-keyguard-ext v4 lewa-support-v7-appcompat com.lewa.themes
LOCAL_JAVA_LIBRARIES := telephony-common lewa-framework

LOCAL_PACKAGE_NAME := SystemUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_RESOURCE_DIR := \
    frameworks/base/packages/Keyguard/res \
    vendor/lewa/apps/LewaSupportLib/actionbar_4.4/res \
    $(LEWA_SYSTEMUI_DIR)/v18res \
    $(LEWA_SYSTEMUI_DIR)/slidingupLibrary/res \
    $(LEWA_SYSTEMUI_DIR)/library/res \
    $(LEWA_SYSTEMUI_DIR)/overlay/statusbar \
    $(LOCAL_PATH)/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.keyguard \
    --extra-packages com.lewa.systemuiext \
    --extra-packages com.sothree.slidinguppanel.library \
    --extra-packages lewa.support.v7.appcompat 

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
include $(BUILD_MULTI_PREBUILT)
#LEWA BEGIN
include $(CLEAR_VARS)
include $(BUILD_MULTI_PREBUILT)
include $(CLEAR_VARS)
include $(LEWA_SYSTEMUI_DIR)/library/Android.mk
#include $(LEWA_SYSTEMUI_DIR)/RoundR/Android.mk
#LEWA END
