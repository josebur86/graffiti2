package com.tinydino.graffiti.ui.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.tinydino.graffiti.ChatMessage;
import com.tinydino.graffiti.ChatMessageAdapter;
import com.tinydino.graffiti.MessageBoardView;
import com.tinydino.graffiti.MessageListener;
import com.tinydino.graffiti.R;
import com.tinydino.graffiti.SocketControllerImpl;
import com.tinydino.graffiti.ui.presenter.MessageBoardPresenter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MessageBoardActivity extends ListActivity implements MessageBoardView {

    private ChatMessageAdapter _messageAdapter;
    private TextView _messageEdit;

    private MessageBoardPresenter _presenter;

    private static int kRequestImageCapture = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_board);

        _messageEdit = (TextView) findViewById(R.id.editMessage);
        _messageEdit.setOnKeyListener(new ReturnKeyListener());

        ImageButton pictureButton = (ImageButton) findViewById(R.id.sendPicture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPicture();
            }
        });

        _messageAdapter = new ChatMessageAdapter(
                this, R.layout.list_item_style, new ArrayList<ChatMessage>());
        ListView mainMessageList = (ListView) findViewById(android.R.id.list);
        mainMessageList.setAdapter(_messageAdapter);

        String username = getIntent().getStringExtra("userName");
        SocketControllerImpl socketController = new SocketControllerImpl(
                "https://thawing-island-7364.herokuapp.com/",
                username, getMessageListener());
        _presenter = new MessageBoardPresenter(this, socketController, username, "TODO");
        _presenter.create();
    }

    private void onSend() {
        String message = _messageEdit.getText().toString().trim();
        _presenter.sendMessage(message);
        _messageEdit.setText("");
    }

    private void onPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, kRequestImageCapture);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == kRequestImageCapture && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            _presenter.sendPicture(getBitmapBuffer(imageBitmap));
        }
    }

    private ByteBuffer getBitmapBuffer(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());

        return buffer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        _presenter.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addMessageToList(ChatMessage message) {
        _messageAdapter.add(message);
    }

    @Override
    public void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessageListener getMessageListener() {
        return new MessageListener(this, _presenter);
    }

    private class ReturnKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                onSend();
                return true;
            }
            return false;
        }
    }

    public void onClickUser(View view) {
        Intent mapIntent = new Intent();
        mapIntent.setClass(this, MapActivity.class);

        startActivity(mapIntent);
    }
}
