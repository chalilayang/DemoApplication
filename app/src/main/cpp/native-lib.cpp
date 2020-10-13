#include <jni.h>
#include <string>

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
