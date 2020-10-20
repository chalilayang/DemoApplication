#include <jni.h>
#include <string>
#include <android/bitmap.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mi_demoapplication_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jintArray JNICALL Java_com_example_mi_demoapplication_RenderScriptActivity_imgToGray(
        JNIEnv *env, jobject obj, jintArray buf, int w, int h) {
    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (cbuf == NULL) {
        return 0;
    }
    int alpha = 0xFF < 24;
    for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
            cbuf[w*i+j] = 1;
        }
    }
    int size = w*h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result,0,size,cbuf);
    env->ReleaseIntArrayElements(buf,cbuf,0);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_example_mi_demoapplication_RenderScriptActivity_compareByte(
        JNIEnv *env, jobject obj, jbyteArray buf1, jbyteArray buf2, int w, int h) {
    jbyte* cbuf;
    cbuf = env->GetByteArrayElements(buf1, JNI_FALSE);
    jbyte* cbuf2;
    cbuf2 = env->GetByteArrayElements(buf2, JNI_FALSE);
    if (cbuf == NULL) {
        return 0;
    }
    jint same = 1;
    for (int i = 0; i < h; i = i + 1) {
        jbyte* line1 = cbuf + i * w;
        jbyte* line2 = cbuf2 + i * w;
        if (memcmp(line1, line2, w) != 0) {
            same = 0;
            break;
        }
    }
    env->ReleaseByteArrayElements(buf1, cbuf,0);
    env->ReleaseByteArrayElements(buf2, cbuf2,0);
    return same;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_example_mi_demoapplication_RenderScriptActivity_nativeCompareBitmap
        (JNIEnv *env, jobject, jobject bitmap, jobject bitmap2) {
    AndroidBitmapInfo bitmapInfo;
    if ((AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        return 0;
    }
    void *bitmapPixels;
    if ((AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        return 0;
    }
    AndroidBitmapInfo bitmapInfo2;
    if ((AndroidBitmap_getInfo(env, bitmap2, &bitmapInfo2)) < 0) {
        return 0;
    }
    void *bitmapPixels2;
    if ((AndroidBitmap_lockPixels(env, bitmap2, &bitmapPixels2)) < 0) {
        return 0;
    }
    uint32_t newWidth = bitmapInfo.height;
    uint32_t newHeight = bitmapInfo.width;
    uint32_t newWidth2 = bitmapInfo2.height;
    uint32_t newHeight2 = bitmapInfo2.width;
    uint32_t stride = bitmapInfo.stride;
    uint32_t stride2 = bitmapInfo2.stride;
    if (newHeight != newHeight2 || newWidth != newWidth2) {
        return 0;
    }
    jint same = 1;
    for (int i = 0; i < newHeight; i = i + 1) {
        jbyte* line1 = (jbyte*) bitmapPixels + i * stride;
        jbyte* line2 = (jbyte*) bitmapPixels2 + i * stride2;
        if (memcmp(line1, line2, newWidth) != 0) {
            same = 0;
            break;
        }
    }
    return same;
}
