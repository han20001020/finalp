package com.example.map;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AdviceFragment extends Fragment {

    private EditText editTextName, editTextEmail, editTextAdivce;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_advice, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextAdivce = view.findViewById(R.id.editTextAdvice);
        submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String advice = editTextAdivce.getText().toString();

                sendDataToGoogleForm(name, email, advice);

            }
        });
        return view;
    }

    private void sendDataToGoogleForm(String name, String email,String advice) {
        final String FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSeBGknHWc3vKihW0qcl8YbPN-MVpDE_F5aMT_LPMnLDlLDKzA/formResponse";

        final String NAME_FIELD = "entry.394063579";
        final String EMAIL_FIELD = "entry.1958182999";
        final String ADVICE_FIELD = "entry.2081990018";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(FORM_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // 編輯提交的數據
                    String postData = NAME_FIELD + "=" + URLEncoder.encode(name, "UTF-8")
                            + "&" + EMAIL_FIELD + "=" + URLEncoder.encode(email, "UTF-8")
                            + "&" + ADVICE_FIELD + "=" + URLEncoder.encode(advice, "UTF-8");

                    OutputStream outputStream = urlConnection.getOutputStream();
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = urlConnection.getResponseCode();

                    Log.d("GoogleForm", "Response code: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Submitted successfully!", Toast.LENGTH_SHORT).show());
                    } else {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Submission failed!", Toast.LENGTH_SHORT).show());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "An error occurred!", Toast.LENGTH_SHORT).show());
                    Log.d("GoogleForm", "Error occurred: " + e.getMessage());
                }
            }
        }).start();
    }
}