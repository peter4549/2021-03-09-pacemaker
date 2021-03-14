package com.flow.android.kotlin.pacemaker.view.main

import android.Manifest
import android.accounts.AccountManager
import android.content.Intent
import android.view.LayoutInflater
import com.flow.android.kotlin.pacemaker.base.BaseActivity
import com.flow.android.kotlin.pacemaker.databinding.ActivityMainBinding
import com.flow.android.kotlin.pacemaker.google_calendar.GoogleCalendarHelper
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.view.dialog_fragment.EditDialogFragment
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>(),
    EditDialogFragment.OnToDoUpdatedListener, EasyPermissions.PermissionCallbacks {

    private val googleCalendarHelper: GoogleCalendarHelper by lazy {
        GoogleCalendarHelper.getInstance(this)
    }

    override val viewBindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = { ActivityMainBinding.inflate(it) }

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    /** EditDialogFragment.OnToDoUpdatedListener */
    override fun onToDoUpdated(toDo: ToDo) {
        viewModel.setModifiedToDo(toDo)
    }

    override fun onError(throwable: Throwable) {
        Timber.e(throwable)
    }

    override fun onResume() {
        super.onResume()

        EasyPermissions.requestPermissions(
            this,
            "This app needs to access your Google account (via Contacts).",
            GoogleCalendarHelper.RequestCode.GetAccounts,
            Manifest.permission.GET_ACCOUNTS
        )


    }


    override fun onActivityResult(
        requestCode: Int,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
        resultCode: Int,  // 요청에 대한 결과 코드
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GoogleCalendarHelper.RequestCode.GooglePlayServices -> {
                if (resultCode == RESULT_OK) {
                    googleCalendarHelper
                } else {

                }
                /*
                if (resultCode != RESULT_OK) {
                mStatusText.setText(
                    " 앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
                            + "구글 플레이 서비스를 설치 후 다시 실행하세요."
                )
            } else {

                getResultsFromApi()

                 */
            }
            GoogleCalendarHelper.RequestCode.ChooseAccount -> {
            if ((resultCode == RESULT_OK) && (data != null) && (data.extras != null)) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    googleCalendarHelper.s(this, accountName)
                }
            }
        }
        /*
            REQUEST_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                getResultsFromApi()
            }

         */
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, requestPermissionList: List<String?>?) {
        if (requestCode == GoogleCalendarHelper.RequestCode.GetAccounts) {
            //println(googleCalendarHelper.googleAccountCredential().allAccounts)
            // googleCalendarHelper.s(this)

                googleCalendarHelper.googleAccountCredential().allAccounts.map { it.name }
            /*
            startActivityForResult(
                googleCalendarHelper.googleAccountCredential().newChooseAccountIntent(),
                GoogleCalendarHelper.RequestCode.ChooseAccount
            )

             */
        }
        // 아무일도 하지 않음
    }

    override fun onPermissionsDenied(requestCode: Int, requestPermissionList: List<String?>?) {

        // 아무일도 하지 않음
    }
}
