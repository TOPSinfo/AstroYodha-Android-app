package com.astroyodha.ui.astrologer.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerEditProfileBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.adapter.AstrologerLanguageAndAstologyTypeAdapter
import com.astroyodha.ui.astrologer.adapter.TimeSlotAdapter
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.astrologer.viewmodel.TimeSlotViewModel
import com.astroyodha.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AstrologerEditProfileActivity : BaseActivity() {

    lateinit var binding: ActivityAstrologerEditProfileBinding
    var mSelectedLanguage: ArrayList<String> = ArrayList()
    var mSelectedAstrtoType: ArrayList<String> = ArrayList()
    var mTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()


    private val profileViewModel: ProfileAstrologerViewModel by viewModels()
    private val timeSlotViewModel: TimeSlotViewModel by viewModels()
    var userModel: AstrologerUserModel? = null
    var profileImagePath: Uri? = null
    val myCalendar: Calendar = Calendar.getInstance()

    lateinit var languageAndAstologyTypeAdapter: AstrologerLanguageAndAstologyTypeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAstrologerEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        binding.edPricePerMin.inputType = InputType.TYPE_CLASS_NUMBER
        binding.edExperience.inputType = InputType.TYPE_CLASS_NUMBER

        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        setTimeSlotAdapter()
        manageFocus()
        clickListener()
        setObserver()
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edLanguage.setOnClickListener {
            languageAndAstrologyTypeSelectionDialog(true)
        }

        binding.edAstroType.setOnClickListener {
            languageAndAstrologyTypeSelectionDialog(false)
        }

        binding.imgEdit.setOnClickListener {
            pickImage()
        }

        binding.edDateOfBirth.setOnClickListener {
            var datePickerDialog = DatePickerDialog(
                this@AstrologerEditProfileActivity,
                R.style.DatePickerTheme,
                date,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog.setTitle(resources.getString(R.string.date_of_birth))
            datePickerDialog.show()
        }

        binding.imgAddTimeSlot.setOnClickListener {
            var intent=Intent(this,AddTimeSlotActivity::class.java)
            intent.putParcelableArrayListExtra(Constants.INTENT_TIME_SLOT_LIST,mTimeSlotList)
            startActivity(intent)
        }

        binding.btnSave.setOnClickListener {
            if (checkValidation()) {
                userModel!!.fullName = binding.edFullName.text.toString()
                userModel!!.email = binding.edEmail.text.toString()
                userModel!!.birthDate = binding.edDateOfBirth.text.toString()
                userModel!!.speciality = mSelectedAstrtoType
                userModel!!.languages = mSelectedLanguage
                userModel!!.price = binding.edPricePerMin.text.toString().trim().toInt()
                userModel!!.experience = binding.edExperience.text.toString().toInt()
                userModel!!.about = binding.edAbout.text.toString()
                userModel!!.fcmToken = pref.getValue(this,Constants.PREF_FCM_TOKEN,"").toString()

                if (profileImagePath != null) {
                    profileImagePath?.let { it1 ->
                        profileViewModel.updateProfilePicture(
                            userModel!!,
                            it1,
                            true
                        )
                    }
                } else {
                    profileViewModel.updateUserData(userModel!!)
                }
            }
        }



    }


    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.userDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        if (userModel != null) {
                            setUserData()
                            timeSlotViewModel.getTimeSlotList(userModel!!.uid!!)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        profileViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                       toast(it)
                        val resultIntent = Intent()
                        setResult(RESULT_OK, resultIntent)
                        finish()

                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        timeSlotViewModel.timeSlotListResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        mTimeSlotList.clear()
                        mTimeSlotList.addAll(it)

                        if (mTimeSlotList.isNotEmpty()) {
                            binding.tvNoDataFound.makeGone()
                            binding.recyclerTimeSoltList.makeVisible()
                            binding.recyclerTimeSoltList.adapter?.notifyDataSetChanged()
                        } else {
                            binding.tvNoDataFound.makeVisible()
                            binding.recyclerTimeSoltList.makeGone()
                        }

                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        timeSlotViewModel.timeslotDeleteResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        it.let { it1 -> binding.root.showSnackBarToast(it1) }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

    }



    /*
      * This is used to set adapter of recyclerview it display list of time slots.
      * */
    private fun setTimeSlotAdapter() {
        with(binding.recyclerTimeSoltList) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = TimeSlotAdapter(
                context, mTimeSlotList,
                object : TimeSlotAdapter.ViewHolderClicks {
                    override fun onClickItem(model: TimeSlotModel, position: Int) {
                        timeSlotViewModel.deleteTimeSlot(userModel!!.uid!!,model.id)
                    }

                }
            )
        }
    }

    /**
     * set user profile data to UI
     */
    private fun setUserData() {
        if (userModel != null) {
            userModel!!.fullName.let {
                binding.edFullName.setText(it)
                binding.txtUserName.setText(it)
            }
            userModel!!.phone.let {
                binding.edPhoneNumber.setText(it)
            }

            userModel!!.email.let {
                binding.edEmail.setText(it)
            }

            userModel!!.birthDate.let {
                binding.edDateOfBirth.setText(it)
            }
            userModel!!.experience.let {
                binding.edExperience.setText(it.toString())
            }
            userModel!!.price.let {
                binding.edPricePerMin.setText(it.toString())
            }
            userModel!!.about.let {
                binding.edAbout.setText(it)
            }

            userModel!!.languages.let {
                for (i in it!!) {
                    mSelectedLanguage.add(i)
                }

                var displayLanguageList = ArrayList<String>()
                for(language in Constants.listOfLanguages)
                {
                    for(selectedLanguage in mSelectedLanguage)
                    {
                        if(language.id.equals(selectedLanguage))
                        {
                            displayLanguageList.add(language.language)
                        }
                    }
                }

                binding.edLanguage.setText(displayLanguageList.joinToString(", "))

            }

            userModel!!.speciality.let {
                for (i in it!!) {
                    mSelectedAstrtoType.add(i)
                }
                var displaySpecialityList = ArrayList<String>()
                for(speciality in Constants.listOfSpeciality)
                {
                    for(selectedSpeciality in mSelectedAstrtoType)
                    {
                        if(speciality.id.equals(selectedSpeciality))
                        {
                            displaySpecialityList.add(speciality.language)
                        }
                    }
                }

                binding.edAstroType.setText(displaySpecialityList.joinToString(", "))
            }

            userModel!!.profileImage.let {
                binding.imgUser.loadProfileImage(it.toString())
            }
        }
    }


    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@AstrologerEditProfileActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerThemeBlue)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_pic)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@AstrologerEditProfileActivity, 100)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }


    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK && data != null) {
                val dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (dataList != null) {
                    if (dataList.size > 0) {
                        openCropper(dataList[0])
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri

                    binding.imgUser.loadProfileImage(ContentUriUtils.getFilePath(this, resultUri))
                    profileImagePath = resultUri

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                }
            }

        }
    }


    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
            .start(this@AstrologerEditProfileActivity)
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edFullName.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_first_name))
            return false
        } else if (TextUtils.isEmpty(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_email_address))
            return false
        } else if (!Utility.emailValidator(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_valid_email_address))
            return false
        } else if (TextUtils.isEmpty(binding.edDateOfBirth.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_date))
            return false
        }
        else if (mSelectedLanguage.isEmpty()) {
            binding.root.showSnackBarToast(getString(R.string.please_select_language))
            return false
        }
        else if (mSelectedAstrtoType.isEmpty()) {
            binding.root.showSnackBarToast(getString(R.string.please_select_astro_type))
            return false
        }
        else if (TextUtils.isEmpty(binding.edPricePerMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_price_per_min))
            return false
        }
        else if (TextUtils.isEmpty(binding.edExperience.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_experience))
            return false
        }
        else if (TextUtils.isEmpty(binding.edAbout.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_about))
            return false
        }
        return true
    }



    var date =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }


    private fun updateLabel() {
        val myFormat = "dd MMM yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.edDateOfBirth.setText(sdf.format(myCalendar.time))
    }



    /**
     * change layout borders color based on view focus
     */
    private fun manageFocus() {

        binding.edFullName.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edFullName.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edFullName.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.orange_theme))
            } else {
                binding.edFullName.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edFullName.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edPhoneNumber.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edPhoneNumber.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edPhoneNumber.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_theme
                    )
                )
            } else {
                binding.edPhoneNumber.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edEmail.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edEmail.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.orange_theme))
            } else {
                binding.edEmail.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edEmail.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }


        binding.edDateOfBirth.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edDateOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edDateOfBirth.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edDateOfBirth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_theme
                    )
                )
            } else {
                binding.edDateOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edDateOfBirth.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edDateOfBirth.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edPricePerMin.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edPricePerMin.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edPricePerMin.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edPricePerMin.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_theme
                    )
                )
            } else {
                binding.edPricePerMin.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edPricePerMin.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edPricePerMin.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edExperience.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edExperience.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.edExperience.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edExperience.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_theme
                    )
                )
            } else {
                binding.edExperience.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edExperience.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edExperience.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edAbout.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edAbout.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_theme))
                binding.lnAbout.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
                binding.edAbout.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_theme
                    )
                )
            } else {
                binding.edAbout.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.lnAbout.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edAbout.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

    }

    /**
     * Dialog for language Selection and astrology type selection
     */
    private fun languageAndAstrologyTypeSelectionDialog(isLanguage: Boolean) {

        var beaconDialog = Dialog(this)
        beaconDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        beaconDialog.setContentView(R.layout.dialog_language_and_astrotype)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(beaconDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        beaconDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        beaconDialog.window!!.attributes = lp
        beaconDialog.show()


        var recyclerLanguageAndAstroType =
            beaconDialog.findViewById<RecyclerView>(R.id.recyclerLanguageAndAstroType)

        var txtSubmit = beaconDialog.findViewById<TextView>(R.id.txtSubmit)
        var txtCancel = beaconDialog.findViewById<TextView>(R.id.txtCancel)

        val mLayoutManager = LinearLayoutManager(this@AstrologerEditProfileActivity)
        recyclerLanguageAndAstroType.layoutManager = mLayoutManager
        recyclerLanguageAndAstroType.itemAnimator = DefaultItemAnimator()

        if (isLanguage) {
            languageAndAstologyTypeAdapter =
                AstrologerLanguageAndAstologyTypeAdapter(this, Constants.listOfLanguages,mSelectedLanguage)
        } else {
            languageAndAstologyTypeAdapter =
                AstrologerLanguageAndAstologyTypeAdapter(this, Constants.listOfSpeciality,mSelectedAstrtoType)
        }

        recyclerLanguageAndAstroType.adapter = languageAndAstologyTypeAdapter


        txtCancel.setOnClickListener {
            beaconDialog.dismiss()
        }

        txtSubmit.setOnClickListener {
            if (isLanguage) {
                mSelectedLanguage =
                    languageAndAstologyTypeAdapter.getSelectedLanguagesAndAstroType()
                var displayLanguageList = ArrayList<String>()
                for (language in Constants.listOfLanguages) {
                    for (selectedLanguage in mSelectedLanguage) {
                        if (language.id.equals(selectedLanguage)) {
                            displayLanguageList.add(language.language)
                        }
                    }
                }

                binding.edLanguage.setText(displayLanguageList.joinToString(", "))
            } else {
                mSelectedAstrtoType =
                    languageAndAstologyTypeAdapter.getSelectedLanguagesAndAstroType()

                var displaySpecialityList = ArrayList<String>()
                for (speciality in Constants.listOfSpeciality) {
                    for (selectedSpeciality in mSelectedAstrtoType) {
                        if (speciality.id.equals(selectedSpeciality)) {
                            displaySpecialityList.add(speciality.language)
                        }
                    }
                }

                binding.edAstroType.setText(displaySpecialityList.joinToString(", "))
            }
            beaconDialog.dismiss()
        }
    }


}