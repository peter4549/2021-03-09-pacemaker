package com.flow.android.kotlin.pacemaker.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.base.BaseActivity
import com.flow.android.kotlin.pacemaker.databinding.ActivityMainBinding
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val viewBindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = { ActivityMainBinding.inflate(it) }

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}