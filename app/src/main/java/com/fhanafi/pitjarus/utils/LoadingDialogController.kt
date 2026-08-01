package com.fhanafi.pitjarus.utils

import androidx.fragment.app.Fragment

fun Fragment.showGlobalLoading(message: String) {
    val manager = childFragmentManager
    val current = manager.findFragmentByTag(LoadingDialog.TAG)
    if (current == null && !manager.isStateSaved) {
        LoadingDialog.newInstance(message).show(manager, LoadingDialog.TAG)
    }
}

fun Fragment.hideGlobalLoading() {
    val current = childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as? LoadingDialog
    current?.dismissAllowingStateLoss()
}
