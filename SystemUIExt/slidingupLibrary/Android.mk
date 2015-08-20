LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := v4
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_CERTIFICATE := platform
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_MODULE := slidingupLibrary
#LOCAL_MANIFEST_FILE := AndroidManifest.xml
#LOCAL_EXPORT_PACKAGE_RESOURCES := true 
LOCAL_JAR_EXCLUDE_FILES := none 
include $(BUILD_STATIC_JAVA_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    v4:libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)