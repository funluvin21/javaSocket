package objectToByte;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectToByte {

	public static void main(String[] args) {
		ST_Header stHeader = new ST_Header();
		stHeader.DataLen = 100;
		stHeader.TrCode = 5000;
		
		byte[] mByteHeader = toByteArray(stHeader);
		
		System.out.println(mByteHeader[0]);
		
		ST_Header mStHeader = new ST_Header();
		mStHeader = (ST_Header)toObject(mByteHeader);
		
		System.out.println(mStHeader.TrCode);

	}
    public static byte[] toByteArray(Object obj) {
    byte[] bytes = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
        ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(obj);
                    oos.flush();
                    oos.close();
                    bos.close();
                    bytes = bos.toByteArray();
            } catch (IOException e) {
        e.printStackTrace();
            }
            return bytes;
    }

public static Object toObject(byte[] bytes) {
    Object obj = null;
    try {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        obj = ois.readObject();
    } catch (IOException ex) {
        ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
     }
    return obj;
}
}
//Header
class ST_Header implements Serializable {
    public int HeaderSz;
    public int DataLen;
    public int TrCode;
    public int Dest_Way;
    public int Client_Handle;
    public int User_Field;
    public int Data_Type;
    public int Client_Rtn1;
    public int Client_Rtn2;
    public int Client_Rtn3;
    public byte[] User_ID;
    //public String User_ID;//[8];
    public byte CompressFlg;
    public byte KillGbn;
    public byte Filler1;
    public byte Filler2;
    public byte[] Msg_cd;//[4];
    //public String Msg_cd;//[4];
    public int Next_KeyLen;
    public int Option_len;
}
