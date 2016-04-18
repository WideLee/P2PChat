package limk.p2pchat.database;

import java.util.ArrayList;

import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class P2PChatDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "p2pchat.db";
	private static final int DB_VERSION = 1;
	private static final String MSG_TABLE_NAME = "message";
	private static final String USER_TABLE_NAME = "user";

	public P2PChatDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public P2PChatDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + MSG_TABLE_NAME
				+ " (_mid INTEGER PRIMARY KEY AUTOINCREMENT, messageID INTEGER, "
				+ " messageUUID INTEGER, messageDate VARCHAR(32),"
				+ " messageType INTEGER, messageView INTEGER,"
				+ " messageState INTEGER, isRead VARCHAR(8),"
				+ " messageHead VARCHAR(64), messagePayload text);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME
				+ " (_uid INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " userName VARCHAR(32), uuid INTEGER, ipAddress VARCHAR(24)); ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void resetUserAddress() {
		SQLiteDatabase database = getWritableDatabase();
		database.execSQL("UPDATE user SET ipAddress = ? ", new String[] { "0.0.0.0" });
		database.close();
	}

	public void updateUserAddressByUUID(String address, long uuid) {
		SQLiteDatabase database = getWritableDatabase();

		database.execSQL("UPDATE user SET ipAddress = ? WHERE uuid = ?", new Object[] { address,
				uuid });

		database.close();
	}

	public ArrayList<UserEntity> getAllUser() {
		SQLiteDatabase database = getWritableDatabase();

		ArrayList<UserEntity> allUser = new ArrayList<UserEntity>();
		Cursor cursor = database.rawQuery("SELECT * FROM user", null);

		while (cursor.moveToNext()) {
			UserEntity user = UserEntity.newBuilder()
					.setName(cursor.getString(cursor.getColumnIndex("userName")))
					.setUuid(cursor.getLong(cursor.getColumnIndex("uuid"))).build();
			allUser.add(user);
		}
		cursor.close();
		database.close();
		return allUser;
	}

	public UserEntity getUserByUUID(long uuid) {
		SQLiteDatabase database = getWritableDatabase();

		Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = ? ",
				new String[] { Long.toString(uuid) });

		UserEntity.Builder userBuilder = UserEntity.newBuilder();
		userBuilder.setName("Anonymous").setUuid(-1);

		if (cursor.moveToNext()) {
			userBuilder.setName(cursor.getString(cursor.getColumnIndex("userName"))).setUuid(
					cursor.getLong(cursor.getColumnIndex("uuid")));
		}
		cursor.close();
		database.close();
		return userBuilder.build();
	}

	public String getUserAddrByUUID(long uuid) {
		SQLiteDatabase database = getWritableDatabase();

		Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = ? ",
				new String[] { Long.toString(uuid) });

		String ipAddr = "0.0.0.0";
		if (cursor.moveToNext()) {
			ipAddr = cursor.getString(cursor.getColumnIndex("ipAddress"));
		}
		cursor.close();
		database.close();
		return ipAddr;
	}

	public void insertUser(UserEntity user) {
		SQLiteDatabase database = getWritableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = ?",
				new String[] { Long.toString(user.getUuid()) });

		if (!cursor.moveToNext()) {
			database.execSQL("INSERT INTO user (userName, uuid, ipAddress) VALUES (?, ?, ?)",
					new Object[] { user.getName(), user.getUuid(), "0.0.0.0" });
		}
		database.close();
	}

	/**
	 * 把消息加入数据库，其中数据库表项中的MessagePayload暂无实际用处，可以作为扩展使用
	 * 
	 * @param msg
	 */
	public void insertMessage(MessageEntity msg) {
		SQLiteDatabase database = getWritableDatabase();
		long messageID = msg.getMessageID();
		Cursor cursor = database.rawQuery("SELECT * FROM message WHERE messageID = ?",
				new String[] { Long.toString(messageID) });
		if (!cursor.moveToNext()) {
			database.execSQL(
					"INSERT INTO message "
							+ " (messageID, messageUUID, messageDate, messageType, messageView,"
							+ " messageState, isRead, messageHead, messagePayload) "
							+ " VALUES (?,?,?,?,?,?,?,?,?)",
					new Object[] { msg.getMessageID(), msg.getUUID(), msg.getMessageDate(),
							msg.getMessageType(), msg.getMessageView(), msg.getMessageState(),
							Boolean.toString(msg.getIsRead()), msg.getMessageHead(),
							msg.getMessageHead() });
		} else {
			database.execSQL(
					"UPDATE message SET messageHead = ?, messagePayload = ? WHERE messageID = ?",
					new Object[] { msg.getMessageHead(), msg.getMessagePayload(),
							msg.getMessageID() });
		}
		cursor.close();
		database.close();
	}

	/**
	 * 根据MessageID 查询消息
	 * 
	 * @param messageID
	 * @return 如果数据库中没有对应MessageID的消息，则返回NULL
	 */
	public MessageEntity getMessageByID(long messageID) {
		SQLiteDatabase database = getWritableDatabase();

		Cursor cursor = database.rawQuery("SELECT * FROM message WHERE messageID = ?",
				new String[] { Long.toString(messageID) });

		if (cursor.moveToNext()) {
			MessageEntity.Builder builder = MessageEntity.newBuilder();
			builder.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")))
					.setUUID(cursor.getLong(cursor.getColumnIndex("messageUUID")))
					.setMessageID(cursor.getLong(cursor.getColumnIndex("messageID")))
					.setMessageDate(cursor.getString(cursor.getColumnIndex("messageDate")))
					.setMessageView(cursor.getInt(cursor.getColumnIndex("messageView")))
					.setMessageState(cursor.getInt(cursor.getColumnIndex("messageState")))
					.setIsRead(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isRead"))))
					.setMessageHead(cursor.getString(cursor.getColumnIndex("messageHead")));

			database.close();
			cursor.close();
			return builder.build();
		} else {
			database.close();
			cursor.close();
			return null;
		}
	}

	public ArrayList<MessageEntity> getUserMessageByUUID(long uuid) {
		SQLiteDatabase database = getWritableDatabase();

		ArrayList<MessageEntity> result = new ArrayList<MessageEntity>();
		Cursor cursor = database.rawQuery("SELECT * FROM message WHERE messageUUID = ?",
				new String[] { Long.toString(uuid) });

		while (cursor.moveToNext()) {
			MessageEntity.Builder builder = MessageEntity.newBuilder();
			builder.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")))
					.setUUID(cursor.getLong(cursor.getColumnIndex("messageUUID")))
					.setMessageID(cursor.getLong(cursor.getColumnIndex("messageID")))
					.setMessageDate(cursor.getString(cursor.getColumnIndex("messageDate")))
					.setMessageView(cursor.getInt(cursor.getColumnIndex("messageView")))
					.setMessageState(cursor.getInt(cursor.getColumnIndex("messageState")))
					.setIsRead(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isRead"))))
					.setMessageHead(cursor.getString(cursor.getColumnIndex("messageHead")));

			result.add(builder.build());
		}
		cursor.close();
		database.close();
		return result;
	}

	public int getUnreadMessageCountByUUID(long uuid) {
		SQLiteDatabase database = getWritableDatabase();
		Cursor cursor = database.rawQuery(" SELECT COUNT(*) AS unReadCount FROM message"
				+ " WHERE isRead = ? AND messageUUID = ?", new String[] { Boolean.toString(false),
				Long.toString(uuid) });

		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(cursor.getColumnIndex("unReadCount"));
		}
		return count;
	}

	public ArrayList<MessageEntity> getSendingMessage() {
		SQLiteDatabase database = getWritableDatabase();

		ArrayList<MessageEntity> result = new ArrayList<MessageEntity>();
		Cursor cursor = database.rawQuery("SELECT * FROM message WHERE messageState = ?",
				new String[] { Integer.toString(MessageEntity.MessageState.SENDING_VALUE) });

		while (cursor.moveToNext()) {
			MessageEntity.Builder builder = MessageEntity.newBuilder();
			builder.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")))
					.setUUID(cursor.getLong(cursor.getColumnIndex("messageUUID")))
					.setMessageID(cursor.getLong(cursor.getColumnIndex("messageID")))
					.setMessageDate(cursor.getString(cursor.getColumnIndex("messageDate")))
					.setMessageView(cursor.getInt(cursor.getColumnIndex("messageView")))
					.setMessageState(cursor.getInt(cursor.getColumnIndex("messageState")))
					.setIsRead(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isRead"))))
					.setMessageHead(cursor.getString(cursor.getColumnIndex("messageHead")));

			result.add(builder.build());
		}
		cursor.close();
		database.close();
		return result;
	}

	public void readMessageByUUID(long uuid) {
		SQLiteDatabase database = getWritableDatabase();

		database.execSQL("UPDATE message SET isRead = ? WHERE messageUUID = ?", new Object[] {
				Boolean.toString(true), uuid });
		database.close();
	}

	public void ChangeMessageStateByID(long messageID, int state) {
		SQLiteDatabase database = getWritableDatabase();

		database.execSQL("UPDATE message SET messageState = ? WHERE messageID = ?", new Object[] {
				state, messageID });
		database.close();
	}
}
