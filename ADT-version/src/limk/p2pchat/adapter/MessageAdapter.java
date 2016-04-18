package limk.p2pchat.adapter;

import java.util.HashMap;
import java.util.List;

import limk.p2pchat.R;
import limk.p2pchat.activity.ImageShowActivity;
import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import limk.p2pchat.database.P2PChatDBHelper;
import limk.p2pchat.net.SendMessage;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter {

	private List<MessageEntity> mData;
	private Context mContext;
	private LayoutInflater mInflater;
	private HashMap<String, Bitmap> cache;
	private P2PChatDBHelper dbHelper;
	private Handler mHandler;

	private OnClickListener imageOnclick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mContext, ImageShowActivity.class);
			intent.putExtra("data", (String) v.getTag());
			mContext.startActivity(intent);
		}
	};
	private OnClickListener btnOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {

			long messageID = (Long) v.getTag();
			Message msg = new Message();
			msg.what = Constant.RESEND_MESSAGE;
			msg.obj = messageID;
			mHandler.sendMessage(msg);

		}
	};

	public MessageAdapter(Context pContext, Handler pHandler, List<MessageEntity> pData) {
		this.mContext = pContext;
		this.mHandler = pHandler;
		this.mData = pData;
		this.mInflater = LayoutInflater.from(mContext);
		this.cache = new HashMap<String, Bitmap>();
		this.dbHelper = new P2PChatDBHelper(mContext);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		MessageEntity entity = mData.get(position);
		return entity.getMessageView();
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		MessageEntity entity = mData.get(arg0);
		int messageViewType = entity.getMessageView();
		int messageType = entity.getMessageType();

		if (messageType == MessageEntity.MessageType.WORD_VALUE) {
			if (messageViewType == MessageEntity.MessageView.RECEIVE_VALUE) {
				arg1 = mInflater.inflate(R.layout.message_list_item_left_word, null);
			} else if (messageViewType == MessageEntity.MessageView.SEND_VALUE) {
				arg1 = mInflater.inflate(R.layout.message_list_item_right_word, null);
			}
			TextView tv_time = (TextView) arg1.findViewById(R.id.tv_time);
			TextView tv_content = (TextView) arg1.findViewById(R.id.tv_chatcontent);
			if (entity.getMessageState() == MessageEntity.MessageState.SEND_OK_VALUE) {
				ImageView send = (ImageView) arg1.findViewById(R.id.iv_send_ok);
				send.setVisibility(View.VISIBLE);
			} else if (entity.getMessageState() == MessageEntity.MessageState.SENDING_VALUE) {
				ProgressBar pbSending = (ProgressBar) arg1.findViewById(R.id.pb_sending);
				pbSending.setVisibility(View.VISIBLE);
			} else if (entity.getMessageState() == MessageEntity.MessageState.SEND_FAIL_VALUE) {
				ImageView send = (ImageView) arg1.findViewById(R.id.iv_send_fail);
				Button btn = (Button) arg1.findViewById(R.id.btn_resend);
				send.setVisibility(View.VISIBLE);
				btn.setVisibility(View.VISIBLE);
				btn.setTag(entity.getMessageID());
				btn.setOnClickListener(btnOnClick);
			}
			tv_time.setText(entity.getMessageDate());
			tv_content.setText(entity.getMessageHead());
		} else if (messageType == MessageEntity.MessageType.PICTURE_VALUE) {
			if (messageViewType == MessageEntity.MessageView.RECEIVE_VALUE) {
				arg1 = mInflater.inflate(R.layout.message_list_item_left_picture, null);
			} else if (messageViewType == MessageEntity.MessageView.SEND_VALUE) {
				arg1 = mInflater.inflate(R.layout.message_list_item_right_picture, null);
			}
			TextView tv_time = (TextView) arg1.findViewById(R.id.tv_time);
			ImageView tv_content = (ImageView) arg1.findViewById(R.id.iv_chatimage);

			if (entity.getMessageState() == MessageEntity.MessageState.SEND_OK_VALUE) {
				ImageView send = (ImageView) arg1.findViewById(R.id.iv_send_ok);
				send.setVisibility(View.VISIBLE);
			} else if (entity.getMessageState() == MessageEntity.MessageState.SENDING_VALUE) {
				ProgressBar pbSending = (ProgressBar) arg1.findViewById(R.id.pb_sending);
				pbSending.setVisibility(View.VISIBLE);
			} else if (entity.getMessageState() == MessageEntity.MessageState.SEND_FAIL_VALUE) {
				ImageView send = (ImageView) arg1.findViewById(R.id.iv_send_fail);
				send.setVisibility(View.VISIBLE);
			}
			tv_time.setText(entity.getMessageDate());
			if (entity.getMessageHead().equals("")) {
				tv_content.setImageResource(R.drawable.ic_launcher);
			} else {
				String uriString = entity.getMessageHead();
				Bitmap bm = cache.get(uriString);
				if (bm == null) {
					Uri imageUri = Uri.parse(entity.getMessageHead());
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
					options.inSampleSize = Constant.computeSampleSize(options, -1, 128 * 128);
					options.inJustDecodeBounds = false;
					bm = BitmapFactory.decodeFile(imageUri.getPath(), options);
					cache.put(uriString, bm);
				}
				tv_content.setImageBitmap(bm);
			}
			tv_content.setTag(entity.getMessageHead());
			tv_content.setOnClickListener(imageOnclick);
		} else {
			System.out.println("ERROR");
		}
		return arg1;
	}
}
