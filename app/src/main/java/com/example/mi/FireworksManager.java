package com.example.mi;

import android.graphics.PointF;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Random;

public class FireworksManager {
    private static final String TAG = "FireworksManager";
    private List<PointF> mFireList;
    private int mDistance;
    private float mSpeed;
    private Random mRandom;
    private Queue<PointF> mCache;
    public FireworksManager(int distance, float speed) {
        mFireList = new LinkedList<>();
        mCache = new LinkedList<>();
        mDistance = distance;
        mSpeed = speed;
        mRandom = new Random(System.currentTimeMillis());
    }

    public synchronized void freshPositions(List<PointF> newPositions, long elapseTime) {
        if (newPositions == null) {
            return;
        }
        float distanceStep = elapseTime * mSpeed;
        ListIterator<PointF> iterator = mFireList.listIterator();
        while (iterator.hasNext()) {
            PointF pointF = iterator.next();
            pointF.y = pointF.y - distanceStep;
            if (pointF.y <= 0) {
                mCache.offer(pointF);
                iterator.remove();
            }
        }
        newPositions.clear();
        newPositions.addAll(mFireList);
    }

    private int mLastIndex;
    public synchronized void fire() {
        int index = mRandom.nextInt(5);
        int tryCount = 1;
        while(true) {
            if (Math.abs(index - mLastIndex) <= 1 && tryCount < 6) {
                index = mRandom.nextInt(5);
                tryCount ++;
            } else {
                break;
            }
        }
        if (index >= 0 && index < 5) {
            PointF pointF = mCache.poll();
            if (pointF == null) {
                pointF = new PointF();
            }
            pointF.x = index;
            pointF.y = mDistance;
            mLastIndex = index;
            mFireList.add(pointF);
        }
    }
}
