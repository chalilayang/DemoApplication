#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>

#define TAG "BitmapUtils-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

extern "C"
jdouble getDiffRate(
        jbyte* bitmapPixelsPre, jint startYPre, jint stridePre,
        jbyte* bitmapPixelsBack, jint startYBack, jint strideBack,
        jint startX, jint width, jint height, jfloat threshold, jint sampleSize) {
    long thresholdValue = height * width * threshold;
    long sumNotSame = 0;
    if (sampleSize < 1) {
        sampleSize = 1;
    }
    for (int lineIndex = 0; lineIndex < height; lineIndex = lineIndex + sampleSize) {
        jint linePre = startYPre + lineIndex;
        jint lineBack = startYBack + lineIndex;
        jint* linePrePixels = (jint *)(bitmapPixelsPre + linePre * stridePre);
        jint* lineBackPixels = (jint *)(bitmapPixelsBack + lineBack * strideBack);
        for (int col = startX, count = startX + width; col < count; col = col + sampleSize) {
            jint pixelPre = *(linePrePixels + col);
            jint pixelBack = *(lineBackPixels + col);
            if (pixelPre != pixelBack) {
                sumNotSame = sumNotSame + sampleSize;
                if (thresholdValue + 1 <= sumNotSame) {
                    break;
                }
            }
        }
        if (thresholdValue + 1 <= sumNotSame) {
            break;
        }
    }
    return sumNotSame * 1.0 / (height * width);
}

