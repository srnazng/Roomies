package com.example.roomies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ManageAccountFragment extends Fragment {
    private ImageView ivProfileImage;
    private ImageView ivAddPhoto;
    private EditText etNameInput;
    private EditText etEmailInput;
    private Button btnUpdateAccount;
    private Bitmap bitmap;
    private ProgressDialog pd;

    public static final int GET_FROM_GALLERY = 3;

    private ParseUser currentUser;

    public ManageAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Fragment allowing users to edit account settings
     * @return A new instance of fragment ManageAccountFragment.
     */
    public static ManageAccountFragment newInstance() {
        ManageAccountFragment fragment = new ManageAccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ParseUser.getCurrentUser();

        // set up progress dialog
        pd = new ProgressDialog(getActivity());
        pd.setTitle("Loading...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_account, container, false);

        // show profile image
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        if (currentUser.getParseFile("image") != null) {
            String imageUrl = currentUser.getParseFile("image").getUrl();
            Glide.with(this).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfileImage);
        }

        // initialize bitmap for sending new profile image
        bitmap = null;

        // button to edit profile image
        ivAddPhoto = view.findViewById(R.id.IvAddPhoto);
        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        // set email input to existing email
        etEmailInput = view.findViewById(R.id.etEmailInput);
        etEmailInput.setText(currentUser.getEmail());

        // set name input to existing user name
        etNameInput = view.findViewById(R.id.etNameInput);
        etNameInput.setText(currentUser.getString("name"));

        // update button
        btnUpdateAccount = view.findViewById(R.id.btnUpdateAccount);
        btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show progress bar
                pd.show();
                updateUser();
            }
        });

        return view;
    }

    public void updateUser() {

        if (currentUser != null) {

            // Change only name and email
            currentUser.put("name", etNameInput.getText().toString());
            currentUser.put("email", etEmailInput.getText().toString());
            currentUser.put("username", etEmailInput.getText().toString()); // username is same as email

            // update profile image if a new one has been uploaded
            if(bitmap != null){
                currentUser.put("image", conversionBitmapParseFile(bitmap));
            }

            // Saves the object.
            currentUser.saveInBackground(e -> {
                if(e==null){
                    //Save successfull
                    Toast.makeText(getActivity(), "Account Update Success", Toast.LENGTH_SHORT).show();
                }else{
                    // Something went wrong while saving
                    Toast.makeText(getActivity(), "Account update failed, try again later.", Toast.LENGTH_SHORT).show();
                }
                // Hide progress bar
                pd.dismiss();
            });
        }
    }

    // convert bitmap image to ParseFile
    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    // after upload new profile image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                // set bitmap to uploaded image
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                // show uploaded image
                Glide.with(this).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivProfileImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}