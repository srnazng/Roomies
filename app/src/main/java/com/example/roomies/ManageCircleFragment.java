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
import com.example.roomies.model.Circle;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Fragment allowing users to edit circle settings
 */
public class ManageCircleFragment extends Fragment {

    private Circle circle;
    private ImageView ivCircleImage;
    private ImageView ivAddPhoto;
    private EditText etNameInput;
    private Button btnUpdate;
    private ProgressDialog pd;

    private Bitmap bitmap;

    public static final int GET_FROM_GALLERY = 3;

    public ManageCircleFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment ManageCircleFragment.
     */
    public static ManageCircleFragment newInstance(Circle circle) {
        ManageCircleFragment fragment = new ManageCircleFragment();
        Bundle args = new Bundle();
        if(circle != null){
            args.putParcelable("circle", circle);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            circle = getArguments().getParcelable("circle");
        }
        pd = new ProgressDialog(getActivity());
        pd.setTitle("Loading...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_circle, container, false);

        // show circle image
        ivCircleImage = view.findViewById(R.id.ivCircleImage);
        if (circle != null && circle.getImage() != null) {
            String imageUrl = circle.getImage().getUrl();
            Glide.with(this).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivCircleImage);
        }

        // initialize bitmap for sending new profile image
        bitmap = null;

        // button to edit circle image
        ivAddPhoto = view.findViewById(R.id.IvAddPhoto);
        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        // set name input to existing circle name
        etNameInput = view.findViewById(R.id.etNameInput);
        if(circle != null){
            etNameInput.setText(circle.getString("name"));
        }

        // update button
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                updateCircle();
            }
        });

        return view;
    }

    // convert bitmap image to ParseFile
    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    // after upload new circle image
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
                Glide.with(this).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivCircleImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateCircle() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Circle");

        // Retrieve the object by id
        query.getInBackground(circle.getObjectId(), (object, e) -> {
            if (e == null) {
                // Object was successfully retrieved
                // Update the fields we want to
                object.put("name", etNameInput.getText().toString());

                // update circle image if a new one has been uploaded
                if(bitmap != null){
                    object.put("image", conversionBitmapParseFile(bitmap));
                }

                //All other fields will remain the same
                object.saveInBackground();
                Toast.makeText(getActivity(), "Update success", Toast.LENGTH_SHORT).show();
            } else {
                // something went wrong
                Toast.makeText(getActivity(), "Update failed, try again later", Toast.LENGTH_SHORT).show();
            }
            pd.dismiss();
        });
    }
}