extern "C"
JNIEXPORT jintArray JNICALL Java_com_miui_screenshot_BitmapUtils_imgToGray(
        JNIEnv *env, jclass obj, jintArray buf, int w, int h) {
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
JNIEXPORT jint JNICALL Java_com_miui_screenshot_BitmapUtils_compareByte(
        JNIEnv *env, jclass obj, jbyteArray buf1, jbyteArray buf2, int w, int h) {
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
JNIEXPORT jint JNICALL Java_com_miui_screenshot_BitmapUtils_nativeCompareBitmapRange
        (JNIEnv *env, jclass bitmapUtilsClass,
                jobject bitmap, jint top, jint bottom,
                jobject bitmap2, jint top2, jint bottom2,
                jint step) {
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
        AndroidBitmap_unlockPixels(env, bitmap);
        return 0;
    }
    void *bitmapPixels2;
    if ((AndroidBitmap_lockPixels(env, bitmap2, &bitmapPixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return 0;
    }
    jint newWidth = bitmapInfo.width;
    jint newHeight = bitmapInfo.height;
    jint newWidth2 = bitmapInfo2.width;
    jint newHeight2 = bitmapInfo2.height;
    jint stride = bitmapInfo.stride;
    jint stride2 = bitmapInfo2.stride;
    if (newHeight != newHeight2 || newWidth != newWidth2) {
        LOGE("nativeCompareBitmapRange width not same");
        AndroidBitmap_unlockPixels(env, bitmap);
        AndroidBitmap_unlockPixels(env, bitmap2);
        return 0;
    }
    if ((bottom - top) != (bottom2 - top2)) {
        LOGE("nativeCompareBitmapRange range not same");
        AndroidBitmap_unlockPixels(env, bitmap);
        AndroidBitmap_unlockPixels(env, bitmap2);
        return 0;
    }
    jint same = 1;
    jint count = bottom - top;
    step = step <= 0 ? 1 : step;
    for(int index = 0; index <= count; index = index + step) {
        jint line1 = top + index;
        jint line2 = top2 + index;
        jbyte* line1Bytes = (jbyte*) bitmapPixels + line1 * stride;
        jbyte* line2Bytes = (jbyte*) bitmapPixels2 + line2 * stride2;
        if (memcmp(line1Bytes, line2Bytes, newWidth) != 0) {
            same = 0;
            break;
        }
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    AndroidBitmap_unlockPixels(env, bitmap2);
    return same;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_miui_screenshot_BitmapUtils_nativeCompareBitmap
        (JNIEnv *env, jclass bitmapUtilClass,
                jobject bitmapPre, jobject bitmapBack, jfloat threshold) {
    AndroidBitmapInfo bitmapInfo;
    if ((AndroidBitmap_getInfo(env, bitmapPre, &bitmapInfo)) < 0) {
        return 0;
    }
    void *bitmapPixels;
    if ((AndroidBitmap_lockPixels(env, bitmapPre, &bitmapPixels)) < 0) {
        return 0;
    }
    AndroidBitmapInfo bitmapInfo2;
    if ((AndroidBitmap_getInfo(env, bitmapBack, &bitmapInfo2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return 0;
    }
    void *bitmapPixels2;
    if ((AndroidBitmap_lockPixels(env, bitmapBack, &bitmapPixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return 0;
    }
    jint newWidth = bitmapInfo.width;
    jint newHeight = bitmapInfo.height;
    jint newWidth2 = bitmapInfo2.width;
    jint newHeight2 = bitmapInfo2.height;
    jint stride = bitmapInfo.stride;
    jint stride2 = bitmapInfo2.stride;
    jint cc = newWidth * threshold;
    LOGE("nativeCompareBitmapRange stride %d, threshold %d ", stride2, threshold);
    if (newHeight != newHeight2 || newWidth != newWidth2) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        AndroidBitmap_unlockPixels(env, bitmapBack);
        return 0;
    }
    jint startOffset = -1;
    for (jint lineBack = newHeight - 1; lineBack >= 0; lineBack --) {
        jint lineBottomPre = newHeight - 1;
        jint lineTopPre = lineBottomPre - lineBack;
        jint lineBottomBack = lineBack;
        jint lineTopBack = 0;
        jint count = lineBottomBack - lineTopBack;
        jint same = 1;
        for(int index = 0; index <= count; index = index + 3) {
            jint line1 = lineTopPre + index;
            jint line2 = lineTopBack + index;
            jint* line1Bytes = (jint *)((jbyte*) bitmapPixels + line1 * stride);
            jint* line2Bytes = (jint *)((jbyte*) bitmapPixels2 + line2 * stride2);
            int step = 0;
            jint notSame = 0;
            while (step < newWidth) {
                jint pixel1 = *(line1Bytes + step);
                jint pixel2 = *(line2Bytes + step);
                if (pixel1 != pixel2) {
                    notSame ++;
                    if (notSame > cc) {
                        break;
                    }
                }
                step ++;
            }
            if (notSame > cc) {
                same = 0;
                break;
            }
//            if (memcmp(line1Bytes, line2Bytes, newWidth) != 0) {
//                same = 0;
//                break;
//            }
        }
        if (same == 1) {
            startOffset = lineBack;
            break;
        }
    }
    AndroidBitmap_unlockPixels(env, bitmapPre);
    AndroidBitmap_unlockPixels(env, bitmapBack);
    return newHeight - startOffset - 1;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_miui_screenshot_BitmapUtils_nativeCompareBitmapWithSimilarity
        (JNIEnv *env, jclass bitmapUtilClass,
         jobject bitmapPre, jobject bitmapBack, jfloat threshold) {
    AndroidBitmapInfo bitmapInfo;
    if ((AndroidBitmap_getInfo(env, bitmapPre, &bitmapInfo)) < 0) {
        return 0;
    }
    void *bitmapPixels;
    if ((AndroidBitmap_lockPixels(env, bitmapPre, &bitmapPixels)) < 0) {
        return 0;
    }
    AndroidBitmapInfo bitmapInfo2;
    if ((AndroidBitmap_getInfo(env, bitmapBack, &bitmapInfo2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return 0;
    }
    void *bitmapPixels2;
    if ((AndroidBitmap_lockPixels(env, bitmapBack, &bitmapPixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return 0;
    }
    jint newWidth = bitmapInfo.width;
    jint newHeight = bitmapInfo.height;
    jint newWidth2 = bitmapInfo2.width;
    jint newHeight2 = bitmapInfo2.height;
    jint stride = bitmapInfo.stride;
    jint stride2 = bitmapInfo2.stride;
    LOGE("nativeCompareBitmapWithSimilarity stride %d, threshold %d ", stride2, threshold);
    if (newHeight != newHeight2 || newWidth != newWidth2) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        AndroidBitmap_unlockPixels(env, bitmapBack);
        return 0;
    }
    jdouble minValue = 100;
    jdouble minIndex = -1;
    for (jint lineBack = newHeight - 1; lineBack >= 0; lineBack --) {
        jint lineBottomPre = newHeight - 1;
        jint lineTopPre = lineBottomPre - lineBack;
        jint lineBottomBack = lineBack;
        jint lineTopBack = 0;
        jint height = lineBottomBack - lineTopBack;
        jdouble diffRate = getDiffRate(
                (jbyte *) bitmapPixels, lineTopPre, stride,
                (jbyte *) bitmapPixels2, lineTopBack, stride2,
                0, newWidth, height, threshold, 10);
        if (diffRate < minValue) {
            minValue = diffRate;
            minIndex = lineBack;
        }
    }
    AndroidBitmap_unlockPixels(env, bitmapPre);
    AndroidBitmap_unlockPixels(env, bitmapBack);
    return newHeight - minIndex - 1;
}

extern "C"
JNIEXPORT jdouble JNICALL Java_com_miui_screenshot_BitmapUtils_nativeGetSimilarity
        (JNIEnv *env, jclass bitmapUtilClass,
                jobject bitmapPre, jint startYPre,
                jobject bitmapBack, jint startYBack,
                jint height, jfloat threshold) {
    if (startYPre < 0 || startYBack < 0) {
        LOGE("nativeGetSimilarity startYPre %d, startYBack %d ", startYPre, startYBack);
        return -1.0f;
    }
    AndroidBitmapInfo bitmapInfo;
    if ((AndroidBitmap_getInfo(env, bitmapPre, &bitmapInfo)) < 0) {
        return -1.0f;
    }
    void *bitmapPixels;
    if ((AndroidBitmap_lockPixels(env, bitmapPre, &bitmapPixels)) < 0) {
        return -1.0f;
    }
    AndroidBitmapInfo bitmapInfo2;
    if ((AndroidBitmap_getInfo(env, bitmapBack, &bitmapInfo2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return -1.0f;
    }
    void *bitmapPixels2;
    if ((AndroidBitmap_lockPixels(env, bitmapBack, &bitmapPixels2)) < 0) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        return -1.0f;
    }
    jint newWidth = bitmapInfo.width;
    jint newHeight = bitmapInfo.height;
    jint newWidth2 = bitmapInfo2.width;
    jint newHeight2 = bitmapInfo2.height;
    jint stride = bitmapInfo.stride;
    jint stride2 = bitmapInfo2.stride;
    if (startYPre + height > newHeight) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        AndroidBitmap_unlockPixels(env, bitmapBack);
        LOGE("nativeGetSimilarity startYPre + heightBack >= newHeight");
        return -1.0f;
    }
    if (startYBack + height > newHeight2) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        AndroidBitmap_unlockPixels(env, bitmapBack);
        LOGE("nativeGetSimilarity startYBack + heightBack >= newHeight2");
        return -1.0f;
    }
    if (newHeight != newHeight2 || newWidth != newWidth2) {
        AndroidBitmap_unlockPixels(env, bitmapPre);
        AndroidBitmap_unlockPixels(env, bitmapBack);
        LOGE("nativeGetSimilarity newHeight != newHeight2 || newWidth != newWidth2");
        return -1.0f;
    }
    jdouble diffRate = getDiffRate(
            (jbyte *) bitmapPixels, startYPre, stride,
            (jbyte *) bitmapPixels2, startYBack, stride2,
            0, newWidth, height, threshold, 1);
    AndroidBitmap_unlockPixels(env, bitmapPre);
    AndroidBitmap_unlockPixels(env, bitmapBack);
    return 1 - diffRate;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_miui_screenshot_BitmapUtils_getBitmapStride
        (JNIEnv *env, jclass bitmapUtilClass, jobject bitmapPre) {
    AndroidBitmapInfo bitmapInfo;
    if ((AndroidBitmap_getInfo(env, bitmapPre, &bitmapInfo)) < 0) {
        return 0;
    }
    return bitmapInfo.stride;
}
