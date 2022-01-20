package com.astroyodha.ui.user.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityEditProfileBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.viewmodel.ProfileViewModel
import com.astroyodha.utils.*
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : BaseActivity() {

    lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    var userModel: UserModel? = null
    var profileImagePath: Uri? = null

    private val AUTOCOMPLETE_REQUEST_CODE = 119

    val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    /**
     * initialize view
     */
    private fun init() {

        // init google place to search address
        Places.initialize(
            this@EditProfileActivity,
            resources.getString(R.string.google_maps_key)
        )
        manageFocus()
        setObserver()
        clickListener()
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {

        binding.imgUser.setOnClickListener {
            pickImage()
        }

        binding.edDateOfBirth.setOnClickListener {
            var datePickerDialog=DatePickerDialog(
                this@EditProfileActivity, date, myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog.setTitle(resources.getString(R.string.date_of_birth))
            datePickerDialog.show()
        }


        binding.edTimeOfBirth.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this@EditProfileActivity,
                { timePicker, selectedHour, selectedMinute ->

                    var timeCalnder=Calendar.getInstance()
                    timeCalnder.set(Calendar.HOUR_OF_DAY,selectedHour)
                    timeCalnder.set(Calendar.MINUTE, selectedMinute)

                    val myFormat = "hh:mm a" //In which you need put here
                    val sdf = SimpleDateFormat(myFormat, Locale.US)

                    binding.edTimeOfBirth.setText(sdf.format(timeCalnder.time))},
                hour,
                minute,
                false
            ) //Yes 24 hour time

            mTimePicker.setTitle(resources.getString(R.string.time_of_birth))
            mTimePicker.show()
        }

        binding.btnUpdate.setOnClickListener {
            if (checkValidation()) {
                userModel!!.fullName = binding.edFullName.text.toString()
                userModel!!.email = binding.edEmail.text.toString()
                userModel!!.birthDate = binding.edDateOfBirth.text.toString()
                userModel!!.birthTime = binding.edTimeOfBirth.text.toString()
                userModel!!.birthPlace = binding.edPlaceOfBirth.text.toString()
                userModel!!.fcmToken = pref.getValue(this,Constants.PREF_FCM_TOKEN,"")

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
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edFullName.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.border_blue))
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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edPhoneNumber.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.border_blue
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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edEmail.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.border_blue))
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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edDateOfBirth.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edDateOfBirth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.border_blue
                    )
                )
            } else {
                binding.edDateOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edDateOfBirth.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edDateOfBirth.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edTimeOfBirth.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edTimeOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edTimeOfBirth.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edTimeOfBirth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.border_blue
                    )
                )
            } else {
                binding.edTimeOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edTimeOfBirth.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edTimeOfBirth.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edPlaceOfBirth.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.edPlaceOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_blue))
                binding.edPlaceOfBirth.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edPlaceOfBirth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.border_blue
                    )
                )
            } else {
                binding.edPlaceOfBirth.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_gray))
                binding.edPlaceOfBirth.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edPlaceOfBirth.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

    }

    /**
     * manage auto search places
     */
    private fun callAutoSearch() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)

            .build(this@EditProfileActivity)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
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
                        binding.root.showSnackBarToast(it)
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
    }

    /**
     * set data to view
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
            userModel!!.birthTime.let {
                binding.edTimeOfBirth.setText(it)
            }
            userModel!!.birthPlace.let {
                binding.edPlaceOfBirth.setText(it)
                binding.txtBirthPlace.setText(it)
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
        TedPermission.with(this@EditProfileActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_pic)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@EditProfileActivity, 100)
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
            AUTOCOMPLETE_REQUEST_CODE-> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = Autocomplete.getPlaceFromIntent(data!!)

                    val latLng: LatLng? = place.latLng
                    val MyLat: Double = latLng!!.latitude
                    val MyLong: Double = latLng!!.longitude
                    val geocoder = Geocoder(this@EditProfileActivity, Locale.getDefault())
                    try {
                        val addresses: List<Address> = geocoder.getFromLocation(MyLat, MyLong, 1)
                        val stateName: String = addresses[0].getAdminArea()
                        val cityName: String = addresses[0].getLocality()
                        binding.edPlaceOfBirth.setText(cityName)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }



                }
                else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    var status = Autocomplete.getStatusFromIntent(data);
                } else {
                    // The user canceled the operation.
                }
            }
        }
    }


    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
            .start(this@EditProfileActivity)
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
        } else if (TextUtils.isEmpty(binding.edTimeOfBirth.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_time))
            return false
        } else if (TextUtils.isEmpty(binding.edPlaceOfBirth.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_place))
            return false
        }
        return true
    }


}