package com.stegano.audiomedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_PERMISSION = 200;  // 요청에 대한 응답코드

    private TextView statusTextView;
    private Button recordButton;
    private Button playButton;

    private String fileName;  // 녹음 파일 저장 경로
    private MediaRecorder mediaRecorder = null;  // 녹음을 위한 객체
    private MediaPlayer mediaPlayer = null;  // 재생을 위한 객체


    // 권한 요청에 대한 응답 처리 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToRecordAccepted = false;
        switch(requestCode) {
            case RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                break;
        }
        if(permissionToRecordAccepted == false) {
            finish();
        }
    }

    // 생명주기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 오디오 권한을 가졌는지 확인
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 권한을 사용자에게 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
        }

        setContentView(R.layout.activity_main);

        statusTextView = (TextView) findViewById(R.id.statusTextView);
        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button) findViewById(R.id.playButton);

        fileName = getExternalCacheDir().getAbsolutePath() + "/record.3gp";
        Log.e("MainActivity", "저장 위치 : " + fileName);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaRecorder == null) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer == null) {
                    startPlaying();
                } else {
                    stopPlaying();
                }
            }
        });
    }

    private void startRecording() {  // 녹음 시작
        statusTextView.setText("녹음중");
        recordButton.setText("녹음중지");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // 마이크 설정
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  // 출력 포멧 지정
        mediaRecorder.setOutputFile(fileName);  // 저장될 경로 지정
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  // 오디오 인코더 지정

        try {
            mediaRecorder.prepare();  // 위의 설정으로 mediaRecorder를 준비한다
            mediaRecorder.start();  // 녹음 시작
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "녹음에 실패하였습니다.", Toast.LENGTH_SHORT);

            statusTextView.setText("대기상태");  // 녹음 시작 전 상태로 되돌리기
            recordButton.setText("녹음시작");
            mediaRecorder = null;  // 참조 제거
        }
    }

    private void stopRecording() {  // 녹음 중지
        statusTextView.setText("대기상태");  // 녹음 시작 전 상태로 되돌리기
        recordButton.setText("녹음시작");

        if(mediaRecorder != null) {
            Log.e("stopRecording()", "녹음 중지 중...");
            mediaRecorder.stop();  // 녹음 중지
            mediaRecorder.release();  // 저장 및 사용하던 하드웨어, 시스템의 사용 자원을 해제한다

            Log.e("stopRecording()", "녹음 중지");
        }
        mediaRecorder = null;
    }

    private void startPlaying() {  // 재생 시작
        statusTextView.setText("재생중");
        playButton.setText("재생중지");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();  // 재생 완료 이벤트 리스너를 연결하여 재생을 종료시킴
            }
        });

        try {
            mediaPlayer.setDataSource(fileName);  // 재생할 파일의 경로
            mediaPlayer.prepare();  // 재생 준비
            mediaPlayer.start();  // 재생

        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "재생에 실패하였습니다.", Toast.LENGTH_SHORT);

            statusTextView.setText("대기상태");
            playButton.setText("재생시작");
            mediaPlayer = null;  // 참조 제거
        }
    }

    private void stopPlaying() {  // 재생 중지
        statusTextView.setText("대기상태");  // 녹음 시작 전 상태로 되돌리기
        playButton.setText("재생시작");

        if(mediaPlayer != null) {
            mediaPlayer.release();  // 사용하던 하드웨어 및 시스템의 사용 자원을 해제한다
        }
        mediaPlayer = null;
    }
}