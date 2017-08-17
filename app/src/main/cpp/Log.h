//
// Created by ksi-android on 2016/4/7.
//

#include "android/log.h"
#include "jni.h"

#ifndef LOG_TAG
#define LOG_TAG "JNI"
#endif
#ifndef IS_DEBUG
#define IS_DEBUG true
#endif
#define LOG_NOOP (void) 0

#define LOG_PRINT(level, fmt, ...) __android_log_print(level,LOG_TAG,"(%s:%u): " fmt,__FILE__,__LINE__,##__VA_ARGS__)
#if IS_DEBUG
#define LOGI(fmt, ...) LOG_PRINT(ANDROID_LOG_INFO,fmt,##__VA_ARGS__)
#else
#define LOGI(...) LOG_NOOP
#endif

#if IS_DEBUG
#define LOGW(fmt, ...) LOG_PRINT(ANDROID_LOG_WARN,fmt ,##__VA_ARGS__)
#else
#define LOGW(...) LOG_NOOP
#endif

#if IS_DEBUG
#define LOGD(fmt, ...) LOG_PRINT(ANDROID_LOG_DEBUG,fmt ,##__VA_ARGS__)
#else
#define LOGD(...) LOG_NOOP
#endif

#if IS_DEBUG
#define LOGE(fmt, ...) LOG_PRINT(ANDROID_LOG_ERROR,fmt ,##__VA_ARGS__)
#else
#define LOGE(...) LOG_NOOP
#endif

#if IS_DEBUG
#define LOGF(fmt, ...) LOG_PRINT(ANDROID_LOG_FATAL,fmt ,##__VA_ARGS__)
#else
#define LOGF(...) LOG_NOOP
#endif

static void ThrowRuntimeExcption(JNIEnv *env, const char *msg) {
    jclass exceptionClazz = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(exceptionClazz, msg);
}


