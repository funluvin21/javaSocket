package NIOServer;

import java.io.Serializable;

class ST_HEADER implements Serializable{
	  public int DataLen;
	  public int TrCode;
	  public int Dest_Way;
	  public int Client_Handle;
	  public int User_Field;
	  public int Data_Type;
	  public int Client_Rtn1;
	  public int Client_Rtn2;
	  public int Client_Rtn3;
	  public byte[] User_ID;//[8]
	  //public String User_ID;
	  public byte CompressFlg;
	  public byte KillGbn;
	  public byte Filler1;
	  public byte Filler2;
	  //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
	  public byte[] Msg_cd;//[4]
	  //public String Msg_cd;
	  public int Next_KeyLen;
	  public int Option_len;
}
