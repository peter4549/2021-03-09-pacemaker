package com.flow.android.kotlin.pacemaker.base

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.pacemaker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseFragment<VM : ViewModel, VB : ViewBinding> : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected abstract val viewBindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    private var _viewBinding: VB? = null
    protected val viewBinding: VB
        get() = requireNotNull(_viewBinding)

    protected abstract val useActivityViewModel: Boolean
    protected lateinit var viewModel: VM

    protected abstract fun viewModel(): Class<VM>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = if (useActivityViewModel)
            requireActivity().run { ViewModelProviders.of(this)[viewModel()] }
        else
            ViewModelProvider(viewModelStore, viewModelFactory).get(viewModel())

        _viewBinding = viewBindingInflater.invoke(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onDestroyView() {
        _viewBinding = null
        super.onDestroyView()
    }

    @Suppress("SameParameterValue")
    protected fun showMaterialAlertDialog(
            title: String?,
            message: String?,
            neutralButtonText: String?,
            neutralButtonClickListener: ((DialogInterface?, Int) -> Unit)?,
            negativeButtonText: String?,
            negativeButtonClickListener: ((DialogInterface?, Int) -> Unit)?,
            positiveButtonText: String?,
            positiveButtonClickListener: ((DialogInterface?, Int) -> Unit)?
    ) {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(neutralButtonText, neutralButtonClickListener)
                .setNegativeButton(negativeButtonText, negativeButtonClickListener)
                .setPositiveButton(positiveButtonText, positiveButtonClickListener)
                .setCancelable(true)
                .show()
    }
}