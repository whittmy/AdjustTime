LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_DEX_PREOPT := false
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_STATIC_JAVA_LIBRARIES  := j4zip
LOCAL_PACKAGE_NAME := AdjustTime
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)


#include $(CLEAR_VARS) 
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES  := j4zip:libs/zip4j_1.3.2.jar
#include $(BUILD_MULTI_PREBUILT)


# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
