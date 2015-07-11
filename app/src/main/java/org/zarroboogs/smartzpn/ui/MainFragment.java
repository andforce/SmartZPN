package org.zarroboogs.smartzpn.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.core.LocalVpnService;

import java.util.Calendar;


/**
 * Created by andforce on 15/7/11.
 */
public class MainFragment extends Fragment implements LocalVpnService.onStatusChangedListener {

    private static String GL_HISTORY_LOGS;

    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    private CircularProgressButton circularProgressButton;
    private TextView textViewLog;
    private ScrollView scrollViewLog;
    private Calendar mCalendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);

        View view = inflater.inflate(R.layout.main_fragment_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewLog = (TextView) view.findViewById(R.id.textViewLog);
        circularProgressButton = (CircularProgressButton) view.findViewById(R.id.circularButton1);
        scrollViewLog = (ScrollView) view.findViewById(R.id.scrollViewLog);

        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = LocalVpnService.prepare(getActivity());
                if (intent == null) {
                    startVPNService();
                } else {
                    startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startVPNService();
            } else {
                onLogReceived("canceled.");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startVPNService() {
        String configUrl = "http://119.254.103.105/pTKII.DGIb8.spac";

        LocalVpnService.ConfigUrl = configUrl;
        getActivity().startService(new Intent(getActivity(), LocalVpnService.class));
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        onLogReceived(status);
        Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLogReceived(String logString) {

        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        System.out.println(logString);

        if (textViewLog.getLineCount() > 200) {
            textViewLog.setText("");
        }
        textViewLog.append(logString);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);
        GL_HISTORY_LOGS = textViewLog.getText() == null ? "" : textViewLog.getText().toString();
    }
}
