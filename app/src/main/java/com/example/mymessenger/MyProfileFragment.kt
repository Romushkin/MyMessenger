package com.example.mymessenger

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymessenger.databinding.FragmentMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream


class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    val REQUEST_CODE_SELECT_IMAGE = 1
    val REQUEST_CODE_SELECT_PHOTO = 2
    val REQUEST_CAMERA_PERMISSION = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = Firebase.database
        val userId = Firebase.auth.currentUser?.uid
        val reference = userId?.let { database.getReference("users").child(it) }

        reference?.child("profileImageUri")?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val downloadUri = dataSnapshot.getValue(String::class.java)
                if (!downloadUri.isNullOrEmpty()) {
                    Picasso.get()
                        .load(downloadUri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.myProfileImageIV)
                } else {
                    Picasso.get()
                        .load(R.drawable.profile)
                        .into(binding.myProfileImageIV)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    requireActivity(),
                    "Ошибка получения изображения",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


        userId?.let {
            reference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    val surname = dataSnapshot.child("surname").getValue(String::class.java)
                    val role = dataSnapshot.child("role").getValue(String::class.java)
                    val address = dataSnapshot.child("address").getValue(String::class.java)
                    val age = dataSnapshot.child("age").getValue(String::class.java)

                    binding.myProfileNameET.setText(name)
                    binding.myProfileSurnameET.setText(surname)
                    binding.myProfileRoleET.setText(role)
                    binding.meProfileAddressET.setText(address)
                    binding.meProfileAgeET.setText(age)

                    reference.child("name").setValue(name)
                    binding.myProfileNameTV.text = name

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        requireActivity(),
                        "Ошибка получения информации из Базы Данных",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
        binding.meProfileSaveBTN.setOnClickListener {
            val name = binding.myProfileNameET.text.toString()
            val surname = binding.myProfileSurnameET.text.toString()
            val role = binding.myProfileRoleET.text.toString()
            val address = binding.meProfileAddressET.text.toString()
            val age = binding.meProfileAgeET.text.toString()

            saveMyProfileInfoToDB(name, surname, role, address, age)
            Toast.makeText(requireActivity(), "Информация сохранена", Toast.LENGTH_LONG).show()
        }

        val user = Firebase.auth.currentUser
        val email = user?.email
        val emailMasked = email?.let {
            val atIndex = it.indexOf('@')
            val maskedPart = it.substring(0, atIndex - 2).replace(Regex("."), "*")
            val lastPart = it.substring(atIndex - 2)
            maskedPart + lastPart
        }
        binding.myProfileEmailTV.text = emailMasked

        userId?.let {
            reference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    if (user != null) {
                        binding.myProfileNameTV.text = name
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        requireActivity(),
                        "Ошибка получения информации из Базы Данных",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        binding.changeMyProfileImageIV.setOnClickListener {
            val options = arrayOf("Галереи", "Камеры")
            AlertDialog.Builder(requireActivity())
                .setTitle("Выбрать/сделать изображение из:")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            val galleryIntent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(galleryIntent, REQUEST_CODE_SELECT_IMAGE)
                        }
                        1 -> {
                            checkCameraPermissionAndLaunchCamera()
                        }
                    }
                }.show()
        }


        binding.addMyProfilePhoneIV.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_phone, null)
            val flagIV = dialogView.findViewById<ImageView>(R.id.flagIV)
            val codeSpinner = dialogView.findViewById<Spinner>(R.id.codeSpinner)
            val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditET)
            val applyButton = dialogView.findViewById<Button>(R.id.applyBTN)

            val adapter = ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.country_codes,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            codeSpinner.adapter = adapter

            codeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val flagsArray = resources.obtainTypedArray(R.array.country_flags)
                    flagIV.setImageResource(flagsArray.getResourceId(position, -1))
                    flagsArray.recycle()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val dialog = AlertDialog.Builder(requireActivity())
                .setView(dialogView)
                .show()

            applyButton.setOnClickListener {
                val selectedCountryCode = codeSpinner.selectedItem.toString()
                val phoneNumber = phoneEditText.text.toString()

                savePhoneNumberToDB(selectedCountryCode, phoneNumber)

                dialog.dismiss()
            }
        }

        reference?.child("profileImageUri")?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fileName = dataSnapshot.getValue(String::class.java)
                fileName?.let {
                    loadProfileImage(it)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireActivity(), "Ошибка получения информации", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun checkCameraPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            launchCameraIntent()
        }
    }


    private fun launchCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE_SELECT_PHOTO)
        } else {
            Toast.makeText(requireActivity(), "Камера не поддерживается на этом устройстве", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhoneNumberToDB(countryCode: String, phoneNumber: String) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("users").child(it) }
        val phoneWithCode = "$countryCode $phoneNumber"
        userRef?.child("phone")?.setValue(phoneWithCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCameraIntent()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Требуется разрешение на камеру",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?

            if (imageBitmap != null) {
                binding.myProfileImageIV.setImageBitmap(imageBitmap)

                val fileName = "profile_${System.currentTimeMillis()}.jpg"
                val filePath = File(requireContext().filesDir, fileName)
                try {
                    val fos = FileOutputStream(filePath)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()

                    saveProfileImageNameToDatabase(fileName)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireActivity(), "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(), "Не удалось захватить изображение", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveProfileImageNameToDatabase(fileName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        userRef.child("profileImageUri").setValue(fileName)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireActivity(), "Имя изображения сохранено", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(), "Ошибка сохранения имени изображения", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun loadProfileImage(fileName: String) {
        val filePath = File(requireContext().filesDir, fileName)

        if (filePath.exists()) {
            Picasso.get()
                .load(filePath)
                .placeholder(R.drawable.profilebackground)
                .error(R.drawable.ic_person)
                .into(binding.myProfileImageIV)
        } else {
            Toast.makeText(requireActivity(), "Изображение не найдено", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveMyProfileInfoToDB(
        name: String,
        surname: String,
        role: String,
        address: String,
        age: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("users").child(it) }
        userRef?.child("name")?.setValue(name)
        userRef?.child("surname")?.setValue(surname)
        userRef?.child("role")?.setValue(role)
        userRef?.child("address")?.setValue(address)
        userRef?.child("age")?.setValue(age)
    }
}