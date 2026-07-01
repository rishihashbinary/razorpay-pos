package com.routehub.pos.onboarding

import android.view.View

interface OnboardingTargetProvider {
    fun getScanQrView(): View?
    fun getMobileNumberView(): View?
    fun getAddManuallyView(): View?
}