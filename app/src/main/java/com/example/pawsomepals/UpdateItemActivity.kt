package com.example.pawsomepals

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pawsomepals.dataclass.MarketplaceItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class UpdateItemActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST_CODE = 2135

    private lateinit var addImageContainer: RelativeLayout
    private lateinit var imageViewSelected: ImageView

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var editTextTitle: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextDiscount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextQty: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var editTextBrand: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextColor: EditText

    private lateinit var selectedCategory: String
    private lateinit var selectedCondition: String

    private lateinit var databaseReferenceUser: DatabaseReference
    private lateinit var databaseReferenceItems: DatabaseReference

    private lateinit var exitingImage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_item)

        supportActionBar?.hide()

        val itemID = intent.getStringExtra("itemID")

        val packageName = applicationContext.packageName
        val preferencesName = "$packageName.user_preferences"
        val sharedPreferences = applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID","").toString()
        val username = sharedPreferences.getString("userName", "Loading...").toString()

        addImageContainer = findViewById(R.id.addImageContainer)
        val imageViewAddImages = findViewById<ImageView>(R.id.imageViewAddImages)
        imageViewSelected = findViewById(R.id.imageViewSelected)

        var selectedImageUri: Uri? = null

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    imageViewAddImages.visibility = View.GONE
                    imageViewSelected.visibility = View.VISIBLE
                    imageViewSelected.setImageURI(selectedImageUri)
                }
            }
        }


        addImageContainer.setOnClickListener {
            // Open the image picker (Gallery)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)

        }

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextDiscount = findViewById(R.id.editTextDiscount)
        editTextQty = findViewById(R.id.editTextQty)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextBrand = findViewById(R.id.editTextBrand)
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextColor = findViewById(R.id.editTextColor)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCondition = findViewById(R.id.spinnerCondition)

        val btnSubmit = findViewById<Button>(R.id.btnSubmitListItem)

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinnerCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCondition = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        databaseReferenceItems = FirebaseDatabase.getInstance().getReference("marketplaceItem")
        if (itemID != null) {
            databaseReferenceItems.child(itemID).get().addOnSuccessListener {
                if (it.exists()){
                    val image = it.child("image").value.toString()
                    exitingImage = image
                    val title = it.child("title").value.toString()
                    val price = it.child("price").value.toString()
                    val discount = it.child("discount").value.toString()
                    val description = it.child("description").value.toString()
                    val condition = it.child("condition").value.toString()
                    val category = it.child("category").value.toString()
                    val qty = it.child("qty").value.toString()
                    val brand = it.child("brand").value.toString()
                    val color = it.child("color").value.toString()
                    val weight = it.child("weight").value.toString()
                    val owner = it.child("owner").value.toString()

                    Picasso.get().load(image).into(imageViewSelected)
                    imageViewSelected.visibility = View.VISIBLE

                    editTextTitle.setText(title)
                    editTextPrice.setText(price)
                    editTextDiscount.setText(discount)
                    editTextQty.setText(qty)
                    editTextDescription.setText(description)
                    editTextBrand.setText(brand)
                    editTextColor.setText(color)
                    editTextWeight.setText(weight)
                    editTextDescription.setText(description)

                    when (category) {
                        "Foods" -> spinnerCategory.setSelection(1)
                        "Kennel" -> spinnerCategory.setSelection(2)
                        "Bird Cages" -> spinnerCategory.setSelection(3)
                        "Pet Accessories" -> spinnerCategory.setSelection(4)
                        "Feeding Accessories" -> spinnerCategory.setSelection(5)
                        "Pet Toys" -> spinnerCategory.setSelection(6)
                        else -> spinnerCategory.setSelection(0)
                    }

                    when (condition) {
                        "New" -> spinnerCondition.setSelection(1)
                        "Used" -> spinnerCondition.setSelection(2)
                        else -> spinnerCondition.setSelection(0)
                    }
                }
            }
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference("user")
        databaseReferenceItems = FirebaseDatabase.getInstance().getReference("marketplaceItem")

        val uniqueFilename = "${userID}_${System.currentTimeMillis()}_${generateRandomString(5)}"
        val imageRef = storageRef.child("marketplaceImages/${uniqueFilename}")

        btnSubmit.setOnClickListener {
            val storageExistingRef = FirebaseStorage.getInstance().getReferenceFromUrl(exitingImage)
            storageExistingRef.delete()

            selectedImageUri?.let {imgUri ->
                imageRef.putFile(imgUri).addOnSuccessListener {taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener {uri ->
                        val downloadUrl = uri.toString()
                        var marketplaceItem = MarketplaceItem(
                            downloadUrl,
                            editTextTitle.text.toString(),
                            editTextPrice.text.toString(),
                            editTextDiscount.text.toString(),
                            selectedCategory,
                            editTextQty.text.toString(),
                            editTextDescription.text.toString(),
                            selectedCondition,
                            editTextBrand.text.toString(),
                            editTextColor.text.toString(),
                            editTextWeight.text.toString(),
                            userID
                        )
                        databaseReferenceItems.push().setValue(marketplaceItem)
                    }
                }.addOnFailureListener{

                }
            }
        }
    }

    private fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}