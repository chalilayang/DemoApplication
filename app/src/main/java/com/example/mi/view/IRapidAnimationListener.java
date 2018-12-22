package com.example.mi.view;

public interface IRapidAnimationListener {
    void onRapidAnimationStart(@ChargeUtils.CHARGE_TYPE int type);
    void onRapidAnimationEnd(@ChargeUtils.CHARGE_TYPE int type);
    void onRapidAnimationDismiss(@ChargeUtils.CHARGE_TYPE int type);
}
